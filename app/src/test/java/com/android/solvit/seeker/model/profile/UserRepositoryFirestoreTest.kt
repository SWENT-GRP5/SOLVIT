package com.android.solvit.seeker.model.profile

import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var firebaseRepository: UserRepositoryFirestore

  private val testSeekerProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = "Chemin des Triaudes")

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
  fun getNewUid() {
    Mockito.`when`(mockDocumentReference.id).thenReturn("12345")
    MatcherAssert.assertThat(firebaseRepository.getNewUid(), CoreMatchers.`is`("12345"))
  }

  @Test
  fun getUserProfile() {
    Mockito.`when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    Mockito.`when`(mockQuerySnapshot.documents).thenReturn(listOf())
    firebaseRepository.getUserProfile({}, { TestCase.fail("Should not fail") })
    verify(mockCollectionReference).get()
  }

  @Test
  fun updateUserProfile() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    firebaseRepository.updateUserProfile(testSeekerProfile, {}, {})
    verify(mockDocumentReference).set(eq(testSeekerProfile))
  }

  @Test
  fun deleteUserProfile() {
    Mockito.`when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
    firebaseRepository.deleteUserProfile(testSeekerProfile.uid, {}, {})
    verify(mockDocumentReference).delete()
  }

  @Test
  fun addUserProfile() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    firebaseRepository.addUserProfile(testSeekerProfile, {}, {})
    verify(mockDocumentReference).set(eq(testSeekerProfile))
  }
}
