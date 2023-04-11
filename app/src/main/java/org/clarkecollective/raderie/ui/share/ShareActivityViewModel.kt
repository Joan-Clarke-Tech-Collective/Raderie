package org.clarkecollective.raderie.ui.share

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.models.Friend

class ShareActivityViewModel(application: Application): AndroidViewModel(application) {
  val nameLiveData = MutableLiveData<String>()
  val nameExists = MutableLiveData<Boolean>()
  private val db = Firebase.firestore
  val auth = Firebase.auth
  //TODO don't make so many calls to auth
  private val usersRef = db.collection("usies")
  private val docRef = usersRef.document(auth.currentUser?.uid.toString())
  private val friendRef = docRef.collection("friendList")
  private val sharedPreferences: SharedPreferences = getApplication<Application>().getSharedPreferences("user-data", Context.MODE_PRIVATE)
  private val friendList = ArrayList<Friend?>()
  val friendListLD = MutableLiveData<ArrayList<Friend?>>()
  val clickedFriend = MutableLiveData<Friend>()
  val shareListener = MutableLiveData(0)

  private val clickInterface: FriendClickInterface = object : FriendClickInterface {
    override fun onFriendClicked(friend: Friend) {
      Logger.d(friend)
      clickedFriend.value = friend
    }
  }
  val friendsAdapter = FriendListRVAdapter(this, R.layout.friend_item, clickInterface)

  fun fetchName() {
    if (sharedPreferences.contains(getApplication<Application>().getString(R.string.choseNameKey))) {
      nameLiveData.value = sharedPreferences.getString(getApplication<Application>().getString(R.string.choseNameKey), null)
      nameExists.value = true
      }
    else nameExists.value = false

    fetchFriends()
  }

  private fun fetchFriends() {
    friendRef.get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        friendList.addAll(task.result.documents.map {
          it.toObject(Friend::class.java)
        }.toList())

        friendListLD.value = friendList
        friendsAdapter.notifyItemRangeInserted(0, friendList.size)
      }
      else {
        task.exception!!.localizedMessage?.let { Logger.e(it) }
      }
    }
  }

  fun submitName(name: String) {
    val map = mapOf(Pair(getApplication<Application>().getString(R.string.choseNameKey), name))
    with (sharedPreferences.edit()) {
      putString(getApplication<Application>().getString(R.string.choseNameKey), name)
      apply()
    }

    docRef.set(map).addOnCompleteListener {
      nameLiveData.value = name
      nameExists.value = true
    }
  }

  fun wipeUser() {
    val map = mapOf(Pair(getApplication<Application>().getString(R.string.choseNameKey), FieldValue.delete()))
    with (sharedPreferences.edit()) {
      remove(getApplication<Application>().getString(R.string.choseNameKey)).commit()
    }
   docRef.update(map)
    nameExists.value = false
  }

  fun addFriend(uuid: String) {
    Logger.d("Adding friend")
    usersRef.document(uuid).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        Logger.d("Task result = ${task.result}")
        val document = task.result
        if (document != null) {
          document.reference.collection("deck").whereGreaterThan("gamesPlayed", 0) .get().addOnCompleteListener { greaterThanTask ->
            Logger.d("It found (deck): $task.result.size()} items large")
            val newFriend = Friend(uuid, document["userChosenName"].toString(), greaterThanTask.result.size() )
            Logger.d(newFriend)
            friendRef.document(uuid).set(newFriend)
          }
        }
        else {
          Logger.d("Collection is null")
        }
      }
      else {
        Logger.e("Document get failed.  ${task.exception}")
      }
    }
  }
}