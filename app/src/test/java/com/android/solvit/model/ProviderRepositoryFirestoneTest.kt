package com.android.solvit.model

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.model.map.Location
import com.android.solvit.model.provider.Provider
import com.android.solvit.model.provider.ProviderRepositoryFirestore
import com.android.solvit.model.provider.Services
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.fail
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProviderRepositoryFirestoneTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var providerRepositoryFirestore: ProviderRepositoryFirestore

  private val provider =
      Provider(
          "test",
          "test",
          Services.PLUMBER,
          "",
          Location(0.0, 0.0, "EPFL"),
          "",
          false,
          0.0,
          0.0,
          Timestamp.now(),
          emptyList())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    providerRepositoryFirestore = ProviderRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("test uid")
    assertThat(providerRepositoryFirestore.getNewUid(), `is`("test uid"))
  }

  @Test
  fun addProvider() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.addProvider(provider, {}, {})
    verify(mockDocumentReference).set(eq(provider))
  }

  @Test
  fun deleteProvider() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.deleteProvider(provider.uid, {}, {})
    verify(mockDocumentReference).delete()
  }

  @Test
  fun updateProvider() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.updateProvider(provider, {}, {})
    verify(mockDocumentReference).set(eq(provider))
  }

  @Test
  fun getProviders() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())
    providerRepositoryFirestore.getProviders(null, {}, { fail("Should not fail") })
    verify(mockCollectionReference).get()
  }
}
