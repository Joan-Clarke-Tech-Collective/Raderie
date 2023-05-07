package org.clarkecollective.raderie.ui.compare

import android.app.Application
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
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
import org.clarkecollective.raderie.capitalizeWords
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.log
import org.clarkecollective.raderie.models.HumanValue
import kotlin.math.max
import kotlin.math.min

class CompareViewModel(app: Application): AndroidViewModel(app) {
  val adapter = CompareRecyclerAdapter(this, R.layout.compare_item)
  val friendDeckLV = MutableLiveData<List<HumanValue>>()
  val myDeckLV = MutableLiveData<List<HumanValue>>()
  val friendNameLV = MutableLiveData<String>()
  private val compositeDisposable = CompositeDisposable()
  private val firebaseAPI: FirebaseAPI = FirebaseAPI(app.applicationContext)
  val compareTotal = MutableLiveData<String>()
  val compareLV = MutableLiveData<List<Comparison>>()
  val comparisonCard1: MutableLiveData<String> = MutableLiveData()
  val comparisonCard2: MutableLiveData<String> = MutableLiveData()
  private val entries = enumValues<SORTBY>().toList()
  val entryNames = entries.map { it.name.replace("_", " ") }
  val selectedItemPosition = MutableLiveData<Int>()
  private val selectedItem = MutableLiveData<SORTBY>()

  private val roomDb = Room.databaseBuilder(
    getApplication<Application>().applicationContext,
    MyValuesDatabase::class.java,
    "values"
  ).fallbackToDestructiveMigration().build()
  private val valueDao = roomDb.valueDao()

  fun startComparison(friendUUID: String, friendName: String) {
    Logger.d("Starting comparison")
    friendNameLV.value = friendName.capitalizeWords()
    fetch(friendUUID, friendName)
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
          val commonality = findCommonality(t)
          compareLV.value = commonality.sortedBy { it.getDelta() }
          calculateDeltas(SORTBY.SIMILAR)
          adapter.notifyItemRangeInserted(0, friendDeck.size)
          Logger.d("Commonality size: %s", commonality.size)
          compareTotal.value = "These users have " + commonality.size.toString() + " values in common"
        }

        override fun onError(e: Throwable) {
          e.log()
        }
      }).addTo(compositeDisposable)
  }

  fun findCommonality(mapOfBoth: Map<WHOSE, List<HumanValue>>): List<Comparison> {
    Logger.d("Finding commonality")

    val friendDeck = mapOfBoth[WHOSE.FRIEND]!!
    val myDeck = mapOfBoth[WHOSE.ME]!!

    Logger.d("Friend deck size: %s, My deck size: %s", friendDeck.size, myDeck.size)

    val commonIDs = friendDeck.filter { f1 -> myDeck.filter { f2 -> f1.id == f2.id }.size == 1 }.map { it.id }
    commonIDs.forEach { commonID ->
      Logger.d("ID: %s, Friend score: %s, My deck: %s", commonID, friendDeck.find { finder -> finder.id == commonID }?.rating, myDeck.find { finder -> finder.id == commonID }?.rating)
    }
    val result = commonIDs.map { sharedID ->
      return@map Comparison(sharedID, friendDeck.find { it.id == sharedID }!!, myDeck.find { it.id == sharedID }!!)
    }
    return result
  }

  private fun fetch(friendUUID: String, friendName: String) : Single<Map<WHOSE, List<HumanValue>>> {
    Logger.d("Fetching friend")
    return Single.zip(
      fetchYou().subscribeOn(Schedulers.io()),
      fetchJustFriend(friendUUID, friendName).subscribeOn(Schedulers.io())
    ) { me, friend ->
      val meFiltered = me.filter { it.gamesPlayed > 0 }
      val friendFiltered = friend.filter { it.gamesPlayed > 0 }
      Logger.d("Me filtered: $meFiltered, friend filtered: $friendFiltered")
      mapOf(WHOSE.ME to meFiltered, WHOSE.FRIEND to friendFiltered)
    }
  }

  // TODO: This is a duplicate of the same method in the DeckViewModel. Refactor to a common place
  // TODO: Check name of friend for changes here
  private fun fetchJustFriend(friendUUID: String, friendName: String) : Single<List<HumanValue>> {
    Logger.d("Fetching friend")
    return Single.create {
      firebaseAPI.getFriendDeck(friendUUID).subscribeOn(Schedulers.io())
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
               myDeckLV.value = t
               it.onSuccess(t)
             }

             override fun onError(e: Throwable) {
               e.log()
               it.onError(e)
             }
           }).addTo(compositeDisposable)
       }
  }
  private fun calculateDeltas(sortBy: SORTBY): Pair<Comparison?, Comparison?> {
    // TODO: account for no commonality

    // TODO: Extract these string resources
    // TODO: Refresh these factoid cards with button

    val topTwo = compareLV.value?.take(2)

    if (topTwo == null) {
      Logger.e("No commonality")
      return Pair(null, null)
    }

    comparisonCard1.value = cardString(topTwo[0], sortBy)
    comparisonCard2.value = cardString(topTwo[1], sortBy)

    return Pair(topTwo[0], topTwo[1])
  }

  private fun cardString(comparison: Comparison, sortBy: SORTBY): String {
    when (sortBy) {
      SORTBY.SIMILAR -> {
        return when {
          comparison.me.rating > 0 -> { getApplication<Application>().getString(R.string.positive, comparison.me.name) }
          comparison.me.rating < 0 -> { getApplication<Application>().getString(R.string.negative, comparison.me.name) }
          else -> { getApplication<Application>().getString(R.string.neutral, comparison.me.name) }
        }
      }

      SORTBY.DIFFERENT -> {
        return when {
          comparison.me.rating > comparison.friend.rating -> { getApplication<Application>().getString(R.string.who_cares_more,"You", comparison.me.name, friendNameLV.value ) }
          comparison.me.rating < comparison.friend.rating -> { getApplication<Application>().getString(R.string.who_cares_more, friendNameLV.value, comparison.me.name, "you") }
          else -> { getApplication<Application>().getString(R.string.neutral, comparison.me.name) }
        }
      }

      SORTBY.I_LIKE -> {
        return when {
          comparison.friend.rating > 0 -> { getApplication<Application>().getString(R.string.positive, comparison.me.name) }
          comparison.friend.rating < 0 -> { getApplication<Application>().getString(R.string.who_cares_more,"You", comparison.me.name, friendNameLV.value) }
          else -> { getApplication<Application>().getString(R.string.they_indifferent, comparison.me.name, friendNameLV.value) }
        }
      }

      SORTBY.THEY_LIKE -> {
        return when {
          comparison.me.rating > 0 -> { getApplication<Application>().getString(R.string.positive, comparison.me.name) }
          comparison.me.rating < 0 -> { getApplication<Application>().getString(R.string.who_cares_more, friendNameLV.value, comparison.me.name, "you") }
          else -> { getApplication<Application>().getString(R.string.i_indifferent, friendNameLV.value, comparison.me.name) }
        }
      }

      SORTBY.I_HATE -> {
        return when {
          comparison.friend.rating > 0 -> { getApplication<Application>().getString(R.string.who_cares_more,"You", comparison.me.name, friendNameLV.value) }
          comparison.friend.rating < 0 -> { getApplication<Application>().getString(R.string.negative, comparison.me.name) }
          else -> { getApplication<Application>().getString(R.string.they_indifferent, comparison.me.name, friendNameLV) }
        }
      }

      SORTBY.THEY_HATE -> {
        return when {
          comparison.me.rating > 0 -> { getApplication<Application>().getString(R.string.who_cares_more, friendNameLV.value, comparison.me.name, "you") }
          comparison.me.rating < 0 -> { getApplication<Application>().getString(R.string.negative, comparison.me.name) }
          else -> { getApplication<Application>().getString(R.string.i_indifferent, friendNameLV.value, comparison.me.name) }
        }
      }
    }
  }

  fun onSelectSortBy(position: Int) {
    Logger.d("Selected position in VM: $position")
    selectedItemPosition.value = position
    selectedItem.value = entries[position]
    sortValues(entries[position])
  }

  private fun sortValues(sortBy: SORTBY) {
    Logger.d("Sorting by: $sortBy")
    when (sortBy) {
      SORTBY.SIMILAR -> { compareLV.value = compareLV.value?.sortedBy { it.getDelta() } }
      SORTBY.DIFFERENT -> compareLV.value = compareLV.value?.sortedByDescending { it.getDelta() }
      SORTBY.I_LIKE -> compareLV.value = compareLV.value?.sortedByDescending { it.me.rating }
      SORTBY.THEY_LIKE -> compareLV.value = compareLV.value?.sortedByDescending { it.friend.rating }
      SORTBY.I_HATE -> compareLV.value = compareLV.value?.sortedBy { it.me.rating }
      SORTBY.THEY_HATE -> compareLV.value = compareLV.value?.sortedBy { it.friend.rating }
    }
    calculateDeltas(sortBy)
    // TODO: DiffUtil Callback or SortedList for data efficiency
    // TODO: Recreate the card content based on the sort
    adapter.notifyDataSetChanged()
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
    firebaseAPI.dispose()
  }
}

enum class WHOSE {
  ME, FRIEND
}

enum class SORTBY {
  SIMILAR, DIFFERENT, I_LIKE, THEY_LIKE, I_HATE, THEY_HATE
}

class Comparison(val id: Int, val friend: HumanValue, val me: HumanValue) {
  fun getDelta(): Int { return (max(friend.rating, me.rating) - min(friend.rating, me.rating)) }
  fun getColorDelta() = Color.argb(255, (255 * (getDelta() / 100)), (255 * (100 - getDelta()) / 100), 0)
}

// Spinner adapters
@BindingAdapter("customEntries")
fun Spinner.setEntries(entries: List<String>) {
  //TODO Change these names
  val adapter = ArrayAdapter(context, R.layout.custom_sort_spinner_item, entries)
  adapter.setDropDownViewResource(R.layout.custom_sort_spinner_item)
  this.adapter = adapter
}