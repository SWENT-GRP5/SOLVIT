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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles incoming FCM messages and token updates */
class NotificationService : FirebaseMessagingService() {

  private val scope = CoroutineScope(Dispatchers.IO)
  private val fcmTokenManager = FcmTokenManager.getInstance()

  companion object {
    private const val TAG = "FCM_DEBUG"
    const val CHANNEL_ID = "service_requests"
    const val CHANNEL_NAME = "Service Requests"
    const val CHANNEL_DESCRIPTION = "Notifications for service request updates"
    private const val NOTIFICATION_ID = 1
  }

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "NotificationService created")
    createNotificationChannel()
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM token received: $token")

    FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
      scope.launch {
        try {
          fcmTokenManager.updateUserFcmToken(userId, token)
          Log.d(TAG, "FCM token successfully stored for user: $userId")
        } catch (e: Exception) {
          Log.e(TAG, "Error storing FCM token", e)
        }
      }
    } ?: Log.w(TAG, "No user logged in when receiving new token")
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Log.d(TAG, "Message received - Data: ${message.data}")

    val title = message.notification?.title
    val body = message.notification?.body
    val requestId = message.data["requestId"]

    if (title == null || body == null) {
      Log.w(TAG, "Received message without title or body")
      return
    }

    showNotification(title, body, requestId)
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
          NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
          }

      val notificationManager =
          getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
      Log.d(TAG, "Notification channel created")
    }
  }

  private fun showNotification(title: String, body: String, requestId: String?) {
    val intent =
        Intent(this, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          requestId?.let { putExtra("requestId", it) }
        }

    val pendingIntent =
        PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(NOTIFICATION_ID, notification)
    Log.d(TAG, "Notification displayed: $title")
  }
}
