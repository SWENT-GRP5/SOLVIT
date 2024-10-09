package com.android.solvit.model.requests

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.model.map.Location
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ServiceRequestRepositoryFirebaseTest {

  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockStorage: FirebaseStorage
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockToDoQuerySnapshot: QuerySnapshot

  private lateinit var serviceRequestRepositoryFirebase: ServiceRequestRepositoryFirebase

  private val serviceRequest =
      ServiceRequest(
          uid = "1",
          title = "Test Request",
          description = "Test Description",
          assigneeName = "Test Assignee",
          dueDate = Timestamp.now(),
          location = Location(name = "EPFL", latitude = 0.0, longitude = 0.0),
          imageUrl = null,
          type = ServiceRequestType.CLEANING,
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    serviceRequestRepositoryFirebase = ServiceRequestRepositoryFirebase(mockFirestore, mockStorage)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun init_doesNotCallOnSuccessWhenUserIsNotAuthenticated() {
    `when`(mockAuth.currentUser).thenReturn(null)

    var onSuccessCalled = false
    serviceRequestRepositoryFirebase.init { onSuccessCalled = true }

    assertFalse(onSuccessCalled)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = serviceRequestRepositoryFirebase.getNewUid()
    assert(uid == "1")
  }

  @Test
  fun getServiceRequests_callsDocuments() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockToDoQuerySnapshot))

    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf())

    serviceRequestRepositoryFirebase.getServiceRequests(
        onSuccess = {
          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { TestCase.fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    org.mockito.kotlin.verify(org.mockito.kotlin.timeout(100)) { (mockToDoQuerySnapshot).documents }
  }

  @Test
  fun saveRequest_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(org.mockito.kotlin.any())).thenReturn(Tasks.forResult(null))

    serviceRequestRepositoryFirebase.saveServiceRequest(
        serviceRequest, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "ToDos" collection
    org.mockito.kotlin.verify(mockDocumentReference).set(org.mockito.kotlin.any())
  }

  @Test
  fun deleteServiceRequestById_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    serviceRequestRepositoryFirebase.deleteServiceRequestById("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    org.mockito.kotlin.verify(mockDocumentReference).delete()
  }

  @Test
  fun documentToServiceRequest_validDocumentSnapshot_returnsServiceRequest() {
    // Set up mock DocumentSnapshot
    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Request")
    `when`(mockDocumentSnapshot.getString("description")).thenReturn("Test Description")
    `when`(mockDocumentSnapshot.getString("assigneeName")).thenReturn("Test Assignee")
    `when`(mockDocumentSnapshot.getTimestamp("dueDate")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.getDouble("location.latitude")).thenReturn(0.0)
    `when`(mockDocumentSnapshot.getDouble("location.longitude")).thenReturn(0.0)
    `when`(mockDocumentSnapshot.getString("location.name")).thenReturn("EPFL")
    `when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("type")).thenReturn("CLEANING")
    `when`(mockDocumentSnapshot.getString("status")).thenReturn("PENDING")

    val result = serviceRequestRepositoryFirebase.documentToServiceRequest(mockDocumentSnapshot)

    // Check that a valid ServiceRequest object is returned
    assertNotNull(result)
    assert(result?.uid == "1")
    assert(result?.title == "Test Request")
    assert(result?.description == "Test Description")
    assert(result?.location?.name == "EPFL")
    assert(result?.type == ServiceRequestType.CLEANING)
    assert(result?.status == ServiceRequestStatus.PENDING)
  }

  @Test
  fun documentToServiceRequest_invalidDocumentSnapshot_returnsNull() {
    // Set up mock DocumentSnapshot with missing required fields
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(null)

    val result = serviceRequestRepositoryFirebase.documentToServiceRequest(mockDocumentSnapshot)

    // Check that null is returned due to missing required fields
    assertNull(result)
  }
}
