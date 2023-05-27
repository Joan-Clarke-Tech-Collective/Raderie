package org.clarkecollective.raderie.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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

  val deck = MutableLiveData<MutableList<HumanValue>>()
  val option1 = MutableLiveData<HumanValue>()
  val option2 = MutableLiveData<HumanValue>()
  val gameOn = MutableLiveData(false)
  val dialog = MutableLiveData<Pair<HumanValue?, HumanValue?>>()
  private val compositeDisposable = CompositeDisposable()
  private val firebaseAPI = FirebaseAPI(application.applicationContext)
  val menuClicked = MutableLiveData<MAINMENU>()
  val toDefine = MutableLiveData<Int>()

  private val roomDb = Room.databaseBuilder(
    getApplication<Application>().applicationContext,
    MyValuesDatabase::class.java,
    getApplication<Application>().getString(R.string.databaseInUse)
  ).fallbackToDestructiveMigration().build()
  private val valueDao = roomDb.valueDao()

  //TODO have the game begin right away, then just use this to correct any issues between the brand new deck and the users actual deck
  //TODO Make items in the deck clickable only once the game has begun

  fun beginGame(list: List<HumanValue>) {
    deck.value = list.toMutableList()
    pullTwoLessRandomly()
  }

  fun menuClicked(whichClicked: MAINMENU) {
    menuClicked.value = whichClicked
  }

  fun pullTwoLessRandomly() {
    val twoCards = repo.drawTwo(deck.value!!)
    option1.value = twoCards[0]
    option2.value = twoCards[1]
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
    pullTwoLessRandomly()
  }

  private fun onlineRankings(winner: HumanValue, loser: HumanValue) {
    val updatedTime: Long = System.currentTimeMillis()

    firebaseAPI.updateCompetitors(winner, loser, updatedTime).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object: DisposableCompletableObserver(){
      override fun onComplete() {
        Logger.d("Remote DB updated")
      }

      override fun onError(e: Throwable) {
        e.log()
      }

    }).addTo(compositeDisposable)
  }

  fun onLongPress(outcome: OUTCOME): Boolean {
    when (outcome) {
      OUTCOME.TOP -> { toDefine.value = option1.value?.id }
      OUTCOME.BOTTOM -> { toDefine.value = option2.value?.id }
      OUTCOME.TIE -> { toDefine.value = -1 }
      OUTCOME.SYNONYMS -> { toDefine.value = -2 }
    }
    return false
  }

  fun removeFromDeck(hv: HumanValue): Boolean {
    return deck.value!!.remove(hv)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
    firebaseAPI.dispose()
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

enum class MAINMENU {
  RESULT,
  SHARE
}