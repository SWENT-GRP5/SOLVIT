package com.android.solvit.seeker.model.profile

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

open class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  private val collectionPath = "user"

  override fun init(onSuccess: () -> Unit) {
    FirebaseAuth.getInstance().addAuthStateListener { onSuccess() }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun addUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  override fun getUserProfile(
      uid: String,
      onSuccess: (SeekerProfile) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUsersProfile")
    db.collection(collectionPath).document(uid).get().addOnCompleteListener { document ->
      if (document.isSuccessful) {

        val user = documentToUser(document.result)
        if (user != null) {
          onSuccess(user)
        } else {
          Log.e("RepositoryFirestore", "Error getting user")
        }
      } else {
        document.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun getUsersProfile(
      onSuccess: (List<SeekerProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUsersProfile")
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val user = task.result?.mapNotNull { document -> documentToUser(document) } ?: emptyList()
        onSuccess(user)
      } else {
        task.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun updateUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  override fun deleteUserProfile(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  fun documentToUser(document: DocumentSnapshot): SeekerProfile? {
    return try {
      val uid = document.id
      val name = document.getString("name") ?: return null
      val username = document.getString("username") ?: return null
      val email = document.getString("email") ?: return null
      val phone = document.getString("phone") ?: return null
      val address = document.getString("address") ?: return null

      SeekerProfile(
          uid = uid,
          name = name,
          username = username,
          email = email,
          phone = phone,
          address = address)
    } catch (e: Exception) {
      Log.e("RepositoryFirestore", "Error converting document to UserProfile", e)
      null
    }
  }
}
