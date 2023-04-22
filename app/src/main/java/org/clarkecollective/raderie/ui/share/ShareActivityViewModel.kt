package org.clarkecollective.raderie.ui.share

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.log
import org.clarkecollective.raderie.models.Friend

class ShareActivityViewModel(application: Application): AndroidViewModel(application) {
  val nameLiveData = MutableLiveData<String>()
  val nameExists = MutableLiveData<Boolean>()
  private val firebaseAPI = FirebaseAPI(application)
  private val db = Firebase.firestore
  val auth = Firebase.auth

  private val usersRef = db.collection("users")
  private val docRef = usersRef.document(auth.currentUser?.uid.toString())
  private val sharedPreferences: SharedPreferences =
    getApplication<Application>().getSharedPreferences("user-data", Context.MODE_PRIVATE)
  private val friendList = ArrayList<Friend?>()
  val friendListLD = MutableLiveData<ArrayList<Friend?>>()
  val clickedFriend = MutableLiveData<Friend>()
  val shareListener = MutableLiveData<ShareButtons>()
  val selfName = ObservableField("")
  private val compositeDisposable = CompositeDisposable()
  val addedName = MutableLiveData<String>()

  private val clickInterface: FriendClickInterface = object : FriendClickInterface {
    override fun onFriendClicked(friend: Friend) {
      Logger.d(friend)
      clickedFriend.value = friend
    }
  }
  val friendsAdapter = FriendListRVAdapter(this, R.layout.friend_item, clickInterface)

  fun fetchName() {
    if (sharedPreferences.contains(getApplication<Application>().getString(R.string.choseNameKey))) {
      nameLiveData.value = sharedPreferences.getString(
        getApplication<Application>().getString(R.string.choseNameKey),
        null
      )
      nameExists.value = true
    } else nameExists.value = false

    fetchFriends()
  }

  private fun fetchFriends() {
    firebaseAPI.getFriendsList().subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeWith(object : DisposableSingleObserver<List<Friend>>() {
        override fun onSuccess(t: List<Friend>) {
          Logger.d("Friends: $t")
          friendList.addAll(t)
          friendListLD.value = friendList
          friendsAdapter.notifyItemRangeInserted(0, friendList.size)
        }

        override fun onError(e: Throwable) {
          e.log()
        }

      }).addTo(compositeDisposable)
  }

  fun onClickSubmitName() {
    val map =
      mapOf(Pair(getApplication<Application>().getString(R.string.choseNameKey), selfName.get()))
    with(sharedPreferences.edit()) {
      putString(getApplication<Application>().getString(R.string.choseNameKey), selfName.get())
      apply()
    }
// TODO Move this to API
    docRef.set(map).addOnCompleteListener {
      nameLiveData.value = selfName.get()
      nameExists.value = true
    }
  }

  fun onShareClicked() {
    shareListener.value = ShareButtons.SHARE
  }

  fun onWipeUserClicked() {
    val map = mapOf(
      Pair(
        getApplication<Application>().getString(R.string.choseNameKey),
        FieldValue.delete()
      )
    )
    with(sharedPreferences.edit()) {
      remove(getApplication<Application>().getString(R.string.choseNameKey)).commit()
    }
    docRef.update(map)
    nameExists.value = false
  }

  fun addFriend(uuid: String) {
    Logger.d("Adding Friend")
    firebaseAPI.addFriend(uuid).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeWith(object : DisposableSingleObserver<Friend>() {
        override fun onSuccess(t: Friend) {
          friendList.add(t)
          friendListLD.value = friendList
          friendsAdapter.notifyItemRangeInserted(friendList.size - 1, 1)
        }

        override fun onError(e: Throwable) {
          e.log()
        }

      }).addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
    firebaseAPI.dispose()
  }
}

enum class ShareButtons {
  SHARE, WIPE_DATA
}