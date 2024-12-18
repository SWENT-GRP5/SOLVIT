package com.android.solvit.shared.model.notification

import com.android.solvit.shared.notifications.FcmTokenManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FcmTokenManagerTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockUsersCollection: CollectionReference
  @Mock private lateinit var mockUserDoc: DocumentReference
  @Mock private lateinit var mockTokensCollection: CollectionReference
  @Mock private lateinit var mockTokenDoc: DocumentReference
  @Mock private lateinit var mockSnapshot: DocumentSnapshot

  private lateinit var fcmTokenManager: FcmTokenManager
  private val testUserId = "user123"
  private val testToken = "test-token"

  @Before
  fun setup() {
    FcmTokenManager.clearInstance()
    fcmTokenManager = FcmTokenManager.getInstance(mockFirestore)

    // Set up collection/document chain
    whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
    whenever(mockUsersCollection.document(testUserId)).thenReturn(mockUserDoc)
    whenever(mockUserDoc.collection("fcm_tokens")).thenReturn(mockTokensCollection)
    whenever(mockTokensCollection.document("current")).thenReturn(mockTokenDoc)
  }

  @Test
  fun `getUserFcmToken returns token when exists`() = runTest {
    // Arrange
    whenever(mockTokenDoc.get()).thenReturn(Tasks.forResult(mockSnapshot))
    whenever(mockSnapshot.exists()).thenReturn(true)
    whenever(mockSnapshot.getString("token")).thenReturn(testToken)

    // Act
    val result = fcmTokenManager.getUserFcmToken(testUserId)

    // Assert
    assertEquals(testToken, result)
    verify(mockFirestore).collection("users")
    verify(mockUsersCollection).document(testUserId)
    verify(mockUserDoc).collection("fcm_tokens")
    verify(mockTokensCollection).document("current")
    verify(mockTokenDoc).get()
    verify(mockSnapshot).getString("token")
  }

  @Test
  fun `getUserFcmToken returns null when document does not exist`() = runTest {
    // Arrange
    whenever(mockTokenDoc.get()).thenReturn(Tasks.forResult(mockSnapshot))
    whenever(mockSnapshot.exists()).thenReturn(false)

    // Act
    val result = fcmTokenManager.getUserFcmToken(testUserId)

    // Assert
    assertNull(result)
    verify(mockTokenDoc).get()
    verify(mockSnapshot, never()).getString(any())
  }

  @Test
  fun `getUserFcmToken handles error`() = runTest {
    // Arrange
    val exception = Exception("Failed to retrieve FCM token for user: $testUserId")
    whenever(mockTokenDoc.get()).thenReturn(Tasks.forException(exception))

    // Act & Assert
    try {
      fcmTokenManager.getUserFcmToken(testUserId)
      fail("Expected exception was not thrown")
    } catch (e: Exception) {
      assertEquals("Failed to retrieve FCM token for user: $testUserId", e.message)
    }
  }

  @Test
  fun `getUserFcmToken returns null when token field is empty`() = runTest {
    // Arrange
    whenever(mockTokenDoc.get()).thenReturn(Tasks.forResult(mockSnapshot))
    whenever(mockSnapshot.exists()).thenReturn(true)
    whenever(mockSnapshot.getString("token")).thenReturn("")

    // Act
    val result = fcmTokenManager.getUserFcmToken(testUserId)

    // Assert
    assertNull(result)
    verify(mockTokenDoc).get()
    verify(mockSnapshot).getString("token")
  }

  @Test
  fun `getUserFcmToken with blank userId throws IllegalArgumentException`() = runTest {
    try {
      fcmTokenManager.getUserFcmToken("")
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("User ID cannot be empty", e.message)
    }
  }

  @Test
  fun `updateUserFcmToken updates token successfully`() {
    // Arrange
    val setTask = Tasks.forResult<Void>(null)
    whenever(mockTokenDoc.set(any())).thenReturn(setTask)

    // Act
    val resultTask = fcmTokenManager.updateUserFcmToken(testUserId, testToken)

    // Assert
    assertTrue(resultTask.isSuccessful)
    verify(mockFirestore).collection("users")
    verify(mockUsersCollection).document(testUserId)
    verify(mockUserDoc).collection("fcm_tokens")
    verify(mockTokensCollection).document("current")
    verify(mockTokenDoc)
        .set(
            check { data: Map<String, Any> ->
              assertEquals(testToken, data["token"])
              assertTrue(data.containsKey("updatedAt"))
              true
            })
  }

  @Test
  fun `updateUserFcmToken validates input`() {
    // Arrange
    val invalidUserId = ""
    val invalidToken = ""

    // Act & Assert for empty userId
    try {
      fcmTokenManager.updateUserFcmToken(invalidUserId, testToken)
      fail("Expected IllegalArgumentException for empty userId")
    } catch (e: IllegalArgumentException) {
      assertEquals("User ID cannot be empty", e.message)
    }

    // Act & Assert for empty token
    try {
      fcmTokenManager.updateUserFcmToken(testUserId, invalidToken)
      fail("Expected IllegalArgumentException for empty token")
    } catch (e: IllegalArgumentException) {
      assertEquals("FCM token cannot be empty", e.message)
    }

    // Verify that `set` was never called
    verify(mockTokenDoc, never()).set(any())
  }

  @Test
  fun `updateUserFcmToken handles error`() {
    // Arrange
    val exception = Exception("Firestore error")
    whenever(mockTokenDoc.set(any())).thenReturn(Tasks.forException(exception))

    // Act
    val resultTask = fcmTokenManager.updateUserFcmToken(testUserId, testToken)

    // Assert
    assertTrue(resultTask.isComplete)
    assertTrue(resultTask.isCanceled || resultTask.exception == exception)
  }

  @Test
  fun `getInstance returns same instance`() {
    // Act
    val instance1 = FcmTokenManager.getInstance(mockFirestore)
    val instance2 = FcmTokenManager.getInstance(mockFirestore)

    // Assert
    assertSame(instance1, instance2)
  }

  @Test
  fun `getInstance creates new instance after clearInstance`() {
    // Arrange
    val instance1 = FcmTokenManager.getInstance(mockFirestore)

    // Act
    FcmTokenManager.clearInstance()
    val instance2 = FcmTokenManager.getInstance(mockFirestore)

    // Assert
    assertNotSame(instance1, instance2)
  }

  @Test
  fun `getInstance is thread safe`() {
    // Arrange
    FcmTokenManager.clearInstance()
    val threads = List(10) { Thread { FcmTokenManager.getInstance(mockFirestore) } }

    // Act
    threads.forEach { it.start() }
    threads.forEach { it.join() }

    // Assert
    val instances = threads.map { FcmTokenManager.getInstance(mockFirestore) }
    instances.forEach { instance -> assertSame(instances.first(), instance) }
  }

  @Test
  fun `updateUserFcmToken includes timestamp`() {
    // Arrange
    val setTask = Tasks.forResult<Void>(null)
    whenever(mockTokenDoc.set(any())).thenReturn(setTask)
    val beforeTime = System.currentTimeMillis()

    // Act
    fcmTokenManager.updateUserFcmToken(testUserId, testToken)

    // Assert
    verify(mockTokenDoc)
        .set(
            check { data: Map<String, Any> ->
              val timestamp = data["updatedAt"] as Long
              assertTrue(timestamp >= beforeTime)
              assertTrue(timestamp <= System.currentTimeMillis())
              true
            })
  }
}
