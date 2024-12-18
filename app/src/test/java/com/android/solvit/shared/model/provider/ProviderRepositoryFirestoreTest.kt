package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
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
import com.google.firebase.firestore.Transaction
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.ZoneId
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
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
          20.0,
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
                                    "endMinute" to 0L))),
                    "type" to "OFF_TIME"))
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

  @Test
  fun `test schedule initialization with invalid time slot format`() {
    setupBasicProviderFields()

    // Setup schedule with invalid time slot values
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "MONDAY" to
                        listOf(
                            mapOf(
                                "startHour" to "invalid",
                                "startMinute" to 0,
                                "endHour" to 17,
                                "endMinute" to 0))),
            "exceptions" to emptyList<Map<String, Any>>())
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    // Get provider
    var provider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id", onSuccess = { p -> provider = p }, onFailure = { fail("Should not fail") })

    assertNotNull(provider)
    assertTrue(provider!!.schedule.regularHours["MONDAY"]?.isEmpty() ?: true)
  }

  @Test
  fun `test schedule initialization with invalid exception data`() {
    setupBasicProviderFields()

    // Setup schedule with invalid exception data
    val scheduleMap =
        mapOf(
            "regularHours" to emptyMap<String, Any>(),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to "invalid",
                        "timeSlots" to
                            listOf(
                                mapOf(
                                    "startHour" to 9,
                                    "startMinute" to 0,
                                    "endHour" to 17,
                                    "endMinute" to 0)),
                        "type" to "OFF_TIME")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    // Get provider
    var provider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id", onSuccess = { p -> provider = p }, onFailure = { fail("Should not fail") })

    assertNotNull(provider)
    assertTrue(provider!!.schedule.exceptions.isEmpty())
  }

  @Test
  fun `test schedule initialization with invalid exception type`() {
    setupBasicProviderFields()

    // Setup schedule with invalid exception type
    val scheduleMap =
        mapOf(
            "regularHours" to emptyMap<String, Any>(),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to Timestamp.now(),
                        "timeSlots" to
                            listOf(
                                mapOf(
                                    "startHour" to 9,
                                    "startMinute" to 0,
                                    "endHour" to 17,
                                    "endMinute" to 0)),
                        "type" to "INVALID_TYPE")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    // Get provider
    var provider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id", onSuccess = { p -> provider = p }, onFailure = { fail("Should not fail") })

    assertNotNull(provider)
    assertTrue(provider!!.schedule.exceptions.isEmpty())
  }

  @Test
  fun `test schedule initialization with missing fields`() {
    setupBasicProviderFields()

    // Setup schedule with missing fields
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "MONDAY" to
                        listOf(
                            mapOf(
                                "startHour" to 9, "startMinute" to 0
                                // Missing endHour and endMinute
                                ))),
            "exceptions" to emptyList<Map<String, Any>>())
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    // Get provider
    var provider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id", onSuccess = { p -> provider = p }, onFailure = { fail("Should not fail") })

    assertNotNull(provider)
    assertTrue(provider!!.schedule.regularHours["MONDAY"]?.isEmpty() ?: true)
  }

  @Test
  fun `test schedule initialization with invalid day name`() {
    setupBasicProviderFields()

    // Setup schedule with invalid day name
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "INVALID_DAY" to
                        listOf(
                            mapOf(
                                "startHour" to 9,
                                "startMinute" to 0,
                                "endHour" to 17,
                                "endMinute" to 0))),
            "exceptions" to emptyList<Map<String, Any>>())
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    // Get provider
    var provider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id", onSuccess = { p -> provider = p }, onFailure = { fail("Should not fail") })

    assertNotNull(provider)
    assertTrue(provider!!.schedule.regularHours.isEmpty())
  }

  @Test
  fun `test schedule initialization with null schedule data`() {
    setupBasicProviderFields()
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(null)

    // Get provider
    var provider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id", onSuccess = { p -> provider = p }, onFailure = { fail("Should not fail") })

    assertNotNull(provider)
    assertTrue(provider!!.schedule.regularHours.isEmpty())
    assertTrue(provider!!.schedule.exceptions.isEmpty())
  }

  @Test
  fun `returnProvider returns valid Provider on success`() = runTest {
    // Mock Firestore to return a successful DocumentSnapshot
    val uid = "test"
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Setup all the necessary fields using our helper
    setupBasicProviderFields()

    // Call the suspend function
    val result = providerRepositoryFirestore.returnProvider(uid)

    // Validate the result
    assertNotNull(result)
    assertEquals("test-id", result!!.uid)
    assertEquals("Test Provider", result.name)
    assertEquals("PLUMBER", result.service.toString())
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

  @Test
  fun `test complex schedule operations with multiple exceptions`() {
    // Setup basic provider fields
    setupBasicProviderFields()

    // Setup complex schedule with multiple regular hours and exceptions
    val scheduleMap =
        mapOf(
            "regularHours" to
                mapOf(
                    "MONDAY" to
                        listOf(createMockTimeSlot(9, 0, 12, 0), createMockTimeSlot(14, 0, 17, 0)),
                    "WEDNESDAY" to listOf(createMockTimeSlot(10, 0, 16, 0))),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to Timestamp.now(),
                        "timeSlots" to listOf(createMockTimeSlot(9, 0, 12, 0)),
                        "type" to "OFF_TIME"),
                    mapOf(
                        "timestamp" to Timestamp.now(),
                        "timeSlots" to listOf(createMockTimeSlot(14, 0, 18, 0)),
                        "type" to "EXTRA_TIME")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    assertEquals(2, capturedProvider!!.schedule.regularHours.size)
    assertEquals(2, capturedProvider!!.schedule.exceptions.size)
  }

  @Test
  fun `test error handling for malformed schedule data`() {
    setupBasicProviderFields()

    // Setup malformed schedule data
    val malformedScheduleMap =
        mapOf(
            "regularHours" to mapOf("INVALID_DAY" to listOf(createMockTimeSlot(9, 0, 17, 0))),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to "invalid-timestamp",
                        "timeSlots" to listOf(createMockTimeSlot(9, 0, 12, 0)),
                        "type" to "INVALID_TYPE")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(malformedScheduleMap)

    var capturedProvider: Provider? = null
    var capturedError: Exception? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { error -> capturedError = error })

    assertNotNull(capturedProvider)
    assertTrue(capturedProvider!!.schedule.regularHours.isEmpty())
    assertTrue(capturedProvider!!.schedule.exceptions.isEmpty())
  }

  @Test
  fun `test edge cases in provider data conversion`() {
    setupBasicProviderFields()

    // Test with empty strings and zero values
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("")
    `when`(mockDocumentSnapshot.getString("imageUrl")).thenReturn("")
    `when`(mockDocumentSnapshot.getString("companyName")).thenReturn("")
    `when`(mockDocumentSnapshot.getString("phone")).thenReturn("")
    `when`(mockDocumentSnapshot.getString("description")).thenReturn("")
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(0.0)
    `when`(mockDocumentSnapshot.getDouble("price")).thenReturn(0.0)
    `when`(mockDocumentSnapshot.getDouble("nbrOfJobs")).thenReturn(0.0)
    `when`(mockDocumentSnapshot.get("languages")).thenReturn(emptyList<String>())

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    assertEquals("", capturedProvider!!.name)
    assertEquals("", capturedProvider!!.imageUrl)
    assertEquals("", capturedProvider!!.companyName)
    assertEquals("", capturedProvider!!.phone)
    assertEquals("", capturedProvider!!.description)
    assertEquals(0.0, capturedProvider!!.rating)
    assertEquals(0.0, capturedProvider!!.price)
    assertEquals(0.0, capturedProvider!!.nbrOfJobs)
    assertTrue(capturedProvider!!.languages.isEmpty())
  }

  @Test
  fun `test schedule conversion with maximum values`() {
    setupBasicProviderFields()

    // Setup schedule with maximum possible values
    val scheduleMap =
        mapOf(
            "regularHours" to mapOf("MONDAY" to listOf(createMockTimeSlot(0, 0, 23, 59))),
            "exceptions" to
                listOf(
                    mapOf(
                        "timestamp" to Timestamp.now(),
                        "timeSlots" to listOf(createMockTimeSlot(0, 0, 23, 59)),
                        "type" to "EXTRA_TIME")))
    `when`(mockDocumentSnapshot.get("schedule")).thenReturn(scheduleMap)

    var capturedProvider: Provider? = null
    providerRepositoryFirestore.getProvider(
        "test-id",
        onSuccess = { provider -> capturedProvider = provider },
        onFailure = { fail("Should not fail") })

    assertNotNull(capturedProvider)
    val mondaySlots = capturedProvider!!.schedule.regularHours["MONDAY"]
    assertNotNull(mondaySlots)
    assertEquals(0, mondaySlots!![0].startHour)
    assertEquals(0, mondaySlots[0].startMinute)
    assertEquals(23, mondaySlots[0].endHour)
    assertEquals(59, mondaySlots[0].endMinute)
  }

  @Test
  fun `addAcceptedRequest adds request to schedule`() = runTest {
    val serviceRequest =
        ServiceRequest(
            uid = "test-request",
            providerId = "test-provider",
            meetingDate =
                Timestamp(
                    LocalDate.now()
                        .with(java.time.DayOfWeek.MONDAY) // Always use the next Monday
                        .atTime(10, 0) // 10:00 AM
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))

    val validSchedule =
        Schedule(
            acceptedTimeSlots = listOf(),
            regularHours = mutableMapOf("MONDAY" to mutableListOf(TimeSlot(9, 0, 17, 0))),
            exceptions = mutableListOf())

    val newTimeSlot =
        AcceptedTimeSlot(requestId = serviceRequest.uid, startTime = serviceRequest.meetingDate!!)

    val expectedSchedule =
        validSchedule.copy(acceptedTimeSlots = validSchedule.acceptedTimeSlots + newTimeSlot)

    // Mock Transaction and DocumentSnapshot
    val mockTransaction = mock(Transaction::class.java)
    `when`(mockTransaction.get(any())).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentSnapshot.toObject(Schedule::class.java)).thenReturn(validSchedule)

    // Mock Firestore chain for "schedules/current"
    val mockSchedulesCollection = mock(CollectionReference::class.java)
    val mockCurrentScheduleDocument = mock(DocumentReference::class.java)
    `when`(mockDocumentReference.collection("schedules")).thenReturn(mockSchedulesCollection)
    `when`(mockSchedulesCollection.document("current")).thenReturn(mockCurrentScheduleDocument)

    // Mock runTransaction with Transaction.Function
    `when`(mockFirestore.runTransaction(any<Transaction.Function<Void>>())).thenAnswer { invocation
      ->
      val function = invocation.arguments[0] as Transaction.Function<Void>
      function.apply(mockTransaction) // Pass the mocked transaction
      null
    }

    // Call the method
    providerRepositoryFirestore.addAcceptedRequest(serviceRequest)

    verify(mockFirestore).runTransaction(any<Transaction.Function<Void>>())

    verify(mockTransaction).get(eq(mockCurrentScheduleDocument))
    verify(mockTransaction).set(eq(mockCurrentScheduleDocument), eq(expectedSchedule))
  }

  @Test
  fun `removeAcceptedRequest removes request from schedule`() = runTest {
    // Set a future meeting date
    val meetingDate =
        Timestamp(
            LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY)
                .atTime(10, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant())

    // Mock ServiceRequest
    val serviceRequest =
        ServiceRequest(
            uid = "test-request", providerId = "test-provider", meetingDate = meetingDate)

    // Create a Schedule instance with the accepted time slot
    val existingTimeSlot =
        AcceptedTimeSlot(requestId = serviceRequest.uid, startTime = serviceRequest.meetingDate!!)
    val scheduleWithRequest =
        Schedule(
            acceptedTimeSlots = listOf(existingTimeSlot),
            regularHours =
                mutableMapOf(
                    "MONDAY" to mutableListOf(TimeSlot(9, 0, 17, 0)) // Regular working hours
                    ),
            exceptions = mutableListOf())

    // Expected updated schedule (without the removed time slot)
    val expectedSchedule =
        scheduleWithRequest.copy(
            acceptedTimeSlots =
                scheduleWithRequest.acceptedTimeSlots.filterNot {
                  it.requestId == serviceRequest.uid
                })

    // Mock Transaction and DocumentSnapshot
    val mockTransaction = mock(Transaction::class.java)

    // Mock Firestore chain for "schedules/current"
    val mockSchedulesCollection = mock(CollectionReference::class.java)
    val mockCurrentScheduleDocument = mock(DocumentReference::class.java)
    `when`(mockDocumentReference.collection("schedules")).thenReturn(mockSchedulesCollection)
    `when`(mockSchedulesCollection.document("current")).thenReturn(mockCurrentScheduleDocument)

    `when`(mockTransaction.get(mockCurrentScheduleDocument)).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentSnapshot.toObject(Schedule::class.java)).thenReturn(scheduleWithRequest)

    // Mock runTransaction with Transaction.Function
    `when`(mockFirestore.runTransaction(any<Transaction.Function<Void>>())).thenAnswer { invocation
      ->
      val function = invocation.arguments[0] as Transaction.Function<Void>
      function.apply(mockTransaction) // Pass the mocked transaction
      null
    }

    // Call the method
    providerRepositoryFirestore.removeAcceptedRequest(serviceRequest)

    // Verify that runTransaction was invoked
    verify(mockFirestore).runTransaction(any<Transaction.Function<Void>>())

    // Verify that the transaction attempted to get and update the correct document reference
    verify(mockTransaction).get(eq(mockCurrentScheduleDocument))
    verify(mockTransaction).set(eq(mockCurrentScheduleDocument), eq(expectedSchedule))
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
