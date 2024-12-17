package com.android.solvit.shared.notifications

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Manages FCM token storage and retrieval in Firestore */
class FcmTokenManager
private constructor(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
  companion object {
    private const val TAG = "FCM_DEBUG"
    private const val USERS_COLLECTION = "users"
    private const val FCM_TOKENS_COLLECTION = "fcm_tokens"
    private const val CURRENT_TOKEN_DOC = "current"
    private const val TOKEN_FIELD = "token"
    private const val UPDATED_AT_FIELD = "updatedAt"

    @Volatile private var instance: FcmTokenManager? = null

    fun getInstance(db: FirebaseFirestore = FirebaseFirestore.getInstance()): FcmTokenManager {
      return instance
          ?: synchronized(this) { instance ?: FcmTokenManager(db).also { instance = it } }
    }

    fun clearInstance() {
      instance = null
    }
  }

  /** Store or update FCM token for a user */
  fun updateUserFcmToken(userId: String, token: String): Task<Void> {
    require(userId.isNotBlank()) { "User ID cannot be empty" }
    require(token.isNotBlank()) { "FCM token cannot be empty" }

    Log.d(TAG, "Storing FCM token for user: $userId")
    val tokenData = mapOf(TOKEN_FIELD to token, UPDATED_AT_FIELD to System.currentTimeMillis())

    return db.collection(USERS_COLLECTION)
        .document(userId)
        .collection(FCM_TOKENS_COLLECTION)
        .document(CURRENT_TOKEN_DOC)
        .set(tokenData)
  }

  /** Retrieve the current FCM token for a user */
  suspend fun getUserFcmToken(userId: String): String? {
    try {
      if (userId.isBlank()) {
        Log.e(TAG, "Invalid user ID provided")
        throw IllegalArgumentException("User ID cannot be empty")
      }

      Log.d(TAG, "Retrieving FCM token for user: $userId")
      val snapshot =
          db.collection(USERS_COLLECTION)
              .document(userId)
              .collection(FCM_TOKENS_COLLECTION)
              .document(CURRENT_TOKEN_DOC)
              .get()
              .await()

      return if (snapshot.exists()) {
        val token = snapshot.getString(TOKEN_FIELD)
        if (token.isNullOrBlank()) {
          Log.w(TAG, "Found token document but token field is null or empty for user: $userId")
          null
        } else {
          Log.d(TAG, "Successfully retrieved FCM token for user: $userId")
          token
        }
      } else {
        Log.d(TAG, "No FCM token found for user: $userId")
        null
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve FCM token for user: $userId", e)
      throw e
    }
  }
}
