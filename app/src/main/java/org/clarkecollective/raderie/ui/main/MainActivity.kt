package org.clarkecollective.raderie.ui.main

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.daos.ValueDao
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.ui.results.ResultsActivity
import org.clarkecollective.raderie.ui.share.ShareActivity

class MainActivity : AppCompatActivity() {
  private val mainActivityViewModel: MainActivityViewModel by viewModels()
  private lateinit var valueDao: ValueDao
  private lateinit var firebaseAPI: FirebaseAPI

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.addLogAdapter(AndroidLogAdapter())
    firebaseAPI = FirebaseAPI(applicationContext)

    valueDao = Room.databaseBuilder(applicationContext, MyValuesDatabase::class.java, "test-db")
      .build().valueDao()

    Logger.d("Files dir: %s",  filesDir)

    firebaseAPI.logInAndReturnUser().subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(object: SingleObserver<FirebaseUser> {
        override fun onSuccess(t: FirebaseUser) {
          Logger.d("Login successful")
          Logger.d("User ID: ${t.uid}")
          mainActivityViewModel.startGame()
        }

        override fun onSubscribe(d: Disposable) {
          Logger.d("Subscribed")
        }

        override fun onError(e: Throwable) {
          Logger.e("Login Unsuccessful: ${e.message}")
        }
      }
      )
    setupBinding()
    setupObservers()
    mainActivityViewModel.dialog.observe(this) {
      createAlertDialog(it.first, it.second).show()
    }
  }

  private fun setupObservers() {
    mainActivityViewModel.menuClicked.observe(this) {
      when (it) {
        MAIN_MENU.SHARE -> startSharingActivity()
        MAIN_MENU.RESULT -> startResultsActivity()
      }
    }
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
      R.id.sharingButton -> {
        startSharingActivity()
        true
      }
        else -> super.onOptionsItemSelected(item)
    }
  }

  private fun startSharingActivity(){
    val intent = Intent(this, ShareActivity::class.java)
    startActivity(intent)
  }

  private fun startResultsActivity() {
    val intent = Intent(this, ResultsActivity::class.java)
    val arrayDeck = mainActivityViewModel.deck.value?.let { ArrayList<HumanValue>(it) }
    intent.putExtra(getString(R.string.deckExtra), arrayDeck)
    startActivity(intent)
  }

  private fun createAlertDialog(hv1: HumanValue?, hv2: HumanValue?): AlertDialog {
    val dialog = AlertDialog.Builder(this@MainActivity).create()
    dialog.setTitle("These Words Are Synonyms...")
    dialog.setMessage("Which Is The Better Word?")
    dialog.setButton(AlertDialog.BUTTON_POSITIVE, hv1!!.name) { _, _ ->
      val success = mainActivityViewModel.removeFromDeck(hv2!!)
      if (!success) {
        Toast.makeText(applicationContext, "Item Not Found", Toast.LENGTH_SHORT).show()
      }
      else {
        Toast.makeText(applicationContext, "Deleted: " + hv2.name, Toast.LENGTH_SHORT).show()
      }
      mainActivityViewModel.pullTwo()
    }
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, hv2!!.name) { _, _ ->
      val success = mainActivityViewModel.removeFromDeck(hv1)
      if (!success) {
        Toast.makeText(applicationContext, "Item Not Found", Toast.LENGTH_SHORT).show()
      }
      else {
        Toast.makeText(applicationContext, "Deleted: " + hv1.name, Toast.LENGTH_SHORT).show()
      }
      mainActivityViewModel.pullTwo()
    }
    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel") { d, _ ->
      d.cancel()
    }
    return dialog
  }
}