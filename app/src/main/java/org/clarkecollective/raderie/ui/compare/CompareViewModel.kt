package org.clarkecollective.raderie.ui.compare

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.adapters.CompareRecyclerAdapter
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.log
import org.clarkecollective.raderie.models.HumanValue

class CompareViewModel(app: Application): AndroidViewModel(app) {
  val adapter = CompareRecyclerAdapter(this, R.layout.result_item)
  val friendDeck = mutableListOf<HumanValue?>()
  val friendDeckLV = MutableLiveData<MutableList<HumanValue>>()
  val myDeckLV = MutableLiveData<MutableList<HumanValue>>()
  val friendNameLV = MutableLiveData<String>()
  val compositeDisposable = CompositeDisposable()
  val firbaseAPI: FirebaseAPI = FirebaseAPI(app.applicationContext)
  val commonality = MutableLiveData<List<HumanValue>>()
  val compareTotal = MutableLiveData<String>()


  private val roomDb = Room.databaseBuilder(
    getApplication<Application>().applicationContext,
    MyValuesDatabase::class.java,
    "values"
  ).fallbackToDestructiveMigration().build()
  private val valueDao = roomDb.valueDao()

  fun startComparison(friendUUID: String, friendName: String) {
    Logger.d("Starting comparison")
    friendNameLV.value = friendName
    fetchFriend(friendUUID, friendName)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeWith(object : DisposableSingleObserver<Map<WHOSE, List<HumanValue>>>() {
        override fun onSuccess(t: Map<WHOSE, List<HumanValue>>) {
          Logger.d("Got Map, friends contains ${t[WHOSE.FRIEND]?.size} values, I have ${t[WHOSE.ME]?.size}")
          val friendDeck = t[WHOSE.FRIEND] as MutableList<HumanValue>
          friendDeckLV.value = friendDeck
          Logger.d("Friend deck size: ${friendDeckLV.value!!.size}")
          val myDeck = t[WHOSE.ME] as MutableList<HumanValue>
          myDeckLV.value = myDeck
          adapter.notifyItemRangeInserted(0, friendDeck.size)
          Logger.d("Commonality: %s", findCommonality())
          compareTotal.value = "These users have " + findCommonality().size + " values in common"
        }

        override fun onError(e: Throwable) {
          e.log()
        }
      }).addTo(compositeDisposable)
  }

  fun findCommonality(): List<Int> {
    Logger.d("Finding commonality")
    val friendDeck = friendDeckLV.value!!.filter { it.gamesPlayed > 0 }
    val myDeck = myDeckLV.value!!.filter { it.gamesPlayed > 0 }

    return friendDeck.filter { myDeck.contains(it) }.map { it.id }
  }

  private fun fetchFriend(friendUUID: String, friendName: String) : Single<Map<WHOSE, List<HumanValue>>> {
    Logger.d("Fetching friend")
    return Single.zip(
      fetchYou().subscribeOn(Schedulers.io()),
      fetchJustFriend(friendUUID, friendName).subscribeOn(Schedulers.io())
    ) { me, friend ->
      Logger.d("Me: $me, friend: $friend")
      mapOf(WHOSE.ME to me, WHOSE.FRIEND to friend)
    }
  }

  private fun fetchJustFriend(friendUUID: String, friendName: String) : Single<List<HumanValue>> {
    Logger.d("Fetching friend")
    return Single.create {
      firbaseAPI.getFriendDeck(friendUUID).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(object : DisposableSingleObserver<List<HumanValue>>() {
          override fun onSuccess(t: List<HumanValue>) {
            Logger.d("Friend deck: $t")
            friendDeckLV.value = t as MutableList<HumanValue>
            it.onSuccess(t)
          }

          override fun onError(e: Throwable) {
            e.log()
            it.onError(e)
          }
        }).addTo(compositeDisposable)
    }
  }

  private fun fetchYou(): Single<List<HumanValue>> {
       return Single.create {
         valueDao.getAllValues().subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread())
           .subscribeWith(object : DisposableSingleObserver<List<HumanValue>>() {
             override fun onSuccess(t: List<HumanValue>) {
               Logger.d("You: $t")
               it.onSuccess(t)
             }

             override fun onError(e: Throwable) {
               e.log()
               it.onError(e)
             }
           }).addTo(compositeDisposable)
       }
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}

enum class WHOSE {
  ME, FRIEND
}