package com.android.solvit.shared.notifications

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Manages FCM token storage and retrieval in Firestore */
class FcmTokenManager private constructor() {
  private val db = FirebaseFirestore.getInstance()

  companion object {
    private const val TAG = "FCM_DEBUG"
    private const val USERS_COLLECTION = "users"
    private const val FCM_TOKENS_COLLECTION = "fcm_tokens"
    private const val CURRENT_TOKEN_DOC = "current"

    @Volatile private var instance: FcmTokenManager? = null

    fun getInstance(): FcmTokenManager {
      return instance ?: synchronized(this) { instance ?: FcmTokenManager().also { instance = it } }
    }
  }

  /** Store or update FCM token for a user */
  suspend fun updateUserFcmToken(userId: String, token: String) {
    try {
      Log.d(TAG, "Storing FCM token for user: $userId")
      db.collection(USERS_COLLECTION)
          .document(userId)
          .collection(FCM_TOKENS_COLLECTION)
          .document(CURRENT_TOKEN_DOC)
          .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
          .await()
      Log.d(TAG, "FCM token stored successfully")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to store FCM token", e)
      throw e
    }
  }

  /** Retrieve the current FCM token for a user */
  suspend fun getUserFcmToken(userId: String): String? {
    return try {
      Log.d(TAG, "Retrieving FCM token for user: $userId")
      val snapshot =
          db.collection(USERS_COLLECTION)
              .document(userId)
              .collection(FCM_TOKENS_COLLECTION)
              .document(CURRENT_TOKEN_DOC)
              .get()
              .await()

      val token = snapshot.getString("token")
      if (token != null) {
        Log.d(TAG, "FCM token retrieved successfully")
      } else {
        Log.w(TAG, "No FCM token found for user: $userId")
      }
      token
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve FCM token", e)
      null
    }
  }
}
