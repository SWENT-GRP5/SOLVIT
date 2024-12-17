package com.android.solvit.shared.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.solvit.BuildConfig
import com.android.solvit.MainActivity
import com.android.solvit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Handles incoming FCM messages and token updates */
open class NotificationService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "NotificationService"
    private const val CHANNEL_ID = "default"
    private const val CHANNEL_NAME = "Default"
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel() // Clean up coroutines when service is destroyed
  }

  /** Creates the default notification channel for the app */
  private fun createNotificationChannel() {
    val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
    channel.description = "Default notification channel"
    channel.enableLights(true)
    channel.lightColor = Color.RED
    channel.enableVibration(true)

    val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)
  }

  private fun getPendingIntent(title: String): PendingIntent? {
    if (BuildConfig.DEBUG) {
      try {
        // Use reflection to access the debug-only NotificationTestHelper
        val helperClass =
            Class.forName("com.android.solvit.shared.notifications.NotificationTestHelper")
        val getTestPendingIntentMethod =
            helperClass.getDeclaredMethod(
                "getTestPendingIntent", Context::class.java, String::class.java)
        val helperInstance = helperClass.getDeclaredField("INSTANCE").get(null)
        val testPendingIntent =
            getTestPendingIntentMethod.invoke(helperInstance, applicationContext, title)
                as? PendingIntent
        if (testPendingIntent != null) {
          return testPendingIntent
        }
      } catch (e: Exception) {
        Log.d(TAG, "Debug helper not found, using default intent", e)
      }
    }

    val intent =
        Intent(applicationContext, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    return PendingIntent.getActivity(
        applicationContext,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  }

  protected open fun showNotification(title: String, message: String) {
    try {
      val builder =
          NotificationCompat.Builder(applicationContext, CHANNEL_ID)
              .setSmallIcon(R.drawable.logosolvit_firstpage)
              .setContentTitle(title)
              .setContentText(message)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)

      val pendingIntent = getPendingIntent(title)
      builder.setContentIntent(pendingIntent)

      with(NotificationManagerCompat.from(applicationContext)) {
        @Suppress("MissingPermission") notify(System.currentTimeMillis().toInt(), builder.build())
      }
    } catch (e: SecurityException) {
      Log.e(TAG, "Security exception when showing notification", e)
    } catch (e: Exception) {
      Log.e(TAG, "Error showing notification", e)
    }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)

    // Handle notification payload
    message.notification?.let { notification ->
      showNotification(notification.title ?: "New Message", notification.body ?: "")
    }

    // Handle data payload
    if (message.data.isNotEmpty()) {
      val title = message.data["title"] ?: "New Message"
      val body = message.data["body"] ?: ""
      if (body.isNotEmpty()) {
        showNotification(title, body)
      }
    }
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)

    // Just log the token receipt - actual update happens when user logs in
    Log.d(TAG, "Received new FCM token")
  }

  /**
   * Updates the FCM token for the current user. This should be called when:
   * 1. A user logs in
   * 2. The app receives a new FCM token while a user is logged in
   */
  fun updateCurrentUserToken(token: String? = null) {
    serviceScope.launch {
      try {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
          val fcmToken = token ?: FirebaseMessaging.getInstance().token.await()
          try {
            FcmTokenManager.getInstance().updateUserFcmToken(currentUser.uid, fcmToken).await()
            Log.d(TAG, "Successfully updated FCM token for user: ${currentUser.uid}")
          } catch (e: Exception) {
            Log.e(TAG, "Failed to update FCM token for user: ${currentUser.uid}", e)
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error updating user token", e)
      }
    }
  }

  suspend fun getToken(): String? {
    return try {
      withContext(Dispatchers.IO) { FirebaseMessaging.getInstance().token.await() }
    } catch (e: Exception) {
      Log.e(TAG, "Error getting FCM token", e)
      null
    }
  }
}
