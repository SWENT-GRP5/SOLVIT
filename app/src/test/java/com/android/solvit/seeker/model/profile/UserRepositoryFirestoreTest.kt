package com.android.solvit.seeker.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @Mock
  private lateinit var mockFirestore: FirebaseFirestore
  @Mock
  private lateinit var mockDocumentReference: DocumentReference
  @Mock
  private lateinit var mockCollectionReference: CollectionReference
  @Mock
  private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock
  private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var firebaseRepository: UserRepositoryFirestore

  private val testSeekerProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = "Chemin des Triaudes"
      )

  @Before
  fun setUp() {
      MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    firebaseRepository = UserRepositoryFirestore(mockFirestore)

    Mockito.`when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    Mockito.`when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    Mockito.`when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid_returnsDocumentId() {
    Mockito.`when`(mockDocumentReference.id).thenReturn("12345")
    val newUid = firebaseRepository.getNewUid()
    assert(newUid == "12345")
  }

  @Test
  fun getUserProfile_callsFirestoreCollection() {

    Mockito.`when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    Mockito.`when`(mockQuerySnapshot.documents).thenReturn(listOf())

    firebaseRepository.getUserProfile(
        onSuccess = {
          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { TestCase.fail("Failure callback should not be called") })

      verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun updateUserProfile_callsFirestoreSet() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    firebaseRepository.updateUserProfile(
        testSeekerProfile,
        onSuccess = { /* Do nothing; success is expected */},
        onFailure = { TestCase.fail("Failure callback should not be called") })

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteUserProfile_callsFirestoreDelete() {

    Mockito.`when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    firebaseRepository.deleteUserProfile(
        "12345",
        onSuccess = { /* Do nothing; success is expected */},
        onFailure = { TestCase.fail("Failure callback should not be called") })

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }
}