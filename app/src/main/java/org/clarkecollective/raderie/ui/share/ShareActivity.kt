package org.clarkecollective.raderie.ui.share

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.databinding.ActivityShareBinding
import org.clarkecollective.raderie.toast
import org.clarkecollective.raderie.ui.compare.CompareActivity

class ShareActivity : AppCompatActivity() {

  private val viewModel: ShareActivityViewModel by viewModels()
  private lateinit var welcomeTV: TextView
  private lateinit var submitButton: Button
  private lateinit var personNameET: EditText
  private lateinit var wipeUserButton: Button
  private lateinit var shareButton: Button

  private val shareClickListener = object : ShareClickListener{
    override fun onShareClicked() {
      sendShareText()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_share)

    welcomeTV = findViewById(R.id.welcomeTV)
    submitButton = findViewById(R.id.submitButton)
    personNameET = findViewById(R.id.personNameET)
    wipeUserButton = findViewById(R.id.wipeUserButton)
    shareButton = findViewById(R.id.shareButton)

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
    buttonListener()

    viewModel.nameLiveData.observe(this) {
      welcomeTV.text = getString(R.string.welcome_back, it)
    }
    viewModel.clickedFriend.observe(this) {
      val compareFriendIntent = Intent(this, CompareActivity::class.java)
      compareFriendIntent.putExtra("selectedFriendID", it.uuid)
      compareFriendIntent.putExtra("selectedFriendName", it.chosenName)
      startActivity(compareFriendIntent)
    }
  }

  private fun setBinding() {
    Logger.d("Setting Binding")
    val binding: ActivityShareBinding = DataBindingUtil.setContentView(this, R.layout.activity_share)
    binding.vm = viewModel
    binding.shareClickListener = shareClickListener
    binding.lifecycleOwner = this
  }

  private fun buttonListener() {
    submitButton.setOnClickListener {
      viewModel.submitName(personNameET.text.toString())
    }
    wipeUserButton.setOnClickListener {
      viewModel.wipeUser()
      Logger.d("Wiping User")
    }
  }

  private fun sendShareText() {
    val outgoingData =
      "Hi!  Friend me over on Raderie.  " +
              "We can compare values and start a conversation about what matters.  " +
              "https://raderie.me/user/${viewModel.auth.currentUser?.uid}"
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
    //      Toast.makeText(this, "Friends list functionality coming next release", Toast.LENGTH_LONG).show()
    }
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { d, _ ->
      d.cancel()
    }
    return dialog
  }
}

interface ShareClickListener {
  fun onShareClicked()
}

@BindingAdapter("setFriendAdapter")
fun bindFriendRV(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
  recyclerView.setHasFixedSize(true)
  recyclerView.isNestedScrollingEnabled = false
  recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
  recyclerView.adapter = adapter
}