package com.android.solvit.shared.model.notification

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.notifications.FcmTokenManager
import com.android.solvit.shared.notifications.NotificationService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], application = Application::class)
class NotificationServiceTest {
  @Mock private lateinit var mockFcmTokenManager: FcmTokenManager
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUser: FirebaseUser

  private lateinit var notificationService: TestNotificationService
  private lateinit var notificationManager: NotificationManager
  private val testDispatcher = TestCoroutineDispatcher()
  private lateinit var context: Context

  private inner class TestNotificationService : NotificationService(mockFcmTokenManager, mockAuth) {
    override fun getSystemService(name: String): Any? {
      return when (name) {
        Context.NOTIFICATION_SERVICE -> notificationManager
        else -> context.getSystemService(name)
      }
    }

    override fun getApplicationContext(): Context = context

    override fun getBaseContext(): Context = context

    override fun getPackageName(): String = "com.android.solvit"

    override fun getResources() = context.resources
  }

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    // Get application context
    context = ApplicationProvider.getApplicationContext()

    // Set up authentication mocks
    whenever(mockAuth.currentUser).thenReturn(mockUser)
    whenever(mockUser.uid).thenReturn("test-user")

    // Set up token manager mock
    whenever(mockFcmTokenManager.updateUserFcmToken(any(), any())).thenReturn(Tasks.forResult(null))

    // Set up notification manager
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create and initialize notification service
    notificationService = TestNotificationService()
    notificationService.onCreate()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
    notificationManager.cancelAll()
  }

  // Tests will be added back once the configuration issues are resolved
}
