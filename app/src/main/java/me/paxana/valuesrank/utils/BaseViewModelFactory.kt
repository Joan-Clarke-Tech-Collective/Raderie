package me.paxana.valuesrank.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.paxana.valuesrank.ui.MainActivityViewModel

class BaseViewModelFactory<T>(val creator: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            val key = "MainActivityViewModel"
            if (hashMapViewModel.containsKey(key)) {
                return getViewModel(key) as T
            }
            else {
                addViewModel(key, MainActivityViewModel())
                return getViewModel(key) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    companion object {
        val hashMapViewModel = HashMap<String, ViewModel>()
        fun addViewModel(key: String, viewModel: ViewModel){
            hashMapViewModel[key] = viewModel
        }
        fun getViewModel(key: String): ViewModel? {
            return hashMapViewModel[key]
        }
    }
}