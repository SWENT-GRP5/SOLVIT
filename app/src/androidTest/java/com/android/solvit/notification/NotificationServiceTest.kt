package com.android.solvit.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.android.solvit.TestActivity
import com.android.solvit.shared.notifications.NotificationService
import com.google.firebase.messaging.RemoteMessage
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "NotificationTest"
private const val NOTIFICATION_TIMEOUT = 5000L // 5 seconds timeout
private const val UI_TIMEOUT = 1000L // 1 second timeout for UI operations

@RunWith(AndroidJUnit4::class)
class NotificationServiceTest {
  private lateinit var context: Context
  private lateinit var notificationManager: NotificationManager
  private lateinit var device: UiDevice
  private lateinit var service: TestNotificationService

  @get:Rule val serviceRule = ServiceTestRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        GrantPermissionRule.grant()
      }

  // Test implementation of NotificationService that uses TestActivity
  private class TestNotificationService : NotificationService() {
    private var testContext: Context? = null

    fun initialize(context: Context) {
      testContext = context
      attachBaseContext(context)
      onCreate()
    }

    override fun getSystemService(name: String): Any? {
      return testContext?.getSystemService(name) ?: super.getSystemService(name)
    }

    override fun onMessageReceived(message: RemoteMessage) {
      Log.d(TAG, "Received message with data: ${message.data}")
      super.onMessageReceived(message)
    }

    override fun showNotification(title: String, message: String) {
      Log.d(TAG, "Showing notification - Title: $title, Message: $message")
      try {
        val intent =
            Intent(testContext, TestActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
              putExtra("notification", true)
              putExtra("title", title)
              putExtra("message", message)
            }

        val pendingIntent =
            PendingIntent.getActivity(
                testContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =
            NotificationCompat.Builder(testContext!!, "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationManager =
            testContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
        Log.d(TAG, "Successfully showed notification")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to show notification", e)
        throw e
      }
    }
  }

  private fun clearSystemState() {
    try {
      // Clear notifications
      notificationManager.cancelAll()

      // Clear notification channels if API 26+
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.notificationChannels.forEach { channel ->
          notificationManager.deleteNotificationChannel(channel.id)
        }
      }

      // Reset UI state
      device.pressHome()
      device.openNotification()
      device.wait(Until.gone(By.textContains("Notification")), UI_TIMEOUT)
      device.pressHome()

      Log.d(TAG, "Successfully cleared system state")
    } catch (e: Exception) {
      Log.e(TAG, "Error clearing system state", e)
    }
  }

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    // Log notification permission status
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val permissionStatus =
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
      val statusString =
          if (permissionStatus == PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"
      Log.d(TAG, "Notification permission status: $statusString")

      // Verify permission is granted
      assertTrue(
          "Notification permission must be granted for tests",
          permissionStatus == PackageManager.PERMISSION_GRANTED)
    }

    // Clear system state before each test
    clearSystemState()

    // Create and initialize service
    Log.d(TAG, "Initializing test service")
    service = TestNotificationService()
    service.initialize(context)
  }

  @After
  fun tearDown() {
    clearSystemState()
  }

  @Test
  fun notificationChannel_isCreatedWithCorrectSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Log.d(TAG, "Checking notification channel settings")
      val channel = notificationManager.getNotificationChannel("default")

      assertNotNull("Notification channel should be created", channel)
      assertEquals("Channel name should match", "Default", channel?.name)
      assertEquals(
          "Channel description should match", "Default notification channel", channel?.description)
      assertEquals(
          "Channel importance should be high",
          NotificationManager.IMPORTANCE_HIGH,
          channel?.importance)
      assertTrue("Channel should have lights enabled", channel?.shouldShowLights() ?: false)
      assertTrue("Channel should have vibration enabled", channel?.shouldVibrate() ?: false)
    }
  }

  @Test
  fun onMessageReceived_withDataPayload_showsNotification() {
    try {
      // Create a data message
      val data = mapOf("title" to "Test Title", "body" to "Test Body")
      val message = RemoteMessage.Builder("test@test.com").setData(data).build()

      Log.d(TAG, "Sending test message")
      service.onMessageReceived(message)

      // Wait for notification drawer to be fully expanded
      Log.d(TAG, "Opening notification drawer")
      device.openNotification()

      // Verify notification is shown
      Log.d(TAG, "Looking for notification title")
      val title = device.wait(Until.findObject(By.text("Test Title")), NOTIFICATION_TIMEOUT)
      Log.d(TAG, "Looking for notification body")
      val body = device.wait(Until.findObject(By.text("Test Body")), NOTIFICATION_TIMEOUT)

      assertNotNull("Notification title should be visible", title)
      assertNotNull("Notification body should be visible", body)
    } catch (e: Exception) {
      Log.e(TAG, "Test failed", e)
      throw e
    }
  }

  @Test
  fun onMessageReceived_withEmptyPayload_doesNotShowNotification() {
    Log.d(TAG, "Testing empty payload")
    // Create an empty message
    val message = RemoteMessage.Builder("test@test.com").build()

    // Send message
    service.onMessageReceived(message)

    // Verify no notification is shown
    Log.d(TAG, "Opening notification drawer")
    device.openNotification()

    val notification =
        device.wait(Until.findObject(By.textContains("Notification")), NOTIFICATION_TIMEOUT)

    assertNull("No notification should be shown", notification)
  }

  @Test
  fun notification_whenClicked_launchesTestActivity() {
    try {
      // Create and send a test message
      val data = mapOf("title" to "Test Title", "body" to "Test Body")
      val message = RemoteMessage.Builder("test@test.com").setData(data).build()

      Log.d(TAG, "Sending test message")
      service.onMessageReceived(message)

      // Open and click the notification
      Log.d(TAG, "Opening notification drawer")
      device.openNotification()

      Log.d(TAG, "Looking for notification to click")
      val notification = device.wait(Until.findObject(By.text("Test Title")), NOTIFICATION_TIMEOUT)
      assertNotNull("Notification should be visible", notification)

      // Get the package name before clicking
      val packageName = context.packageName
      Log.d(TAG, "Package name: $packageName")

      // Click the notification
      Log.d(TAG, "Clicking notification")
      notification?.click()

      // Wait for app to launch and verify
      Log.d(TAG, "Waiting for app to launch")
      val appLaunched = device.wait(Until.hasObject(By.pkg(packageName)), NOTIFICATION_TIMEOUT)
      assertTrue("App should be launched", appLaunched)

      // Verify we're on the test activity
      val testText = device.wait(Until.findObject(By.text("Test Activity")), NOTIFICATION_TIMEOUT)
      assertNotNull("Test activity should be visible", testText)
    } catch (e: Exception) {
      Log.e(TAG, "Test failed", e)
      throw e
    }
  }
}
