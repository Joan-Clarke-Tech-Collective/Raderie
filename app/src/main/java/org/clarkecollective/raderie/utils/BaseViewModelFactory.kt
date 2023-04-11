package org.clarkecollective.raderie.utils

import android.app.Application
import androidx.lifecycle.ViewModelProvider

class BaseViewModelFactory<T>(val creator: () -> T, private val application: Application) : ViewModelProvider.AndroidViewModelFactory(application) {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
//            val key = "MainActivityViewModel"
//            if (hashMapViewModel.containsKey(key)) {
//                return getViewModel(key) as T
//            }
//            else {
//                addViewModel(key, MainActivityViewModel(application))
//                return getViewModel(key) as T
//            }
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//    companion object {
//        val hashMapViewModel = HashMap<String, ViewModel>()
//        fun addViewModel(key: String, viewModel: ViewModel){
//            hashMapViewModel[key] = viewModel
//        }
//        fun getViewModel(key: String): ViewModel? {
//            return hashMapViewModel[key]
//        }
//    }
}