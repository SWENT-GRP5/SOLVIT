package com.android.solvit.shared.model.notification

import com.android.solvit.shared.notifications.FcmTokenManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

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
}