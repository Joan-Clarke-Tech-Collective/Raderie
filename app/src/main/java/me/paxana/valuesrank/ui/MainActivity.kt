package me.paxana.valuesrank.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import me.paxana.valuesrank.R
import me.paxana.valuesrank.getViewModel

class MainActivity : AppCompatActivity() {
  private val mainActivityViewModel by lazy {
    getViewModel { MainActivityViewModel(application) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    Logger.addLogAdapter(AndroidLogAdapter())
    mainActivityViewModel.startGame()


  }
}