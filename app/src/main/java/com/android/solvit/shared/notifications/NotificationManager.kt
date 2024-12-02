package com.android.solvit.shared.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

/** Manages sending FCM notifications through Firebase Cloud Functions */
class NotificationManager private constructor() {
  private val functions = FirebaseFunctions.getInstance()
  private val gson = Gson()
  private val fcmTokenManager = FcmTokenManager.getInstance()

  companion object {
    private const val TAG = "FCM_DEBUG"
    private const val SEND_NOTIFICATION_FUNCTION = "sendNotification"

    @Volatile private var instance: NotificationManager? = null

    fun getInstance(): NotificationManager {
      return instance
          ?: synchronized(this) { instance ?: NotificationManager().also { instance = it } }
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
      Log.d(TAG, "Preparing to send notification to user: $recipientUserId")

      // Get recipient's FCM token
      val recipientToken = fcmTokenManager.getUserFcmToken(recipientUserId)
      if (recipientToken == null) {
        Log.e(TAG, "No FCM token found for user: $recipientUserId")
        return Result.failure(Exception("Recipient FCM token not found"))
      }

      // Get sender's user ID
      val senderId = FirebaseAuth.getInstance().currentUser?.uid
      if (senderId == null) {
        Log.e(TAG, "No authenticated user found")
        return Result.failure(Exception("No authenticated user"))
      }

      // Prepare notification data
      val notificationData =
          mapOf(
              "recipientToken" to recipientToken,
              "notification" to mapOf("title" to title, "body" to body),
              "data" to
                  data.plus(
                      mapOf(
                          "senderId" to senderId,
                          "timestamp" to System.currentTimeMillis().toString())))

      Log.d(TAG, "Calling Cloud Function with data: ${gson.toJson(notificationData)}")

      // Call Firebase Cloud Function
      functions.getHttpsCallable(SEND_NOTIFICATION_FUNCTION).call(notificationData).await()

      Log.d(TAG, "Notification sent successfully")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send notification", e)
      Result.failure(e)
    }
  }

  /** Send a service request update notification */
  suspend fun sendServiceRequestUpdateNotification(
      recipientUserId: String,
      requestId: String,
      status: String,
      message: String
  ): Result<Unit> {
    val title = "Service Request Update"
    val data = mapOf("requestId" to requestId, "status" to status, "type" to "request_update")
    return sendNotification(recipientUserId, title, message, data)
  }

  /** Send a service request accepted notification */
  suspend fun sendServiceRequestAcceptedNotification(
      recipientUserId: String,
      requestId: String,
      providerName: String
  ): Result<Unit> {
    val title = "Service Request Accepted"
    val message = "$providerName has accepted your service request"
    val data =
        mapOf(
            "requestId" to requestId, "providerName" to providerName, "type" to "request_accepted")
    return sendNotification(recipientUserId, title, message, data)
  }
}
