package org.clarkecollective.raderie.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.ValueRepo
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.log
import org.clarkecollective.raderie.utils.RankingCalculator

class MainActivityViewModel(application: Application): AndroidViewModel(application) {

  private val repo = ValueRepo()
  private var tempDeck = listOf<HumanValue>()
  private val remoteDeck = MutableLiveData<MutableList<HumanValue>>()
  val deck = MutableLiveData<MutableList<HumanValue>>()
  val option1 = MutableLiveData<HumanValue>()
  val option2 = MutableLiveData<HumanValue>()
  private var higher = true
  val gameOn = MutableLiveData(false)
  val dialog = MutableLiveData<Pair<HumanValue?, HumanValue?>>()
  private val compositeDisposable = CompositeDisposable()
  private val firebaseAPI = FirebaseAPI(application.applicationContext)
  private val updatedLast = MutableLiveData<Long>()
  var lastUpdatedRemote = 0L
  private var lastUpdatedLocal = 0L
  val menuClicked = MutableLiveData<MAIN_MENU>()

  private val roomDb = Room.databaseBuilder(
    getApplication<Application>().applicationContext,
    MyValuesDatabase::class.java,
    "values"
  ).fallbackToDestructiveMigration().build()
  private val valueDao = roomDb.valueDao()

  fun startGame(){
    getTimes().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<Map<SOURCE, Long>>() {
      override fun onSuccess(t: Map<SOURCE, Long>) {
        Logger.d("Map = $t")
        compareTimes(t)
      }

      override fun onError(e: Throwable) {
        e.log()
      }

    }).addTo(compositeDisposable)
  }

  private fun getTimes(): Single<Map<SOURCE, Long>> {
    return Single.zip(getLocalUpdatedLast().subscribeOn(Schedulers.io()), getRemoteUpdatedLast().subscribeOn(Schedulers.io())) { local, remote ->
      lastUpdatedLocal = local
      lastUpdatedRemote = remote
      mapOf(SOURCE.LOCAL to local, SOURCE.REMOTE to remote)
    }
  }

  private fun compareTimes(map: Map<SOURCE, Long>) {

    val remote = map[SOURCE.REMOTE] ?: 0L
    val local = map[SOURCE.LOCAL] ?: 0L

    val time = System.currentTimeMillis()

    if (local == 0L && remote == 0L) {
      Logger.d("Both DBs are empty")
      tempDeck = repo.freshDeck()
      deck.value = tempDeck.toMutableList()
      pullTwo()
      updateDeck(SOURCE.LOCAL, tempDeck, time)
      updateDeck(SOURCE.REMOTE, tempDeck, time)
    }
    else if ( (local >= remote) ) {
      Logger.d("Local DB is newer")
      valueDao.getAllValues().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(object :
          DisposableSingleObserver<List<HumanValue>>() {
          override fun onSuccess(t: List<HumanValue>) {
            tempDeck = t
            deck.value = t.toMutableList()
            pullTwo()
            updateDeck(SOURCE.LOCAL, t, time)
          }

          override fun onError(e: Throwable) {
            Logger.e(e, "Error getting local DB")
          }

        }).addTo(compositeDisposable)
    }
    else {
      Logger.d("Remote DB is newer")
      firebaseAPI.getMyDeck().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(object : DisposableSingleObserver<List<HumanValue>>() {
          override fun onSuccess(t: List<HumanValue>) {
            tempDeck = t
            deck.value = t.toMutableList()
            pullTwo()
            updateDeck(SOURCE.LOCAL, t, time)
          }

          override fun onError(e: Throwable) {
            Logger.e(e, "Error getting remote DB")
          }

        }).addTo(compositeDisposable)
    }
  }

  fun updateDeck(source: SOURCE, deck: List<HumanValue>, time:Long) {
    if (source == SOURCE.LOCAL || source == SOURCE.BOTH) {
      valueDao.insertAllValues(deck).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
        .subscribeWith(object : DisposableCompletableObserver() {
          override fun onComplete() {
            Logger.d("Local DB updated")
            setLocalUpdatedLast(time)
          }

          override fun onError(e: Throwable) {
            Logger.e(e, "Error updating local DB")
          }

        }).addTo(compositeDisposable)
    }
    if (source == SOURCE.REMOTE || source == SOURCE.BOTH) {
      firebaseAPI.mergeMyDeck(deck, time).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
        .subscribeWith(object : DisposableObserver<Int>() {
          override fun onComplete() {
            Logger.d("Remote DB updated")
          }

          override fun onNext(t: Int) {
            Logger.d("Remote DB updated: $t%")
          }

          override fun onError(e: Throwable) {
            Logger.e(e, "Error updating remote DB")
          }

        }).addTo(compositeDisposable)
    }
  }

  fun menuClicked(whichClicked: MAIN_MENU) {
    menuClicked.value = whichClicked
  }

  private fun getDecksAndCompareTimes(): Completable {
    val time = System.currentTimeMillis()
    return Single.zip(
      getLocalUpdatedLast().subscribeOn(Schedulers.io()),
      getRemoteUpdatedLast().subscribeOn(Schedulers.io())
    ) { local, remote ->
      Logger.d("Local: $local, Remote: $remote")
      if (local == 0L && remote == 0L) {
        tempDeck = repo.freshDeck()
        deck.value = tempDeck.toMutableList()
        pullTwo()
        updateDeck(SOURCE.LOCAL, repo.freshDeck(), time)
        updateDeck(SOURCE.REMOTE, repo.freshDeck(), time)
      }
      else if (local == remote || local > remote) {
        Logger.d("Local DB is newer or identical")
        valueDao.getAllValues().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object :
          DisposableSingleObserver<List<HumanValue>>() {
          override fun onSuccess(t: List<HumanValue>) {
            tempDeck = t
            deck.value = t.toMutableList()
            pullTwo()
            updateDeck(SOURCE.REMOTE, t, time)
          }

          override fun onError(e: Throwable) {
            Logger.e(e, "Error getting local DB")
          }

        }).addTo(compositeDisposable)
      } else {
        Logger.d("Remote DB is newer")
        firebaseAPI.getMyDeck().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object : DisposableSingleObserver<List<HumanValue>>() {
          override fun onSuccess(t: List<HumanValue>) {
            tempDeck = t
            deck.value = t.toMutableList()
            pullTwo()
            valueDao.insertAllValues(t).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object :
              DisposableCompletableObserver() {
              override fun onComplete() {
                Logger.d("Remote DB inserted into local DB")
              }

              override fun onError(e: Throwable) {
                e.log()
              }

            }).addTo(compositeDisposable)
          }

          override fun onError(e: Throwable) {
            e.log()
          }

        }).addTo(compositeDisposable)
      }
    }.ignoreElement()
  }

  private fun getRemoteDB(): Single<List<HumanValue>> {
    return Single.create { emitter ->
      firebaseAPI.getMyDeck().subscribeOn(Schedulers.newThread()).observeOn(Schedulers.io()).subscribeWith(object :
        DisposableSingleObserver<List<HumanValue>>() {
        override fun onSuccess(t: List<HumanValue>) {
          emitter.onSuccess(t)
        }

        override fun onError(e: Throwable) {
          Logger.e(e, "Error getting remote DB")
          emitter.onError(e)
        }

      }).addTo(compositeDisposable)
    }
  }

  private fun getLocalDB(): Single<List<HumanValue>> {
    return Single.create { emitter ->
      valueDao.getAllValues().subscribeOn(Schedulers.newThread()).observeOn(Schedulers.io()).subscribeWith(object :
        DisposableSingleObserver<List<HumanValue>>() {
        override fun onSuccess(t: List<HumanValue>) {
          if (t.isEmpty()) {
            emitter.onSuccess(repo.freshDeck())
          } else {
            emitter.onSuccess(t)
          }
        }

        override fun onError(e: Throwable) {
          when (e.message) {
            "No value found" -> {
              Logger.d("No values found in local DB")
              emitter.onSuccess(listOf())
            }
            else -> {
              Logger.e(e, "Error getting local DB")
              emitter.onError(e)
            }
          }
          emitter.onError(e)
        }

      }).addTo(compositeDisposable)
    }
  }

  private fun getLocalUpdatedLast(): Single<Long> {
    return Single.create {
      val sharedPref = getApplication<Application>().getSharedPreferences(
        "org.clarkecollective.raderie",
        Context.MODE_PRIVATE
      )
      lastUpdatedLocal =
        sharedPref.getLong(getApplication<Application>().getString(R.string.lastUpdated), 0L)
      it.onSuccess(lastUpdatedLocal)
    }
  }

  private fun setLocalUpdatedLast(time: Long) {
    val sharedPref = getApplication<Application>().getSharedPreferences(
      "org.clarkecollective.raderie",
      Context.MODE_PRIVATE
    )
    with(sharedPref.edit()) {
      putLong(getApplication<Application>().getString(R.string.lastUpdated), time)
      apply()
    }
  }

  private fun getRemoteUpdatedLast(): Single<Long> {
    return Single.create {
      firebaseAPI.getLastUpdated().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object :
        DisposableSingleObserver<Long>() {
        override fun onSuccess(t: Long) {
          lastUpdatedRemote = t
          it.onSuccess(t)
        }

        override fun onError(e: Throwable) {
          if ((e.message == getApplication<Application>().getString(R.string.no_user_error))) {
            firebaseAPI.createUser().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object :
              DisposableCompletableObserver() {
              override fun onComplete() {
                Logger.d("User created")
                it.onSuccess(0L)
              }

              override fun onError(e: Throwable) {
                e.log()
              }

            }).addTo(compositeDisposable)
          }
          else if (e.message == getApplication<Application>().getString(R.string.no_last_updated_error)) {
            firebaseAPI.updateTimestamp(0L).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object : DisposableCompletableObserver(){
              override fun onComplete() {
                it.onSuccess(0L)
              }

              override fun onError(e: Throwable) {
                e.log()
              }

            }).addTo(compositeDisposable)
          }
        }

      }).addTo(compositeDisposable)
    }
  }

  fun pullTwo() {
    // TODO: pull the two less randomly
    if (deck.value != null) {
      val splitList = deck.value?.sortedBy { it.gamesPlayed }!!.chunked(deck.value!!.size / 3)
      option1.value = splitList[1].random()
      if (higher) {
        option2.value = splitList[0].random()
        higher = false
      } else {
        val merged = splitList[2] + splitList.last()
        option2.value = merged.random()
        higher = true
      }
      gameOn.value = true
      Logger.d("Value 1: %s, Value 2: %s", option1.value!!.name, option2.value!!.name)
    }
  }

  fun decided(outcome: OUTCOME) {
    when (outcome) {
      OUTCOME.TOP -> {
        adjustRankings(option1.value!!, option2.value!!, false)
      }
      OUTCOME.BOTTOM -> {
        adjustRankings(option2.value!!, option1.value!!, false)
      }
      OUTCOME.TIE -> {
        adjustRankings(option1.value!!, option2.value!!, true)
      }
      OUTCOME.SYNONYMS -> {
        dialog.value = Pair(option1.value, option2.value)
      }
    }
    Logger.d(deck.value!!.sortedBy { it.rating }.asReversed())
  }

  private fun adjustRankings(winner: HumanValue, loser: HumanValue, tie: Boolean) {
    val newResult = RankingCalculator().calculateNewRanking(winner, loser, tie)
    winner.gamesPlayed++
    winner.rating = newResult.first
    valueDao.updateValue(winner)
    loser.gamesPlayed++
    loser.rating = newResult.second
    valueDao.updateValue(loser)

    deck.value?.find { it.id == winner.id }.let {
      it!!.gamesPlayed++
      it.rating = newResult.first
    }
    deck.value?.find { it.id == loser.id }.let {
      it!!.gamesPlayed++
      it.rating = newResult.second
    }
    onlineRankings(winner, loser)
    pullTwo()
  }

  private fun onlineRankings(winner: HumanValue, loser: HumanValue) {
    val disposableUpdater = object : DisposableCompletableObserver() {
      override fun onComplete() {
        Logger.d("Remote DB updated")
      }

      override fun onError(e: Throwable) {
        Logger.e(e, "Error updating remote DB")
      }
    }

    val updatedTime: Long = System.currentTimeMillis()

    firebaseAPI.updateCompetitors(winner, loser, updatedTime).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object: DisposableCompletableObserver(){
      override fun onComplete() {
        Logger.d("Remote DB updated")
      }

      override fun onError(e: Throwable) {
        Logger.e("Error updating remote DB")
      }

    }).addTo(compositeDisposable)
  }

  fun removeFromDeck(hv: HumanValue): Boolean {
    return deck.value!!.remove(hv)
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }
}

enum class SOURCE {
  LOCAL,
  REMOTE,
  BOTH
}

enum class OUTCOME {
  TOP,
  BOTTOM,
  TIE,
  SYNONYMS
}

enum class MAIN_MENU {
  RESULT,
  SHARE

}