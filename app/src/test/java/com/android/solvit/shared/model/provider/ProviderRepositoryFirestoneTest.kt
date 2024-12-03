package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
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
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockTaskUser: Task<UserRepository>
  @Mock private lateinit var mockTaskDoc: Task<DocumentSnapshot>
  @Mock private lateinit var mockStorage: FirebaseStorage

  private lateinit var providerRepositoryFirestore: ProviderRepositoryFirestore

  private val provider =
      Provider(
          "test",
          "test",
          Services.PLUMBER,
          "",
          "",
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

    providerRepositoryFirestore = ProviderRepositoryFirestore(mockFirestore, mockStorage)

    Mockito.`when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    Mockito.`when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    Mockito.`when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    Mockito.`when`(mockDocumentReference.id).thenReturn("test uid")
    MatcherAssert.assertThat(providerRepositoryFirestore.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun addProvider() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.addProvider(provider, any(), {}, {})
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

  @Test
  fun getProvider() {
    // For success

    Mockito.`when`(mockDocumentReference.get()).thenReturn(mockTaskDoc)
    Mockito.`when`(mockTaskDoc.isSuccessful).thenReturn(true)
    Mockito.`when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    Mockito.`when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockTaskDoc.addOnCompleteListener(Mockito.any())).thenAnswer {
      val listener = it.arguments[0] as OnCompleteListener<DocumentSnapshot>
      listener.onComplete(mockTaskDoc)
      mockTaskDoc
    }

    // Mock the document snapshot to return data
    // Mock basic string fields
    Mockito.`when`(mockDocumentSnapshot.id).thenReturn(provider.uid)
    Mockito.`when`(mockDocumentSnapshot.getString("name")).thenReturn(provider.name)
    Mockito.`when`(mockDocumentSnapshot.getString("service")).thenReturn(provider.service.name)
    Mockito.`when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn(provider.imageUrl)
    Mockito.`when`(mockDocumentSnapshot.getString("description")).thenReturn(provider.description)
    Mockito.`when`(mockDocumentSnapshot.getString("companyName")).thenReturn(provider.companyName)
    Mockito.`when`(mockDocumentSnapshot.getString("phone")).thenReturn(provider.phone)

    // Mock location map field
    val locationMap =
        mapOf(
            "latitude" to provider.location.latitude,
            "longitude" to provider.location.longitude,
            "name" to provider.location.name)
    Mockito.`when`(mockDocumentSnapshot.get("location")).thenReturn(locationMap)

    // Mock other fields
    Mockito.`when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(provider.rating)
    Mockito.`when`(mockDocumentSnapshot.getBoolean("popular")).thenReturn(provider.popular)
    Mockito.`when`(mockDocumentSnapshot.getDouble("price")).thenReturn(provider.price)
    Mockito.`when`(mockDocumentSnapshot.getTimestamp("deliveryTime"))
        .thenReturn(provider.deliveryTime)
    Mockito.`when`(mockDocumentSnapshot.get("languages"))
        .thenReturn(provider.languages.map { it.name })
    val onFailure: () -> Unit = mock()

    providerRepositoryFirestore.getProvider(
        "12345",
        onSuccess = { profile ->
          assertEquals(provider, profile) // Ensure correct profile is returned
        },
        onFailure = { onFailure() })

    verify(mockDocumentReference).get()
    Mockito.verify(mockTaskDoc).addOnCompleteListener(Mockito.any())
  }

  @Test
  fun `returnProvider returns valid Provider on success`() = runTest {
    // Mock Firestore to return a successful DocumentSnapshot
    val uid = "test"
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Mock the document fields
    `when`(mockDocumentSnapshot.id).thenReturn(uid)
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(provider.name)
    `when`(mockDocumentSnapshot.getString("service")).thenReturn(provider.service.name)
    `when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn(provider.imageUrl)
    `when`(mockDocumentSnapshot.getString("description")).thenReturn(provider.description)
    `when`(mockDocumentSnapshot.getString("companyName")).thenReturn(provider.companyName)
    `when`(mockDocumentSnapshot.getString("phone")).thenReturn(provider.phone)

    // Mock location map field
    val locationMap =
        mapOf(
            "latitude" to provider.location.latitude,
            "longitude" to provider.location.longitude,
            "name" to provider.location.name)
    `when`(mockDocumentSnapshot.get("location")).thenReturn(locationMap)

    // Mock other fields
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(provider.rating)
    `when`(mockDocumentSnapshot.getBoolean("popular")).thenReturn(provider.popular)
    `when`(mockDocumentSnapshot.getDouble("price")).thenReturn(provider.price)
    `when`(mockDocumentSnapshot.getTimestamp("deliveryTime")).thenReturn(provider.deliveryTime)
    `when`(mockDocumentSnapshot.get("languages")).thenReturn(provider.languages.map { it })

    // Call the suspend function
    val result = providerRepositoryFirestore.returnProvider(uid)

    // Validate the result
    assertEquals(provider, result)
  }

  @Test
  fun `returnProvider returns null when document does not exist`() = runTest {
    // Mock Firestore to return a DocumentSnapshot without necessary data
    val uid = "nonexistent"
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Mock the document as missing fields
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(null)

    // Call the suspend function
    val result = providerRepositoryFirestore.returnProvider(uid)

    // Validate the result is null
    assertNull(result)
  }
}
