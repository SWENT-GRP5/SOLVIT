package com.android.solvit.shared.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

/** Manages sending FCM notifications through Firebase Cloud Functions */
class NotificationManager
private constructor(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val fcmTokenManager: FcmTokenManager = FcmTokenManager.getInstance()
) {
  private val gson = Gson()

  companion object {
    private const val TAG = "FCM_DEBUG"
    private const val SEND_NOTIFICATION_FUNCTION = "sendNotification"

    @Volatile private var instance: NotificationManager? = null

    fun getInstance(
        functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
        auth: FirebaseAuth = FirebaseAuth.getInstance(),
        fcmTokenManager: FcmTokenManager = FcmTokenManager.getInstance()
    ): NotificationManager {
      return instance
          ?: synchronized(this) {
            instance ?: NotificationManager(functions, auth, fcmTokenManager).also { instance = it }
          }
    }

    fun clearInstance() {
      instance = null
    }
  }

  /** Send a notification to a specific user */
  suspend fun sendNotification(
      recipientUserId: String,
      title: String,
      body: String,
      data: Map<String, String> = emptyMap()
  ): Result<Unit> {
    return try {
      if (recipientUserId.isBlank()) {
        Log.e(TAG, "Invalid recipient user ID provided")
        return Result.failure(IllegalArgumentException("Recipient user ID cannot be empty"))
      }

      // Get sender's user ID first to fail fast if not authenticated
      val senderId =
          auth.currentUser?.uid
              ?: run {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(IllegalStateException("No authenticated user"))
              }

      Log.d(TAG, "Preparing to send notification from $senderId to: $recipientUserId")

      // Get recipient's FCM token
      val recipientToken =
          fcmTokenManager.getUserFcmToken(recipientUserId)
              ?: run {
                Log.e(TAG, "No FCM token found for user: $recipientUserId")
                return Result.failure(IllegalStateException("Recipient FCM token not found"))
              }

      if (recipientToken.isBlank()) {
        Log.e(TAG, "Empty FCM token found for user: $recipientUserId")
        return Result.failure(IllegalStateException("Invalid recipient FCM token"))
      }

      // Prepare notification data
      val notificationData =
          mapOf(
              "recipientToken" to recipientToken,
              "notification" to mapOf("title" to title, "body" to body),
              "data" to (data + mapOf("senderId" to senderId, "recipientId" to recipientUserId)))

      Log.d(TAG, "Sending notification through Cloud Function")

      // Send notification through Cloud Function
      functions.getHttpsCallable(SEND_NOTIFICATION_FUNCTION).call(notificationData).await()

      Log.d(TAG, "Notification sent successfully to user: $recipientUserId")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send notification to user: $recipientUserId", e)
      Result.failure(e)
    }
  }

  /** Send a notification about a service request update */
  suspend fun sendServiceRequestUpdateNotification(
      recipientUserId: String,
      requestId: String,
      status: String,
      message: String
  ): Result<Unit> {
    return sendNotification(
        recipientUserId,
        "Service Request Update",
        message,
        mapOf("requestId" to requestId, "status" to status))
  }

  /** Send a notification when a service request is accepted */
  suspend fun sendServiceRequestAcceptedNotification(
      recipientUserId: String,
      requestId: String,
      providerName: String
  ): Result<Unit> {
    return sendNotification(
        recipientUserId,
        "Service Request Accepted",
        "$providerName has accepted your service request",
        mapOf("requestId" to requestId, "status" to "ACCEPTED"))
  }
}
