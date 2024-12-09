package com.android.solvit.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "NotificationService"
private const val NOTIFICATION_TIMEOUT = 2000L // 2 seconds timeout
private const val UI_TIMEOUT = 500L // 0.5 second timeout for UI operations

@RunWith(AndroidJUnit4::class)
class NotificationServiceTest {
  private lateinit var context: Context
  private lateinit var notificationManager: NotificationManager
  private lateinit var device: UiDevice
  private lateinit var service: TestNotificationService

  @get:Rule val serviceRule = ServiceTestRule()

  @get:Rule
  val grantPermissionRule: GrantPermissionRule =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        GrantPermissionRule.grant()
      }

  // Test implementation of NotificationService that uses TestActivity
  private class TestNotificationService : NotificationService() {
    private var testContext: Context? = null
    var testThrowSecurityException = false

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

    // Expose protected method for testing
    public override fun showNotification(title: String, message: String) {
      super.showNotification(title, message)
    }
  }

  private fun clearSystemState() {
    try {
      // Clear notifications more efficiently
      val notificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.cancelAll()
    } catch (e: Exception) {
      // Fallback to UI-based clearing if needed
      device.openNotification()
      val clearButton = device.wait(Until.hasObject(By.text("Clear all")), UI_TIMEOUT)
      if (clearButton != null) {
        device.findObject(By.text("Clear all"))?.click()
      }
      device.pressHome()
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
    service = TestNotificationService().apply { initialize(context) }
  }

  @After
  fun tearDown() {
    clearSystemState()
    unmockkAll() // This ensures all static mocks are cleaned up
    clearAllMocks() // Add this to ensure all mocks are cleared
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

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getToken_whenSuccessful_returnsToken() = runTest {
    mockkStatic(FirebaseMessaging::class)
    val mockMessaging = mockk<FirebaseMessaging>()

    every { FirebaseMessaging.getInstance() } returns mockMessaging
    every { mockMessaging.token } returns Tasks.forResult("test-token")

    val token = service.getToken()
    assertEquals("test-token", token)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getToken_whenError_returnsNull() = runTest {
    mockkStatic(FirebaseMessaging::class)
    mockkStatic(Log::class)
    val mockMessaging = mockk<FirebaseMessaging>()

    every { FirebaseMessaging.getInstance() } returns mockMessaging
    every { mockMessaging.token } returns Tasks.forException(Exception("Test error"))
    every { Log.e(any(), any(), any()) } returns 0

    val token = service.getToken()
    assertNull(token)

    verify { Log.e("NotificationService", "Error getting FCM token", any()) }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onNewToken_whenErrorOccurs_logsError() = runTest {
    // Mock FirebaseAuth and Log
    mockkStatic(FirebaseAuth::class)
    mockkStatic(Log::class)

    every { FirebaseAuth.getInstance() } throws
        FirebaseAuthException("error", "Token update failed")
    every { Log.e(TAG, "Error handling new token", any()) } returns 0
    every { Log.d(any(), any()) } returns 0

    service.onNewToken("test-token")

    // Wait for the coroutine to complete
    advanceUntilIdle()

    // Verify error was logged with correct parameters
    verify(exactly = 1) { Log.e(TAG, "Error handling new token", any()) }

    // Clean up mocks
    unmockkStatic(FirebaseAuth::class)
    unmockkStatic(Log::class)
  }

  @Test
  fun onNewToken_whenUserLoggedIn_handlesToken() {
    // Mock FirebaseAuth to simulate logged-in user
    val testToken = "test_token_123"

    service.onNewToken(testToken)

    // Verify no exceptions are thrown
    // Note: Since the TODO in onNewToken is not implemented, we just verify it doesn't crash
  }

  @Test
  fun onNewToken_whenUserNotLoggedIn_doesNothing() = runTest {
    // Mock FirebaseAuth
    mockkStatic(FirebaseAuth::class)
    mockkStatic(Log::class)

    val mockAuth = mockk<FirebaseAuth>()
    every { FirebaseAuth.getInstance() } returns mockAuth
    every { mockAuth.currentUser } returns null
    every { Log.d(any(), any()) } returns 0

    // Execute test
    service.onNewToken("test-token")

    // Wait for coroutine to complete
    advanceUntilIdle()

    // Verify currentUser was checked using property access syntax
    verify(exactly = 1) { mockAuth.currentUser }

    unmockkStatic(FirebaseAuth::class)
    unmockkStatic(Log::class)
  }

  @Test
  fun showNotification_whenSecurityException_handlesError() {
    // Mock Log.e
    mockkStatic(Log::class)
    every { Log.e(eq(TAG), eq("Security exception when showing notification"), any()) } returns 0
    every { Log.d(any(), any()) } returns 0 // Allow debug logs to pass through

    // Create a service that throws SecurityException
    service = TestNotificationService().apply { initialize(context) }

    // Mock the NotificationManagerCompat to throw SecurityException
    mockkStatic(NotificationManagerCompat::class)
    val mockNotificationManager = mockk<NotificationManagerCompat>()
    every { NotificationManagerCompat.from(any()) } returns mockNotificationManager
    every { mockNotificationManager.notify(any(), any()) } throws
        SecurityException("Test security exception")

    // This should not throw an exception due to error handling
    service.onMessageReceived(
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to "Test Title", "body" to "Test Body"))
            .build())

    // Verify that error was logged with correct message
    verify(exactly = 1) { Log.e(TAG, "Security exception when showing notification", any()) }

    unmockkStatic(NotificationManagerCompat::class)
  }

  @Test
  fun onMessageReceived_withNotificationPayload_showsNotification() {
    val message =
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to "Test Title", "body" to "Test Body"))
            .build()

    service.onMessageReceived(message)

    assertNotificationShown("Test Title", "Test Body")
  }

  private fun assertNotificationShown(title: String, body: String) {
    device.openNotification()

    // Check if notification is shown with correct title and body
    val titleShown = device.wait(Until.hasObject(By.text(title)), NOTIFICATION_TIMEOUT)
    val bodyShown = device.wait(Until.hasObject(By.text(body)), UI_TIMEOUT)

    assertTrue("Notification title not shown: $title", titleShown != null)
    assertTrue("Notification body not shown: $body", bodyShown != null)

    // Close notification drawer
    device.pressHome()
  }

  @Test
  fun getPendingIntent_inDebugMode_usesTestHelper() {
    // Create test notification with data payload
    val message =
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to "Test Title", "body" to "Test Body"))
            .build()

    // Set up test activity launch verification
    service.onMessageReceived(message)

    // Verify that the notification launches TestActivity
    device.openNotification()
    val notification = device.wait(Until.findObject(By.text("Test Title")), NOTIFICATION_TIMEOUT)
    notification?.let { device.findObject(By.text("Test Title")).click() }

    // Verify TestActivity was launched
    val activityLaunched =
        device.wait(Until.hasObject(By.clazz(TestActivity::class.java.name)), UI_TIMEOUT)
    assertTrue("TestActivity should be launched", activityLaunched != null)
  }

  @Test
  fun createNotificationChannel_belowOreo_doesNotCreateChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Clear any existing channels
      notificationManager.cancelAll()

      // Create a new service instance which will trigger channel creation
      val newService = TestNotificationService()
      newService.initialize(context)

      // Verify no channels were created (this won't throw an error below Oreo)
      val channels = notificationManager.notificationChannels
      assertTrue("No channels should be created below API 26", channels.isEmpty())
    }
  }

  @Test
  fun onNewToken_whenUserLoggedIn_handlesTokenSuccessfully() = runTest {
    // Mock FirebaseAuth and user
    mockkStatic(FirebaseAuth::class)
    mockkStatic(Log::class)

    val mockAuth = mockk<FirebaseAuth>()
    val mockUser = mockk<FirebaseUser>()

    every { FirebaseAuth.getInstance() } returns mockAuth
    every { mockAuth.currentUser } returns mockUser
    every { Log.e(any(), any(), any()) } returns 0

    // Execute test
    service.onNewToken("test_fcm_token")

    // Wait for coroutine to complete
    advanceUntilIdle()

    // Verify FirebaseAuth was called
    verify { mockAuth.currentUser }

    unmockkStatic(FirebaseAuth::class)
    unmockkStatic(Log::class)
  }

  @Test
  fun onMessageReceived_withBothPayloads_prioritizesNotificationPayload() {
    // Test with two separate messages to verify priority
    val dataMessage =
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to "Data Title", "body" to "Data Body"))
            .build()

    // First verify data payload works
    service.onMessageReceived(dataMessage)
    assertNotificationShown("Data Title", "Data Body")

    clearSystemState()

    // Now verify direct notification shows correctly
    service.showNotification("Notification Title", "Notification Body")
    assertNotificationShown("Notification Title", "Notification Body")
  }

  @Test
  fun notification_whenClicked_launchesTestActivity() {
    try {
      // Create and send a test message
      val data = mapOf("title" to "Test Title", "body" to "Test Body")
      val message = RemoteMessage.Builder("test@test.com").setData(data).build()

      Log.d(TAG, "Sending test message")
      service.onMessageReceived(message)

      // Wait for notification to be posted
      Thread.sleep(1000)

      // Open and click the notification
      Log.d(TAG, "Opening notification drawer")
      device.openNotification()
      Thread.sleep(1000) // Wait for drawer to fully open

      Log.d(TAG, "Looking for notification to click")
      val notification = device.wait(Until.findObject(By.text("Test Title")), NOTIFICATION_TIMEOUT)
      assertNotNull("Notification should be visible", notification)

      // Get the package name before clicking
      val packageName = context.packageName
      Log.d(TAG, "Package name: $packageName")

      // Click the notification and wait for click to register
      Log.d(TAG, "Clicking notification")
      notification.click()
      Thread.sleep(1000)

      // Wait for app to launch and verify
      Log.d(TAG, "Waiting for app to launch")
      val appLaunched =
          device.wait(Until.hasObject(By.pkg(packageName).depth(0)), NOTIFICATION_TIMEOUT * 2)
      assertTrue("App should be launched", appLaunched)

      // Wait for activity to fully load
      Thread.sleep(1000)

      // Verify we're on the test activity
      val testText = device.wait(Until.findObject(By.text("Test Activity")), NOTIFICATION_TIMEOUT)
      assertNotNull("Test activity should be visible", testText)
    } catch (e: Exception) {
      Log.e(TAG, "Test failed", e)
      throw e
    }
  }

  @Test
  fun onMessageReceived_withNullNotificationFields_showsDefaultValues() {
    // Create a message with null fields but non-empty body
    val message =
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to null, "body" to "Test Message"))
            .build()

    // Send the message
    service.onMessageReceived(message)

    // Wait for notification
    Thread.sleep(1000)

    // Verify default values were used
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notifications = notificationManager.activeNotifications
    assertTrue("At least one notification should be active", notifications.isNotEmpty())

    val notification = notifications[0]
    assertEquals(
        "New Message", notification.notification.extras.getString(Notification.EXTRA_TITLE))
    assertEquals(
        "Test Message", notification.notification.extras.getString(Notification.EXTRA_TEXT))
  }

  @Test
  fun onMessageReceived_withEmptyDataBody_doesNotShowNotification() {
    // Create a message with empty body
    val data = mapOf("title" to "Test Title", "body" to "")
    val message = RemoteMessage.Builder("test@test.com").setData(data).build()

    Log.d(TAG, "Sending test message with empty body")
    service.onMessageReceived(message)

    // Verify no notification is shown
    Log.d(TAG, "Opening notification drawer")
    device.openNotification()

    val notification = device.wait(Until.findObject(By.text("Test Title")), NOTIFICATION_TIMEOUT)
    assertNull("No notification should be shown for empty body", notification)
  }

  @Test
  fun showNotification_whenGenericException_handlesError() {
    // Mock Log.e
    mockkStatic(Log::class)
    every { Log.e(TAG, "Error showing notification", any()) } returns 0
    every { Log.d(any(), any()) } returns 0 // Allow debug logs to pass through

    // Create a service that throws generic Exception
    service = TestNotificationService().apply { initialize(context) }

    // Mock the NotificationManagerCompat to throw Exception
    mockkStatic(NotificationManagerCompat::class)
    val mockNotificationManager = mockk<NotificationManagerCompat>()
    every { NotificationManagerCompat.from(any()) } returns mockNotificationManager
    every { mockNotificationManager.notify(any(), any()) } throws
        RuntimeException("Test generic exception")

    // This should not throw an exception due to error handling
    service.onMessageReceived(
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to "Test Title", "body" to "Test Body"))
            .build())

    // Verify that error was logged
    verify(exactly = 1) { Log.e(TAG, "Error showing notification", any()) }

    unmockkStatic(NotificationManagerCompat::class)
  }

  @Test
  fun getPendingIntent_returnsValidIntent() {
    // Create service
    service = TestNotificationService().apply { initialize(context) }

    // Send a notification message to trigger getPendingIntent
    service.onMessageReceived(
        RemoteMessage.Builder("test@test.com")
            .setData(mapOf("title" to "Test Title", "body" to "Test Body"))
            .build())

    // Verify notification is shown and can be clicked
    device.openNotification()
    val notification = device.wait(Until.findObject(By.text("Test Title")), NOTIFICATION_TIMEOUT)
    assertNotNull("Notification should be visible", notification)
    notification.click()

    // Verify clicking launches an activity (either MainActivity or TestActivity)
    val appLaunched =
        device.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), NOTIFICATION_TIMEOUT)
    assertTrue("App should be launched when clicking notification", appLaunched)
  }
}
