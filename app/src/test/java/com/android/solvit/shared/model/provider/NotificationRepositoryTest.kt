package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.NotificationsRepositoryFirestore
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.service.Services
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationsRepositoryTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  private lateinit var notificationsRepository: NotificationsRepositoryFirestore

  private val providerId = "provider123"
  private val notificationId = "notif123"
  private val serviceRequest =
      ServiceRequest(
          "service123",
          "title",
          Services.TUTOR,
          "",
          "",
          "",
          Timestamp.now(),
          Timestamp.now(),
          Location(0.0, 0.0, ""))
  private val provider = Provider("provider123", "john", Services.TUTOR)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    notificationsRepository = NotificationsRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun init_callOnSuccess() {
    var onSuccessCalled = false
    notificationsRepository.init { onSuccessCalled = true }

    Assert.assertTrue(onSuccessCalled)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("test uid")
    MatcherAssert.assertThat(notificationsRepository.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun `sendNotification sends notifications to matching providers`() {
    // Setup mock data for matching providers
    val matchingProvider = listOf(provider)

    // Mock the document ID generation for the new notification
    `when`(mockDocumentReference.id).thenReturn(notificationId)

    // Mock Firestore set operation
    `when`(mockDocumentReference.set(any()))
        .thenReturn(com.google.android.gms.tasks.Tasks.forResult(null))

    // Call the function to send notifications
    notificationsRepository.sendNotification(
        serviceRequest,
        matchingProvider,
        {
          // On success
        },
        { exception ->
          // On failure
          exception.printStackTrace()
          assert(false)
        })

    // Verify that Firestore's set method was called for the matching provider
    verify(mockDocumentReference).set(any())
    // Verify that a new document reference with an ID was generated
    verify(mockCollectionReference).document()
  }

  @Test
  fun `getNotification fetches notifications for the provider`() {
    // Create a mock document snapshot
    val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the methods of the document snapshot
    `when`(mockDocumentSnapshot.getString("providerId")).thenReturn(providerId)
    `when`(mockDocumentSnapshot.getString("title")).thenReturn("Test Title")
    `when`(mockDocumentSnapshot.getString("message")).thenReturn("Test Message")
    `when`(mockDocumentSnapshot.getTimestamp("timestamp")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.getBoolean("isRead")).thenReturn(false)

    // Create a mock query object
    val mockQuery = mock(Query::class.java)

    // Mock the behavior of whereEqualTo to return the mock query
    `when`(mockCollectionReference.whereEqualTo("providerId", providerId)).thenReturn(mockQuery)

    // Create a TaskCompletionSource to simulate a successful task
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()

    // Mock the get() method on the query to return the task from TaskCompletionSource
    `when`(mockQuery.get()).thenReturn(taskCompletionSource.task)

    // Create a mock QuerySnapshot and mock its behavior
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Complete the task with the mockQuerySnapshot
    taskCompletionSource.setResult(mockQuerySnapshot)

    // Call the function to fetch notifications
    notificationsRepository.getNotification(
        providerId,
        onSuccess = { notifications ->
          // Assert that the notification was retrieved and mapped correctly
          assertEquals(1, notifications.size)
          assertEquals("Test Title", notifications[0].title)
          assertEquals("Test Message", notifications[0].message)
        },
        onFailure = { exception ->
          exception.printStackTrace()
          assert(false)
        })

    // Verify the Firestore collection was queried
    verify(mockCollectionReference).whereEqualTo("providerId", providerId)
    verify(mockQuery).get() // This ensures that the get() method was called on the mock query
  }

  @Test
  fun `documentToNotif converts document snapshot to Notification`() {
    val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock the DocumentSnapshot to return specific data
    `when`(mockDocumentSnapshot.id).thenReturn("notif123")
    `when`(mockDocumentSnapshot.getString("providerId")).thenReturn("provider123")
    `when`(mockDocumentSnapshot.getString("title")).thenReturn("New Request")
    `when`(mockDocumentSnapshot.getString("message"))
        .thenReturn("A new service request has been posted.")
    `when`(mockDocumentSnapshot.getTimestamp("timestamp")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.getBoolean("isRead")).thenReturn(false)

    // Mock the serviceRequest map
    val serviceRequestMap =
        mapOf(
            "uid" to "service123",
            "title" to "Service Title",
            "type" to "TUTOR",
            "description" to "Need help with Math",
            "userId" to "user123",
            "providerId" to providerId,
            "dueDate" to Timestamp.now(),
            "meetingDate" to Timestamp.now(),
            "location" to mapOf("latitude" to 10.0, "longitude" to 20.0, "name" to "New York"),
            "status" to "PENDING")

    `when`(mockDocumentSnapshot.get("serviceRequest")).thenReturn(serviceRequestMap)

    // Convert the document snapshot to a notification
    val notification = notificationsRepository.documentToNotif(mockDocumentSnapshot)

    // Assert the notification fields
    assertEquals("notif123", notification?.uid)
    assertEquals("provider123", notification?.providerId)
    assertEquals("New Request", notification?.title)
    assertEquals("A new service request has been posted.", notification?.message)
    assertEquals(false, notification?.isRead)

    // Also assert the serviceRequest fields
    val serviceRequest = notification?.serviceRequest
    assertEquals("service123", serviceRequest?.uid)
    assertEquals("Service Title", serviceRequest?.title)
    assertEquals(Services.TUTOR, serviceRequest?.type)
    assertEquals("Need help with Math", serviceRequest?.description)
  }

  /**
   * Tests that the `updateNotificationReadStatus` function correctly updates the `isRead` field of
   * a notification document in Firestore.
   */
  @Test
  fun `updateNotificationReadStatus updates notification isRead status in Firestore`() {
    // Arrange
    val notificationId = "notif123" // The ID of the notification to be updated
    val isRead = true // The new read status to set

    val mockTaskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.update("isRead", isRead)).thenReturn(mockTaskCompletionSource.task)

    notificationsRepository.updateNotificationReadStatus(notificationId, isRead)
    // Assert that the Firestore update method was called with the correct arguments
    verify(mockDocumentReference).update("isRead", isRead)
  }
}
