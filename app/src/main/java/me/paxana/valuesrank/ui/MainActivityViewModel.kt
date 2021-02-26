package me.paxana.valuesrank.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import me.paxana.valuesrank.HumanValue
import me.paxana.valuesrank.ValueRepo

class MainActivityViewModel(app: Application): AndroidViewModel(app) {
    val repo = ValueRepo()
    val deck = MutableLiveData<List<() -> HumanValue>>()

    fun startGame() {
        deck.value = repo.freshDeck()
        Logger.d(deck.value)
    }


}