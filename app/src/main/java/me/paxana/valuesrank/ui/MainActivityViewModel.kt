package me.paxana.valuesrank.ui

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.orhanobut.logger.Logger
import me.paxana.valuesrank.HumanValue
import me.paxana.valuesrank.R
import me.paxana.valuesrank.ValueRepo
import me.paxana.valuesrank.adapters.ResultsRecyclerAdapter
import me.paxana.valuesrank.utils.RankingCalculator

class MainActivityViewModel(application: Application): AndroidViewModel(application) {
    private val repo = ValueRepo()
    val deck = MutableLiveData<MutableList<HumanValue>>()
    val option1 = MutableLiveData<HumanValue>()
    val option2 = MutableLiveData<HumanValue>()
    var higher = true
    val gameOn = MutableLiveData(false)
    val adapter = ResultsRecyclerAdapter(this, R.layout.result_item)
    val dialog = MutableLiveData<Pair<HumanValue, HumanValue>>()

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
    fun decided(outcome: OUTCOME) {

        when (outcome) {
            OUTCOME.TOP -> {
                adjustRankings(option1.value!!, option2.value!!, false)
            }
            OUTCOME.BOTTOM -> {
                adjustRankings(option2.value!!, option1.value!!, false)
            }
            OUTCOME.TIE -> {
                adjustRankings(option1.value!!, option2.value!!, true )
            }
            OUTCOME.SYNONYMS -> {
                dialog.value = Pair(option1.value!!, option2.value!!)
            }
        }

        Logger.d(deck.value!!.sortedBy { it.rating }.asReversed())

    }
    private fun adjustRankings(winner: HumanValue, loser: HumanValue, tie: Boolean) {
        val newRankingsMap = RankingCalculator().calculateNewRanking(winner, loser, tie)
        newRankingsMap.forEach { entry ->
            deck.value!!.find {it.id == entry.key.id}.let {
                it!!.gamesPlayed++
                it.rating = entry.value
                if (it.id == winner.id && !tie) {
                    it.gamesWon++
                }
                else if (it.id == loser.id && !tie) {
                    it.gamesLost++
                }
            }
        }
        pullTwo()
    }
    private fun removeFromDeck(hv: HumanValue): Boolean {
        return deck.value!!.remove(hv)
    }
    fun createAlertDialog(hv1: HumanValue, hv2: HumanValue, context: Context): AlertDialog {
        val dialog = AlertDialog.Builder(context).create()
        dialog.setTitle("These Words Are Synonyms...")
        dialog.setMessage("Which Is The Better Word?")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, hv1.name) { _, _ ->
            val success = removeFromDeck(hv2)
            if (!success) {
                Toast.makeText(getApplication(), "Item Not Found", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(getApplication(), "Deleted: " + hv2.name, Toast.LENGTH_SHORT).show()
            }
            pullTwo()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, hv2.name) { _, _ ->
            val success = removeFromDeck(hv1)
            if (!success) {
                Toast.makeText(getApplication(), "Item Not Found", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(getApplication(), "Deleted: " + hv1.name, Toast.LENGTH_SHORT).show()

            }
            pullTwo()
        }
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel") { d, which ->
            d.cancel()
        }
        return dialog
    }
}

enum class OUTCOME {
    TOP,
    BOTTOM,
    TIE,
    SYNONYMS
}