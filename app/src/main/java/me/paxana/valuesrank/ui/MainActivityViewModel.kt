package me.paxana.valuesrank.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.orhanobut.logger.Logger
import me.paxana.valuesrank.HumanValue
import me.paxana.valuesrank.R
import me.paxana.valuesrank.ValueRepo
import me.paxana.valuesrank.adapters.ResultsRecyclerAdapter
import me.paxana.valuesrank.utils.RankingCalculator

class MainActivityViewModel(): ViewModel() {
    val repo = ValueRepo()
    val deck = MutableLiveData<List<HumanValue>>()
    val option1 = MutableLiveData<HumanValue>()
    val option2 = MutableLiveData<HumanValue>()
    var higher = true
    val gameOn = MutableLiveData(false)
    val adapter = ResultsRecyclerAdapter(this, R.layout.result_item)

    fun startGame() {
        gameOn.value = true
        deck.value = repo.freshDeck()
        pullTwo()
    }

    private fun pullTwo() {
        if (deck.value != null) {
            val splitList = deck.value?.sortedBy { it.rating }!!.chunked(deck.value!!.size / 3)
            Logger.d("Split into %s chunks.  First chunk is %s long. Remainder chunk is %s long", splitList.size, splitList[0].size, splitList[3].size)
            option1.value = splitList[1].random()
            if (higher) {
                option2.value = splitList[0].random()
                higher = false
            }
            else {
                val merged = splitList[2] + splitList[3]
                option2.value = merged.random()
                higher = true
            }
            Logger.d("Value 1: %s, Value 2: %s", option1.value!!.name, option2.value!!.name )
        }
    }
    fun winner(outcome: OUTCOME) {
        var won: HumanValue? = null
        var lost: HumanValue? = null
        if (outcome == OUTCOME.TOP) {
            won = option1.value
            lost = option2.value
        }
        else if (outcome == OUTCOME.BOTTOM) {
            won = option2.value
            lost = option1.value
        }
        Logger.d("%s is the winner. %s is the loser", won!!.name, lost!!.name)

        deck.value!!.find { it.id == won.id }.let {
            it!!.gamesPlayed++
            it.gamesWon++
            it.rating = RankingCalculator().calculateNewRanking(it, lost, "+")
        }
        deck.value!!.find { it.id == lost.id }.let {
            it!!.gamesPlayed++
            it.gamesLost++
            it.rating = RankingCalculator().calculateNewRanking(it, won, "-")
        }

        pullTwo()
        Logger.d(deck.value!!.sortedBy { it.rating }.asReversed())

    }
}

enum class OUTCOME {
    TOP,
    BOTTOM,
    TIE
}