package com.android.solvit.seeker.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.map.Location
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockTaskUser: Task<UserRepository>
  @Mock private lateinit var mockTaskDoc: Task<DocumentSnapshot>

  private lateinit var firebaseRepository: UserRepositoryFirestore

  private val testSeekerProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = Location(0.0, 0.0, "Chemin des Triaudes"))

  private val testLocation1 = Location(0.0, 0.0, "Location1")
  private val testLocation2 = Location(0.0, 0.0, "Location2")
  private val testUserId = "12345"
  private val mockPreferences = listOf("⚡ Electrical Work", "📚 Tutoring")

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

    Mockito.`when`(mockDocumentSnapshot.id).thenReturn(testSeekerProfile.uid)
    Mockito.`when`(mockDocumentSnapshot.getString("name")).thenReturn(testSeekerProfile.name)
    Mockito.`when`(mockDocumentSnapshot.getString("username"))
        .thenReturn(testSeekerProfile.username)
    Mockito.`when`(mockDocumentSnapshot.getString("email")).thenReturn(testSeekerProfile.email)
    Mockito.`when`(mockDocumentSnapshot.getString("phone")).thenReturn(testSeekerProfile.phone)
    Mockito.`when`(mockDocumentSnapshot.getString("address"))
        .thenReturn(testSeekerProfile.address.name)
    val onFailure: () -> Unit = mock()

    firebaseRepository.getUserProfile(
        uid = "12345",
        onSuccess = { profile ->
          assertEquals(testSeekerProfile, profile) // Ensure correct profile is returned
        },
        onFailure = { onFailure() })

    verify(mockDocumentReference).get()
    Mockito.verify(mockTaskDoc).addOnCompleteListener(Mockito.any())
  }

  /*@Test
  fun getUserProfileFail() {
    // Mocking a failed task scenario
    val mockException = Exception("Firestore error")

    // Simulate failure by setting isSuccessful to false and providing an exception
    Mockito.`when`(mockTaskFailure.isSuccessful).thenReturn(false)
    Mockito.`when`(mockTaskFailure.exception).thenReturn(mockException)

    Mockito.`when`(mockDocumentReference.get()).thenReturn(mockTaskFailure)

    // Call the method and verify failure callback is invoked
    firebaseRepository.getUserProfile(
        uid = "12345",
        onSuccess = { TestCase.fail("Success callback should not be called") },
        onFailure = { e ->
          assertEquals(
              mockException, e) // Ensure the failure callback is called with the right exception
        })
  }*/

  @Test
  fun updateUserProfileTest() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    firebaseRepository.updateUserProfile(
        profile = testSeekerProfile, onSuccess = {}, onFailure = {})
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).set(testSeekerProfile)
  }

  @Test
  fun addUserProfileTest() {
    Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    firebaseRepository.addUserProfile(profile = testSeekerProfile, onSuccess = {}, onFailure = {})
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).set(testSeekerProfile)
  }

  @Test
  fun getUsersProfile_callsFirestoreCollection() {

    Mockito.`when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    Mockito.`when`(mockQuerySnapshot.documents).thenReturn(listOf())

    firebaseRepository.getUsersProfile(
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

  @Test
  fun `getLocations returns parsed locations from document`() {
    val locationMap1 = mapOf("latitude" to 0.0, "longitude" to 0.0, "name" to "Location1")
    val locationMap2 = mapOf("latitude" to 0.0, "longitude" to 0.0, "name" to "Location2")

    `when`(mockDocumentSnapshot.get("cachedLocations"))
        .thenReturn(listOf(locationMap1, locationMap2))

    val locations = firebaseRepository.getLocations(mockDocumentSnapshot)

    assertEquals(2, locations.size)
    assertEquals(testLocation1, locations[0])
    assertEquals(testLocation2, locations[1])
  }

  @Test
  fun `updateUserLocations updates cachedLocations with new location added first`() {
    val newLocation = Location(48.8566, 2.3522, "New Location")
    val locations = mutableListOf(testLocation1, testLocation2)
    val updatedLocations = mutableListOf(newLocation, testLocation1)

    Mockito.`when`(mockDocumentReference.get()).thenReturn(mockTaskDoc)
    Mockito.`when`(mockTaskDoc.isSuccessful).thenReturn(true)
    Mockito.`when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    Mockito.`when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.get("cachedLocations")).thenReturn(locations)

    // Mock update call to return a completed task
    `when`(mockDocumentReference.update(eq("cachedLocations"), any()))
        .thenReturn(Tasks.forResult(null))

    firebaseRepository.updateUserLocations(
        testUserId,
        newLocation,
        onSuccess = { updatedList ->
          assertEquals(2, updatedList.size)
          assertEquals(newLocation, updatedList[0])
          assertEquals(testLocation1, updatedList[1])
        },
        onFailure = {})
  }

  @Test
  fun `getCachedLocation returns list of locations on success`() {
    // Mock the document retrieval task to return a successful result
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Mock the locations data
    val locationMap1 = mapOf("latitude" to 0.0, "longitude" to 0.0, "name" to "Location1")
    val locationMap2 = mapOf("latitude" to 0.0, "longitude" to 0.0, "name" to "Location2")
    `when`(mockDocumentSnapshot.get("cachedLocations"))
        .thenReturn(listOf(locationMap1, locationMap2))

    // Call getCachedLocation and assert the results
    firebaseRepository.getCachedLocation(
        testUserId,
        onSuccess = { locations ->
          assertEquals(2, locations.size)
          assertEquals(testLocation1, locations[0])
          assertEquals(testLocation2, locations[1])
        },
        onFailure = {})
  }

  @Test
  fun testAddUserPreferenceSuccess() {
    // Mock existing preferences in Firestore document
    val existingPreferences = mutableListOf("⚡ Electrical Work", "📚 Tutoring")
    Mockito.`when`(mockDocumentSnapshot.get("preferences")).thenReturn(existingPreferences)

    // Mock successful document retrieval and set operation
    Mockito.`when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    Mockito.`when`(mockDocumentReference.set(any<Map<String, Any>>()))
        .thenReturn(Tasks.forResult(null))

    // Test adding new preference
    firebaseRepository.addUserPreference(
        userId = testUserId,
        preference = "🔧 Plumbing",
        onSuccess = {
          // Verify that the new preference was added
          assert(existingPreferences.contains("🔧 Plumbing"))
        },
        onFailure = { TestCase.fail("Failure callback should not be called") })

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    // Verify the correct map was passed to the set() method, with the updated preferences
    verify(mockDocumentReference)
        .set(
            argThat { argument ->
              val map = argument as? Map<String, Any>
              val preferences = map?.get("preferences") as? List<*>
              preferences != null &&
                  preferences.contains("🔧 Plumbing") // Verify new preference is in the list
            })
  }

  @Test
  fun testAddUserPreferenceFailure() {
    // Mock a failure scenario
    val mockException = Exception("Firestore error")
    Mockito.`when`(mockDocumentReference.get()).thenReturn(Tasks.forException(mockException))

    firebaseRepository.addUserPreference(
        userId = testUserId,
        preference = "🔧 Plumbing",
        onSuccess = { TestCase.fail("Success callback should not be called") },
        onFailure = { e -> assertEquals(mockException, e) } // Verify failure callback is triggered
        )
  }

  @Test
  fun testDeleteUserPreferenceSuccess() {
    // Mock existing preferences
    val existingPreferences = mutableListOf("⚡ Electrical Work", "📚 Tutoring", "🔧 Plumbing")
    Mockito.`when`(mockDocumentSnapshot.get("preferences")).thenReturn(existingPreferences)

    // Mock successful document retrieval and update
    Mockito.`when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    Mockito.`when`(mockDocumentReference.update(eq("preferences"), any()))
        .thenReturn(Tasks.forResult(null))
    // Test removing a preference
    firebaseRepository.deleteUserPreference(
        userId = testUserId,
        preference = "🔧 Plumbing",
        onSuccess = {
          assert(!existingPreferences.contains("🔧 Plumbing")) // Verify the preference is removed
        },
        onFailure = { TestCase.fail("Failure callback should not be called") })

    Shadows.shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).update(eq("preferences"), any()) // Verify update was called
  }

  @Test
  fun testDeleteUserPreferenceFailure() {
    // Mock a failure scenario
    val mockException = Exception("Firestore error")
    Mockito.`when`(mockDocumentReference.get()).thenReturn(Tasks.forException(mockException))

    firebaseRepository.deleteUserPreference(
        userId = testUserId,
        preference = "🔧 Plumbing",
        onSuccess = { TestCase.fail("Success callback should not be called") },
        onFailure = { e -> assertEquals(mockException, e) } // Verify failure callback is triggered
        )
  }

  @Test
  fun `getUserPreferences should return user preferences on success`() {
    // Mock Firestore document retrieval to return a successful task
    Mockito.`when`(mockDocumentReference.get()).thenReturn(mockTaskDoc)
    Mockito.`when`(mockTaskDoc.isSuccessful).thenReturn(true)
    Mockito.`when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    Mockito.`when`(mockDocumentSnapshot.get("preferences")).thenReturn(mockPreferences)

    // Simulate adding a listener and calling onSuccess
    Mockito.`when`(mockTaskDoc.addOnCompleteListener(Mockito.any())).thenAnswer {
      val listener = it.arguments[0] as OnCompleteListener<DocumentSnapshot>
      listener.onComplete(mockTaskDoc)
      mockTaskDoc
    }

    // Call the method to test
    firebaseRepository.getUserPreferences(
        userId = testUserId,
        onSuccess = { preferences ->
          assertEquals(mockPreferences, preferences) // Verify preferences match
        },
        onFailure = { TestCase.fail("Failure callback should not be called") })

    verify(mockDocumentReference).get()
    Mockito.verify(mockTaskDoc).addOnCompleteListener(Mockito.any())
  }

  @Test
  fun `getUserPreferences should call onFailure on Firestore error`() {
    // Mock a failure scenario
    val mockException = Exception("Firestore error")
    Mockito.`when`(mockTaskDoc.isSuccessful).thenReturn(false)
    Mockito.`when`(mockTaskDoc.exception).thenReturn(mockException)
    Mockito.`when`(mockDocumentReference.get()).thenReturn(mockTaskDoc)

    // Simulate adding a listener and calling onFailure
    Mockito.`when`(mockTaskDoc.addOnCompleteListener(Mockito.any())).thenAnswer {
      val listener = it.arguments[0] as OnCompleteListener<DocumentSnapshot>
      listener.onComplete(mockTaskDoc)
      mockTaskDoc
    }

    // Call the method to test
    firebaseRepository.getUserPreferences(
        userId = testUserId,
        onSuccess = { TestCase.fail("Success callback should not be called") },
        onFailure = { e -> assertEquals(mockException, e) })

    verify(mockDocumentReference).get()
    Mockito.verify(mockTaskDoc).addOnCompleteListener(Mockito.any())
  }

  @Test
  fun documentToUser_success() {
    // Mock a DocumentSnapshot

    // Simulate the document having all the necessary fields
    `when`(mockDocumentSnapshot.id).thenReturn("12345")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("John Doe")
    `when`(mockDocumentSnapshot.getString("username")).thenReturn("johndoe")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("john.doe@example.com")
    `when`(mockDocumentSnapshot.getString("phone")).thenReturn("+1234567890")
    val locationMap =
        mapOf("latitude" to 46.5197, "longitude" to 6.6323, "name" to "Chemin des Triaudes")
    `when`(mockDocumentSnapshot.get("address")).thenReturn(locationMap)
    `when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn("")

    // Call the helper method
    val profile = firebaseRepository.documentToUser(mockDocumentSnapshot)

    // Assert that the profile was correctly created
    assertEquals("12345", profile?.uid)
    assertEquals("John Doe", profile?.name)
    assertEquals("johndoe", profile?.username)
    assertEquals("john.doe@example.com", profile?.email)
    assertEquals("+1234567890", profile?.phone)
    assertEquals("Chemin des Triaudes", profile?.address?.name)
    assertEquals("", profile?.imageUrl)
  }

  @Test
  fun `returnSeekerById returns valid SeekerProfile on success`() = runTest {
    // Mock the behavior of Firestore to return a successful task
    val uid = "12345"
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Mock the document fields
    `when`(mockDocumentSnapshot.id).thenReturn(uid)
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("John Doe")
    `when`(mockDocumentSnapshot.getString("username")).thenReturn("johndoe")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("john.doe@example.com")
    `when`(mockDocumentSnapshot.getString("phone")).thenReturn("+1234567890")
    val locationMap = mapOf("latitude" to 0.0, "longitude" to 0.0, "name" to "Chemin des Triaudes")
    `when`(mockDocumentSnapshot.get("address")).thenReturn(locationMap)
    `when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn("")
    `when`(mockDocumentSnapshot.get("preferences")).thenReturn(mockPreferences)

    // Call the suspend function
    val result = firebaseRepository.returnSeekerById(uid)

    // Validate the result
    assertEquals(
        SeekerProfile(
            uid = "12345",
            name = "John Doe",
            username = "johndoe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            address = Location(0.0, 0.0, "Chemin des Triaudes"),
            preferences = mockPreferences),
        result)
  }

  @Test
  fun `returnSeekerById returns null when document does not exist`() = runTest {
    // Mock the behavior of Firestore to return an empty document
    val uid = "56789"
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Mock the document as missing fields
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(null)

    // Call the suspend function
    val result = firebaseRepository.returnSeekerById(uid)

    // Validate the result is null
    assertNull(result)
  }
}
