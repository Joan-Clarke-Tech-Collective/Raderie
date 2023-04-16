package org.clarkecollective.raderie.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.adapters.ResultsRecyclerAdapter

class ResultsActivityViewModel(application: Application): AndroidViewModel(application) {
//    val deck = MutableLiveData<MutableList<HumanValue>>()
    val adapter = ResultsRecyclerAdapter(this, R.layout.result_item)
    val meaningfulDeck = MutableLiveData<List<HumanValue>>()

    fun setDeck(usedDeck: ArrayList<HumanValue>) {
        meaningfulDeck.value = onlyMeaningfulResults(usedDeck)
        Logger.d("Meaningful deck size: " + meaningfulDeck.value?.size)
        adapter.notifyItemRangeInserted(0, meaningfulDeck.value?.size ?: 0)
    }
    private fun onlyMeaningfulResults(results: List<HumanValue>): MutableList<HumanValue> {
        return results.filter {
            it.gamesPlayed > 0
        }.toMutableList()
}

}