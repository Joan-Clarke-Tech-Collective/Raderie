package org.clarkecollective.raderie.api

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.ValueRepo
import org.clarkecollective.raderie.log
import org.clarkecollective.raderie.models.Friend
import org.clarkecollective.raderie.models.HumanValue

class FirebaseAPI(val context: Context) {

  private val db = Firebase.firestore
  private val auth = Firebase.auth
  private val userRef = db.collection("users")
  private var docRef =
    userRef.document(auth.currentUser?.uid.toString()).collection("testDeck")
  private var friendsRef =
    userRef.document(auth.currentUser?.uid.toString()).collection("friendsList")
  private val repo = ValueRepo()
  private val compositeDisposable = io.reactivex.rxjava3.disposables.CompositeDisposable()

  fun getMyDeck(): Single<List<HumanValue>> {
    return Single.create { emitter ->
      docRef.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val collection = task.result
          if (collection != null) {
            if (!collection.isEmpty) {
              // User has a saved deck
              emitter.onSuccess(collection.toObjects(HumanValue::class.java))
            } else {
              //User has no saved deck
              emitter.onSuccess(repo.freshDeckObject())
//              emitter.onError(Exception(context.getString(R.string.no_deck_error)))
            }
          } else {
            // There is no user in the DB
            createUser()
            emitter.onError(Exception(context.getString(R.string.no_user_error)))
          }
        }
      }
    }
  }

    fun getFriendDeck(friendID: String): Single<List<HumanValue>> {
      return Single.create {
        userRef.document(friendID).collection("deck").get().addOnCompleteListener { task ->
          if (task.isSuccessful) {
            val collection = task.result
            if (collection != null) {
              if (!collection.isEmpty) {
                // User has a saved deck
                it.onSuccess(collection.toObjects(HumanValue::class.java))
              } else {
                //User has no saved deck
                it.onError(Exception(context.getString(R.string.no_deck_error)))
              }
            } else {
              // There is no user in the DB
              it.onError(Exception(context.getString(R.string.no_user_error)))
            }
          }
        }
      }
    }

  fun logInAndReturnUser(): Single<FirebaseUser> {
    return Single.create { emitter ->
      auth.signInAnonymously()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            emitter.onSuccess(task.result.user!!)
          } else {
            emitter.onError(Throwable(task.exception!!.message))
          }
        }
    }
  }

  fun getLastUpdated(): Single<Long> {
    return Single.create {
      userRef.document(auth.currentUser?.uid.toString()).get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val user = task.result
          if (user != null) {
            if (user.exists()) {
              val lastUpdated = user.get("lastUpdate")
              if (lastUpdated != null) {
                it.onSuccess(lastUpdated as Long)
              }
              else {
                it.onError(Throwable(context.getString(R.string.no_last_updated_error)))
              }
            }
            else {
              it.onError(Throwable(context.getString(R.string.no_user_error)))
            }
          }
        }
      }
    }
  }

  fun mergeMyDeck(deck: List<HumanValue>, updateTime: Long): Observable<Int> {
    return Observable.create { emitter ->
      deck.forEachIndexed { index, humanValue ->
        docRef.document(humanValue.id.toString()).set(humanValue).addOnCompleteListener {
          if (it.isSuccessful) {
            val percent = (index / deck.size)
            emitter.onNext(percent)
          } else {
            emitter.onError(Throwable(it.exception!!.message))
          }
        }
      }
      updateTimestamp(updateTime).subscribe()
    }
  }

  fun updateTimestamp(time: Long): Completable {
    return Completable.create { emitter ->
      userRef.document(auth.currentUser?.uid.toString()).update("lastUpdate", time).addOnCompleteListener { task ->
        if (task.isSuccessful) {
          emitter.onComplete()
        } else {
          emitter.onError(Throwable(task.exception!!.message))
        }
      }
    }
  }

  fun updateCompetitors(winner: HumanValue, loser: HumanValue, updateTime: Long): Completable {
    val winnerCompletable = Completable.create { emitter ->
      docRef.document(winner.id.toString()).set(winner).addOnCompleteListener { deckUpdateTask ->
        if (deckUpdateTask.isSuccessful) {
          userRef.document(auth.currentUser?.uid.toString()).update("lastUpdate", updateTime).addOnCompleteListener { timeUpdateTask ->
            if (timeUpdateTask.isSuccessful) {
              emitter.onComplete()
            } else {
              emitter.onError(Throwable(timeUpdateTask.exception!!.message))
            }
          }
        } else {
          emitter.onError(Throwable(deckUpdateTask.exception!!.message))
        }
      }
    }
    val loserCompletable = Completable.create { emitter ->
      docRef.document(loser.id.toString()).set(loser).addOnCompleteListener {
        if (it.isSuccessful) {
          emitter.onComplete()
        } else {
          emitter.onError(Throwable(it.exception!!.message))
        }
      }
    }

    return Completable.mergeArray(winnerCompletable, loserCompletable)
  }

  fun createUser(): Completable {
    return Completable.create { emitter ->
      userRef.document(auth.currentUser?.uid.toString()).set(mapOf("userChosenName" to "Anonymous", "userCreated" to System.currentTimeMillis()))
        .addOnSuccessListener {
          emitter.onComplete()
        }
        .addOnFailureListener {
          emitter.onError(it)
        }
    }
  }

  fun getFriendsList(): Single<List<Friend>> {
    return Single.create { emitter ->
      friendsRef.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val collection = task.result
          if (collection != null) {
            if (!collection.isEmpty) {
              // User has a saved deck
              emitter.onSuccess(collection.toObjects(Friend::class.java))
            } else {
              //User has no friends :'(
              emitter.onError(Exception(context.getString(R.string.no_friends_error)))
            }
          } else {
            // There is no user in the DB
            createUser()
            emitter.onError(Exception(context.getString(R.string.no_user_error)))
          }
        }
      }
    }
  }

  private fun getUserData(uuid: String): Single<Friend> {
    return Single.create { emitter ->
      userRef.document(uuid).get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val user = task.result
          if (user != null) {
            if (user.exists()) {
              val name = user.get("userChosenName")
              if (name != null) {
                emitter.onSuccess(Friend(uuid, name as String, 0))
              }
              else {
                emitter.onError(Throwable(context.getString(R.string.no_name_error)))
              }
            }
            else {
              emitter.onError(Throwable(context.getString(R.string.no_user_error)))
            }
          }
          else {
            emitter.onError(Throwable(context.getString(R.string.no_user_error)))
          }
        }
        else {
          task.exception?.log()
          emitter.onError(Throwable(task.exception!!.message))
        }
      }
    }
  }

  fun addName(name: String): Completable {
    return Completable.create { emitter ->
      userRef.document(auth.currentUser?.uid.toString()).update("userChosenName", name).addOnCompleteListener { task ->
        if (task.isSuccessful) {
          emitter.onComplete()
        } else {
          emitter.onError(Throwable(task.exception!!.message))
        }
      }
    }
  }

  fun addFriend(uuid: String): Single<Friend> {
    return Single.create { emitter ->
      getUserData(uuid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<Friend>() {
        override fun onSuccess(t: Friend) {
          friendsRef.document(uuid).set(t).addOnCompleteListener { task ->
            if (task.isSuccessful) {
              emitter.onSuccess(t)
            } else {
              Logger.e(task.exception!!.localizedMessage?: "Error without message")
              emitter.onError(Throwable(task.exception!!.message))
            }
          }
        }

        override fun onError(e: Throwable) {
          emitter.onError(e)
        }

      }).addTo(compositeDisposable)
    }
  }

  fun dispose() {
    compositeDisposable.dispose()
  }
}