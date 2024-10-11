package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.map.Location
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
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
class ProviderRepositoryFirestoneTest {

  @Mock
  private lateinit var mockFirestore: FirebaseFirestore
  @Mock
  private lateinit var mockDocumentReference: DocumentReference
  @Mock
  private lateinit var mockCollectionReference: CollectionReference
  @Mock
  private lateinit var mockQuerySnapshot: QuerySnapshot

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
          emptyList()
      )

  @Before
  fun setUp() {
      MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    providerRepositoryFirestore = ProviderRepositoryFirestore(mockFirestore)

    Mockito.`when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    Mockito.`when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    Mockito.`when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    Mockito.`when`(mockDocumentReference.id).thenReturn("test uid")
      MatcherAssert.assertThat(
          providerRepositoryFirestore.getNewUid(),
          CoreMatchers.`is`("test uid")
      )
  }

  @Test
  fun addProvider() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.addProvider(provider, {}, {})
    verify(mockDocumentReference).set(eq(provider))
  }

  @Test
  fun deleteProvider() {
    Mockito.`when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.deleteProvider(provider.uid, {}, {})
    verify(mockDocumentReference).delete()
  }

  @Test
  fun updateProvider() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.updateProvider(provider, {}, {})
    verify(mockDocumentReference).set(eq(provider))
  }

  @Test
  fun getProviders() {
    Mockito.`when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    Mockito.`when`(mockQuerySnapshot.documents).thenReturn(listOf())
    providerRepositoryFirestore.getProviders(null, {}, { TestCase.fail("Should not fail") })
    verify(mockCollectionReference).get()
  }
}