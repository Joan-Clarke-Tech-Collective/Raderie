package org.clarkecollective.raderie.ui.share

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.databinding.ActivityShareBinding
import org.clarkecollective.raderie.log
import org.clarkecollective.raderie.toast
import org.clarkecollective.raderie.ui.compare.CompareActivity

class ShareActivity : AppCompatActivity() {

  companion object {
    fun newIntent(context: Context): Intent {
      return Intent(context, ShareActivity::class.java)
    }
  }

  private val viewModel: ShareActivityViewModel by viewModels()
  private lateinit var welcomeTV: TextView

  private lateinit var firebaseAPI: FirebaseAPI
  private val compositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_share)

    firebaseAPI = FirebaseAPI(application)

    setObservers()

    if (intent.getStringExtra(getString(R.string.CallingActivity)) == getString(R.string.LinksActivity)) {
      val friendUUID = intent.getStringExtra(getString(R.string.frienduuid)) ?: ""
      Logger.d("Friend UUID = $friendUUID")
      this@ShareActivity.toast("Add friend: $friendUUID?")
      addFriendConfirmDialogue(friendUUID).show()
    }
    viewModel.fetchName()
    setBinding()
  }

  private fun setObservers() {
    viewModel.nameLiveData.observe(this) {
      welcomeTV.text = getString(R.string.welcome_back, it)
    }
    viewModel.clickedFriend.observe(this) {
      val compareFriendIntent = Intent(this, CompareActivity::class.java)
      compareFriendIntent.putExtra("selectedFriendID", it.uuid)
      compareFriendIntent.putExtra("selectedFriendName", it.chosenName)
      startActivity(compareFriendIntent)
    }

    viewModel.shareListener.observe(this) {
      when (it) {
        ShareButtons.SHARE -> sendShareText()
        ShareButtons.WIPE_DATA -> {}
        else -> {}
      }
      sendShareText()
    }

    val sharedPreferences = getSharedPreferences("user-data", Context.MODE_PRIVATE)

    viewModel.addedName.observe(this) {
      welcomeTV.text = getString(R.string.welcome_back, it)
      with (sharedPreferences.edit()) {
        putString(getString(R.string.choseNameKey), it)
        apply()
      }
      firebaseAPI.addName(it).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribeWith(object: DisposableCompletableObserver() {
        override fun onComplete() {
          Logger.d("Added name to firebase")
        }

        override fun onError(e: Throwable) {
          e.log()
        }
      }).addTo(compositeDisposable)
    }
  }

  private fun setBinding() {
    Logger.d("Setting Binding")
    val binding: ActivityShareBinding = DataBindingUtil.setContentView(this, R.layout.activity_share)
    welcomeTV = binding.welcomeTV

    // TODO A lot of these could be done with data binding

    binding.vm = viewModel
    binding.lifecycleOwner = this
  }

  private fun sendShareText() {
    val outgoingData = getString(R.string.friend_me, viewModel.auth.currentUser?.uid)
    val sendIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, outgoingData)
      type = "text/plain"
    }
    startActivity(sendIntent)
  }
  private fun addFriendConfirmDialogue(newFriendUID: String): AlertDialog {
    val dialog = AlertDialog.Builder(this@ShareActivity).create()
    dialog.setTitle("Add Friend?")
    dialog.setMessage("Do you want to add $newFriendUID to your friend list?")
    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ ->
      viewModel.addFriend(newFriendUID)
    }
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { d, _ ->
      d.cancel()
    }
    return dialog
  }

  override fun onDestroy() {
    firebaseAPI.dispose()
    super.onDestroy()
  }
}

@BindingAdapter("setFriendAdapter")
fun bindFriendRV(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
  recyclerView.setHasFixedSize(true)
  recyclerView.isNestedScrollingEnabled = false
  recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
  recyclerView.adapter = adapter
}