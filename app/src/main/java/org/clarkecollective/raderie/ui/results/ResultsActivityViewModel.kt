package org.clarkecollective.raderie.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.adapters.ResultsRecyclerAdapter

class ResultsActivityViewModel(application: Application): AndroidViewModel(application) {
    val deck = MutableLiveData<MutableList<HumanValue>>()
    val adapter = ResultsRecyclerAdapter(this, R.layout.result_item)
    private val meaningfulDeck = MutableLiveData<MutableList<HumanValue>>()

    fun setDeck(usedDeck: ArrayList<HumanValue>) {
        deck.value = usedDeck.toMutableList()
    }
//TODO don't compute this every time
    fun onlyMeaningfulResults(): MutableList<HumanValue>? {
        meaningfulDeck.value = deck.value!!.filter {
            it.gamesPlayed > 0
        }.toMutableList()

        return meaningfulDeck.value
    }
}