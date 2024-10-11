package com.android.solvit.seeker.model.profile

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepositoryImp(private val db: FirebaseFirestore) : FirebaseRepository {
  private val auth = FirebaseAuth.getInstance()
  private val firestore = FirebaseFirestore.getInstance()
  private val collectionPath = "user"

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun getUserProfile(
      onSuccess: (List<UserProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUserProfile")
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
      profile: UserProfile,
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

  override fun getCurrentUserEmail(): String? {
    return auth.currentUser?.email
  }

  override fun getCurrentUserPhoneNumber(): String? {
    return auth.currentUser?.phoneNumber
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

  private fun documentToUser(document: DocumentSnapshot): UserProfile? {
    return try {
      val uid = document.id
      val name = document.getString("name") ?: return null
      val username = document.getString("username") ?: return null
      val email = document.getString("email") ?: return null
      val phone = document.getString("phone") ?: return null
      val address = document.getString("address") ?: return null

      UserProfile(
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
