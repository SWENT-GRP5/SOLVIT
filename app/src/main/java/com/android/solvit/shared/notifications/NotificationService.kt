package com.android.solvit.shared.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
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

  protected open val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

  /** Creates a PendingIntent for the notification */
  protected open fun getPendingIntent(title: String): PendingIntent {
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

  protected open suspend fun showNotification(
      title: String,
      message: String,
      imageUrl: String? = null,
      requestTitle: String? = null
  ) {
    try {
      Log.d(TAG, "Showing notification - Title: $title, Message: $message, ImageUrl: $imageUrl")

      // Create basic notification builder
      val notificationMessage =
          if (!requestTitle.isNullOrEmpty()) {
            "$message: $requestTitle"
          } else {
            message
          }

      val builder =
          NotificationCompat.Builder(applicationContext, CHANNEL_ID)
              .setSmallIcon(R.drawable.logosolvit_firstpage)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)
              .setContentTitle(title)
              .setContentText(notificationMessage)
              .setWhen(System.currentTimeMillis())
              .setShowWhen(true)

      // Add provider picture if URL is provided
      if (!imageUrl.isNullOrEmpty()) {
        Log.d(TAG, "Attempting to load image from URL: $imageUrl")
        CoroutineScope(Dispatchers.Main).launch {
          try {
            // Load a larger image for expanded state
            val request =
                ImageRequest.Builder(applicationContext)
                    .data(imageUrl)
                    .allowHardware(false)
                    .size(256, 256) // Larger size for expanded view
                    .build()

            val result = applicationContext.imageLoader.execute(request) as? SuccessResult
            val drawable = result?.drawable
            if (drawable is BitmapDrawable) {
              Log.d(TAG, "Successfully loaded image, creating notification")
              val bitmap = drawable.bitmap

              // Create a scaled down version for the large icon (collapsed state)
              val smallBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)

              // Use BigPictureStyle with the larger image
              val bigPictureStyle =
                  NotificationCompat.BigPictureStyle()
                      .bigPicture(bitmap)
                      .bigLargeIcon(null as Bitmap?) // Hide the small icon in expanded state
                      .setBigContentTitle(title)
                      .setSummaryText(
                          notificationMessage) // Use summary text for better compatibility

              builder
                  .setLargeIcon(smallBitmap) // For collapsed state
                  .setStyle(bigPictureStyle)

              Log.d(TAG, "Applied big picture style to notification")

              // Show the notification
              val pendingIntent = getPendingIntent(title)
              builder.setContentIntent(pendingIntent)

              with(NotificationManagerCompat.from(applicationContext)) {
                if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                  notify(System.currentTimeMillis().toInt(), builder.build())
                  Log.d(TAG, "Successfully posted notification with image")
                }
              }
            } else {
              Log.e(
                  TAG,
                  "Failed to convert image to bitmap, drawable: ${drawable?.javaClass?.simpleName}")
              showBasicNotification(builder, title, notificationMessage)
            }
          } catch (e: Exception) {
            Log.e(TAG, "Error loading image for notification", e)
            showBasicNotification(builder, title, notificationMessage)
          }
        }
      } else {
        Log.d(TAG, "No image URL provided, showing basic notification")
        showBasicNotification(builder, title, notificationMessage)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error showing notification", e)
    }
  }

  private fun showBasicNotification(
      builder: NotificationCompat.Builder,
      title: String,
      message: String
  ) {
    val pendingIntent = getPendingIntent(title)
    builder.setContentIntent(pendingIntent)

    with(NotificationManagerCompat.from(applicationContext)) {
      if (ContextCompat.checkSelfPermission(
          applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED) {
        notify(System.currentTimeMillis().toInt(), builder.build())
        Log.d(TAG, "Successfully posted basic notification")
      }
    }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Log.d(
        TAG, "Received FCM message - Notification: ${message.notification}, Data: ${message.data}")

    // Handle data payload first as it contains our custom data
    if (message.data.isNotEmpty()) {
      Log.d(TAG, "Processing data payload")
      val imageUrl = message.data["imageUrl"]
      val requestTitle = message.data["requestTitle"]

      // Get notification content from either the data payload or notification payload
      val title = message.data["title"] ?: message.notification?.title ?: "New Message"
      val body = message.data["body"] ?: message.notification?.body ?: ""

      if (body.isNotEmpty()) {
        Log.d(TAG, "Showing notification with imageUrl: $imageUrl")
        serviceScope.launch { showNotification(title, body, imageUrl, requestTitle) }
      }
    }
    // Handle notification-only payload as fallback
    else if (message.notification != null) {
      Log.d(TAG, "Processing notification-only payload")
      serviceScope.launch {
        showNotification(
            message.notification?.title ?: "New Message", message.notification?.body ?: "")
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
