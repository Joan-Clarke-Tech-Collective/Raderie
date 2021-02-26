package me.paxana.valuesrank.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import me.paxana.valuesrank.R
import me.paxana.valuesrank.databinding.ActivityMainBinding
import me.paxana.valuesrank.getViewModel

class MainActivity : AppCompatActivity() {
  private val mainActivityViewModel by lazy {
    getViewModel { MainActivityViewModel() }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.addLogAdapter(AndroidLogAdapter())
    setupBinding()
    mainActivityViewModel.startGame()
  }

  private fun setupBinding() {
    val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.viewModel = mainActivityViewModel
    binding.lifecycleOwner = this
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.resultsMenuItem -> {
        startResultsActivity()
        true
      }
        else -> super.onOptionsItemSelected(item)
    }
  }

  private fun startResultsActivity() {
    val intent = Intent(this, ResultsActivity::class.java)
    startActivity(intent)
  }
}