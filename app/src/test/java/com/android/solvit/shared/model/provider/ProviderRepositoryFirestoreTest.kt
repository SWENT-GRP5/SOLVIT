package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Tasks.forResult
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProviderRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
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
  fun setup() {
    openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    mockTaskDoc = mock(Task::class.java) as Task<DocumentSnapshot>
    mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(mockTaskDoc)
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    // Handle task completion
    Mockito.doAnswer { invocation ->
          val callback = invocation.getArgument<OnCompleteListener<DocumentSnapshot>>(0)
          callback.onComplete(mockTaskDoc)
          mockTaskDoc
        }
        .`when`(mockTaskDoc)
        .addOnCompleteListener(any())

    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)

    providerRepositoryFirestore = ProviderRepositoryFirestore(mockFirestore, mockStorage)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("test uid")
    MatcherAssert.assertThat(providerRepositoryFirestore.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun addProvider() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    providerRepositoryFirestore.addProvider(provider, any(), {}, {})
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

  @Test
  fun getProvider() {
    setupBasicProviderFields()

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    assertEquals("test-id", capturedProvider!!.uid)
    assertEquals("Test Provider", capturedProvider!!.name)
    assertEquals("PLUMBER", capturedProvider!!.service.toString())
  }

  @Test
  fun `test getProvider with valid schedule`() {
    // Setup basic provider fields
    setupBasicProviderFields()

    // Setup schedule data with regular hours and exceptions
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "MONDAY" to
                        listOf(
                            mapOf(
                                "startHour" to 9L,
                                "startMinute" to 0L,
                                "endHour" to 17L,
                                "endMinute" to 0L),
                            mapOf(
                                "startHour" to 18L,
                                "startMinute" to 0L,
                                "endHour" to 20L,
                                "endMinute" to 0L)),
                    "TUESDAY" to
                        listOf(
                            mapOf(
                                "startHour" to 10L,
                                "startMinute" to 0L,
                                "endHour" to 18L,
                                "endMinute" to 0L))),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to Timestamp.now(),
                        "timeSlots" to
                            listOf(
                                mapOf(
                                    "startHour" to 9L,
                                    "startMinute" to 0L,
                                    "endHour" to 12L,
                                    "endMinute" to 0L)),
                        "type" to "OFF_TIME")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val schedule = capturedProvider!!.schedule
    assertEquals(2, schedule.regularHours.size)
    assertTrue(schedule.regularHours.containsKey("MONDAY"))
    assertTrue(schedule.regularHours.containsKey("TUESDAY"))
    assertEquals(2, schedule.regularHours["MONDAY"]?.size)
    assertEquals(1, schedule.regularHours["TUESDAY"]?.size)
    assertEquals(1, schedule.exceptions.size)
    assertEquals(1, schedule.exceptions[0].timeSlots.size)
  }

  @Test
  fun `test getProvider with empty schedule`() {
    // Setup basic provider fields
    setupBasicProviderFields()

    // Setup empty schedule
    val scheduleMap = mapOf<String, Any>()
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val schedule = capturedProvider!!.schedule
    assertTrue(schedule.regularHours.isEmpty())
    assertTrue(schedule.exceptions.isEmpty())
  }

  @Test
  fun `test getProvider with invalid time slot format`() {
    // Setup basic provider fields
    setupBasicProviderFields()

    // Setup schedule with invalid time slot data
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "MONDAY" to
                        listOf(
                            mapOf(
                                "startHour" to "invalid", // String instead of Long
                                "startMinute" to 0L,
                                "endHour" to 17L,
                                "endMinute" to 0L))),
            "exceptions" to listOf<Map<String, Any>>())
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val schedule = capturedProvider!!.schedule
    assertTrue(schedule.regularHours["MONDAY"]?.isEmpty() ?: true)
  }

  @Test
  fun `test schedule initialization with null fields`() {
    setupBasicProviderFields()

    // Setup schedule with null fields
    val scheduleMap = mapOf("regularHours" to null, "exceptions" to null)
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val schedule = capturedProvider!!.schedule
    assertTrue(schedule.regularHours.isEmpty())
    assertTrue(schedule.exceptions.isEmpty())
  }

  @Test
  fun `test schedule initialization with malformed exception data`() {
    setupBasicProviderFields()

    // Setup schedule with malformed exception data
    val scheduleMap =
        mapOf(
            "regularHours" to emptyMap<String, List<Map<String, Any>>>(),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to "invalid-timestamp", // Invalid timestamp format
                        "timeSlots" to
                            listOf(
                                mapOf(
                                    "startHour" to 9L,
                                    "startMinute" to 0L,
                                    "endHour" to 12L,
                                    "endMinute" to 0L)))))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val schedule = capturedProvider!!.schedule
    assertTrue(schedule.exceptions.isEmpty()) // Should ignore malformed exception
  }

  @Test
  fun `test conversion of invalid time slots`() {
    setupBasicProviderFields()

    // Setup schedule with invalid time slot values
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "MONDAY" to
                        listOf(
                            mapOf(
                                "startHour" to -1L, // Invalid hour
                                "startMinute" to 60L, // Invalid minute
                                "endHour" to 24L, // Invalid hour
                                "endMinute" to -30L // Invalid minute
                                ))))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val schedule = capturedProvider!!.schedule
    assertTrue(schedule.regularHours["MONDAY"]?.isEmpty() ?: true)
  }

  @Test
  fun `test schedule exception validation`() {
    // Setup basic provider fields
    setupBasicProviderFields()

    // Setup schedule with overlapping exceptions
    val overlappingScheduleMap =
        mapOf(
            "regularHours" to mapOf("MONDAY" to listOf(createMockTimeSlot(9, 0, 17, 0))),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to Timestamp.now(),
                        "timeSlots" to
                            listOf(
                                createMockTimeSlot(10, 0, 12, 0),
                                createMockTimeSlot(11, 0, 13, 0) // Overlapping slot
                                ),
                        "type" to "EXTRA_TIME")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(overlappingScheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { p -> capturedProvider = p },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    // Verify that overlapping slots are handled gracefully
    assertEquals(1, capturedProvider!!.schedule.exceptions.size)
    assertEquals(2, capturedProvider!!.schedule.exceptions[0].timeSlots.size)
  }

  private fun setupBasicProviderFields() {
    `when`(mockDocumentSnapshot.id).thenReturn("test-id")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Provider")
    `when`(mockDocumentSnapshot.getString("service")).thenReturn("PLUMBER")
    `when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn("http://test.com/image.jpg")
    `when`(mockDocumentSnapshot.getString("description")).thenReturn("Test Description")
    `when`(mockDocumentSnapshot.getString("companyName")).thenReturn("Test Company")
    `when`(mockDocumentSnapshot.getString("phone")).thenReturn("123456789")

    val locationMap = mapOf("latitude" to 46.5197, "longitude" to 6.6323, "name" to "Test Location")
    `when`(mockDocumentSnapshot.get("location")).thenReturn(locationMap)

    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(4.5)
    `when`(mockDocumentSnapshot.getBoolean("popular")).thenReturn(true)
    `when`(mockDocumentSnapshot.getDouble("price")).thenReturn(100.0)
    `when`(mockDocumentSnapshot.getTimestamp("deliveryTime")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.get("languages")).thenReturn(listOf("ENGLISH", "FRENCH"))
  }

  private fun createMockTimeSlot(
      startHour: Long,
      startMinute: Long,
      endHour: Long,
      endMinute: Long
  ): Map<String, Long> {
    return mapOf(
        "startHour" to startHour,
        "startMinute" to startMinute,
        "endHour" to endHour,
        "endMinute" to endMinute)
  }
}
