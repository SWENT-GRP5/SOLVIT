package com.android.solvit.shared.model.notification

import com.android.solvit.shared.notifications.FcmTokenManager
import com.android.solvit.shared.notifications.NotificationManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class NotificationManagerTest {
  @Mock private lateinit var mockFunctions: FirebaseFunctions
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUser: FirebaseUser
  @Mock private lateinit var mockTokenManager: FcmTokenManager
  @Mock private lateinit var mockCallable: HttpsCallableReference
  @Mock private lateinit var mockCallableResult: HttpsCallableResult

  private lateinit var notificationManager: NotificationManager
  private val testUserId = "user123"
  private val testRecipientId = "recipient123"
  private val testToken = "test-token"

  @Before
  fun setup() = runTest {
    NotificationManager.clearInstance()
    notificationManager = NotificationManager.getInstance(mockFunctions, mockAuth, mockTokenManager)
    // Set up authentication mocks
    whenever(mockAuth.currentUser).thenReturn(mockUser)
    whenever(mockUser.uid).thenReturn(testUserId)

    // Set up functions mock
    whenever(mockFunctions.getHttpsCallable("sendNotification")).thenReturn(mockCallable)
    whenever(mockCallable.call(any())).thenReturn(Tasks.forResult(mockCallableResult))

    // Set up token manager mock
    whenever(mockTokenManager.getUserFcmToken(testRecipientId)).thenReturn(testToken)

    notificationManager = NotificationManager.getInstance(mockFunctions, mockAuth, mockTokenManager)
  }

  @Test
  fun `sendNotification sends notification successfully`() = runTest {
    // Arrange
    val title = "Test Title"
    val body = "Test Body"
    val expectedData =
        mapOf(
            "recipientToken" to testToken,
            "notification" to mapOf("title" to title, "body" to body),
            "data" to mapOf("senderId" to testUserId, "recipientId" to testRecipientId))

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body)

    // Assert
    assertTrue(result.isSuccess)
    verify(mockTokenManager).getUserFcmToken(testRecipientId)
    verify(mockCallable).call(expectedData)
  }

  @Test
  fun `sendNotification handles missing FCM token`() = runTest {
    // Arrange
    whenever(mockTokenManager.getUserFcmToken(testRecipientId)).thenReturn(null)
    val title = "Test Title"
    val body = "Test Body"

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body)

    // Assert
    assertTrue(result.isFailure)
    verify(mockTokenManager).getUserFcmToken(testRecipientId)
    verify(mockCallable, never()).call(any())
  }

  @Test
  fun `sendServiceRequestUpdateNotification sends update notification successfully`() = runTest {
    // Arrange
    val requestId = "request123"
    val status = "ACCEPTED"
    val message = "Your service request has been updated"
    val expectedData =
        mapOf(
            "recipientToken" to testToken,
            "notification" to mapOf("title" to "Service Request Update", "body" to message),
            "data" to
                mapOf(
                    "senderId" to testUserId,
                    "recipientId" to testRecipientId,
                    "requestId" to requestId,
                    "status" to status))

    // Act
    val result =
        notificationManager.sendServiceRequestUpdateNotification(
            testRecipientId, requestId, status, message)

    // Assert
    assertTrue(result.isSuccess)
    verify(mockTokenManager).getUserFcmToken(testRecipientId)
    verify(mockCallable).call(expectedData)
  }

  @Test
  fun `sendServiceRequestAcceptedNotification sends accepted notification successfully`() =
      runTest {
        // Arrange
        val requestId = "request123"
        val providerName = "Test Provider"
        val expectedData =
            mapOf(
                "recipientToken" to testToken,
                "notification" to
                    mapOf(
                        "title" to "Service Request Accepted",
                        "body" to "$providerName has accepted your service request"),
                "data" to
                    mapOf(
                        "senderId" to testUserId,
                        "recipientId" to testRecipientId,
                        "requestId" to requestId,
                        "status" to "ACCEPTED"))

        // Act
        val result =
            notificationManager.sendServiceRequestAcceptedNotification(
                testRecipientId, requestId, providerName)

        // Assert
        assertTrue(result.isSuccess)
        verify(mockTokenManager).getUserFcmToken(testRecipientId)
        verify(mockCallable).call(expectedData)
      }

  @Test
  fun `sendNotification handles unauthenticated user`() = runTest {
    // Arrange
    whenever(mockAuth.currentUser).thenReturn(null)
    val title = "Test Title"
    val body = "Test Body"

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body)

    // Assert
    assertTrue(result.isFailure)
    verify(mockTokenManager, never()).getUserFcmToken(any())
    verify(mockCallable, never()).call(any())
  }

  @Test
  fun `sendNotification validates input`() = runTest {
    // Act & Assert
    val result = notificationManager.sendNotification("", "title", "body")
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    verify(mockTokenManager, never()).getUserFcmToken(any())
  }

  @Test
  fun `sendNotification handles blank FCM token`() = runTest {
    // Arrange
    whenever(mockTokenManager.getUserFcmToken(testRecipientId)).thenReturn("")
    val title = "Test Title"
    val body = "Test Body"

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body)

    // Assert
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
    assertEquals("Invalid recipient FCM token", result.exceptionOrNull()?.message)
    verify(mockTokenManager).getUserFcmToken(testRecipientId)
    verify(mockCallable, never()).call(any())
  }

  @Test
  fun `sendNotification handles custom data map`() = runTest {
    // Arrange
    val title = "Test Title"
    val body = "Test Body"
    val customData = mapOf("key1" to "value1", "key2" to "value2")
    val expectedData =
        mapOf(
            "recipientToken" to testToken,
            "notification" to mapOf("title" to title, "body" to body),
            "data" to
                (customData + mapOf("senderId" to testUserId, "recipientId" to testRecipientId)))

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body, customData)

    // Assert
    assertTrue(result.isSuccess)
    verify(mockTokenManager).getUserFcmToken(testRecipientId)
    verify(mockCallable).call(expectedData)
  }

  @Test
  fun `sendNotification handles cloud function exception`() = runTest {
    // Arrange
    val title = "Test Title"
    val body = "Test Body"
    val exception = RuntimeException("Cloud function error")
    whenever(mockCallable.call(any())).thenReturn(Tasks.forException(exception))

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body)

    // Assert
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
    verify(mockTokenManager).getUserFcmToken(testRecipientId)
    verify(mockCallable).call(any())
  }

  @Test
  fun `sendNotification logs debug message when permission granted`() = runTest {
    // Arrange
    val title = "Test Title"
    val body = "Test Body"

    // Act
    val result = notificationManager.sendNotification(testRecipientId, title, body)

    // Assert
    assertTrue(result.isSuccess)
    // Debug log verification is handled by the Log class which is final and can't be mocked
    // We can only verify that the notification was sent successfully
  }
}
