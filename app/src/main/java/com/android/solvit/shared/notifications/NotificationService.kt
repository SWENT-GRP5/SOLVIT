package com.android.solvit.shared.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.solvit.MainActivity
import com.android.solvit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Handles incoming FCM messages and token updates */
open class NotificationService(
    private val fcmTokenManager: FcmTokenManager = FcmTokenManager.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : FirebaseMessagingService() {

  private val scope = CoroutineScope(Dispatchers.IO)

  companion object {
    private const val TAG = "FCM_DEBUG"
    const val CHANNEL_ID = "service_requests"
    const val CHANNEL_NAME = "Service Requests"
    const val CHANNEL_DESCRIPTION = "Notifications for service request updates"
    private const val NOTIFICATION_ID = 1

    /** Store current FCM token for authenticated user */
    fun storeCurrentToken() {
      val auth = FirebaseAuth.getInstance()
      val messaging = FirebaseMessaging.getInstance()
      val fcmTokenManager = FcmTokenManager.getInstance()

      auth.currentUser?.uid?.let { userId ->
        CoroutineScope(Dispatchers.IO).launch {
          try {
            val token = messaging.token.await()
            Log.d(TAG, "Retrieved current FCM token: $token")
            fcmTokenManager.updateUserFcmToken(userId, token)
            Log.d(TAG, "Stored current FCM token for user: $userId")
          } catch (e: Exception) {
            Log.e(TAG, "Failed to store current FCM token", e)
          }
        }
      } ?: Log.w(TAG, "No authenticated user to store FCM token for")
    }
  }

  override open fun onCreate() {
    super.onCreate()
    Log.d(TAG, "NotificationService created")
    createNotificationChannel()
  }

  override open fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM token received: $token")

    auth.currentUser?.uid?.let { userId ->
      try {
        fcmTokenManager.updateUserFcmToken(userId, token)
        Log.d(TAG, "FCM token successfully stored for user: $userId")
      } catch (e: Exception) {
        Log.e(TAG, "Error storing FCM token", e)
      }
    } ?: Log.w(TAG, "No authenticated user to store FCM token for")
  }

  override open fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Log.d(TAG, "Message received from: ${message.from}")

    // Handle data message
    if (message.data.isNotEmpty()) {
      Log.d(TAG, "Message data payload: ${message.data}")
      val title = message.data["title"]
      val body = message.data["body"]
      showNotification(title ?: "New Notification", body ?: "You have a new notification")
      return
    }

    // Handle notification message
    message.notification?.let { notification ->
      Log.d(TAG, "Message Notification Title: ${notification.title}")
      Log.d(TAG, "Message Notification Body: ${notification.body}")

      showNotification(
          notification.title ?: "New Notification",
          notification.body ?: "You have a new notification")
    } ?: Log.d(TAG, "Message received without notification payload")
  }

  override open fun getSystemService(name: String): Any? {
    return super.getSystemService(name)
  }

  protected open fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                .apply {
                  description = CHANNEL_DESCRIPTION
                  enableLights(true)
                  lightColor = Color.RED
                  enableVibration(true)
                }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to create notification channel", e)
      }
    }
  }

  private fun showNotification(title: String, message: String) {
    try {
      val builder =
          NotificationCompat.Builder(applicationContext, CHANNEL_ID)
              .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon for tests
              .setContentTitle(title)
              .setContentText(message)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)

      val intent =
          Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          }

      val pendingIntent =
          PendingIntent.getActivity(
              applicationContext,
              0,
              intent,
              PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

      builder.setContentIntent(pendingIntent)

      val notificationManager =
          getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.notify(NOTIFICATION_ID, builder.build())
      Log.d(TAG, "Notification displayed with title: $title and message: $message")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to show notification", e)
      throw e // Rethrow for tests to catch
    }
  }
}
