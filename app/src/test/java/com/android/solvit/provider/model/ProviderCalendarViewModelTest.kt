package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.*
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")

  // Create a test schedule with regular hours and exceptions
  private val testSchedule =
      Schedule(
          regularHours =
              mapOf(
                  DayOfWeek.MONDAY.name to
                      listOf(
                          TimeSlot(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                          TimeSlot(LocalTime.of(13, 0), LocalTime.of(17, 0))),
                  DayOfWeek.WEDNESDAY.name to
                      listOf(TimeSlot(LocalTime.of(10, 0), LocalTime.of(16, 0)))),
          exceptions =
              listOf(
                  ScheduleException(
                      LocalDateTime.of(2024, 1, 1, 0, 0), // First Monday of 2024
                      listOf(TimeSlot(LocalTime.of(14, 0), LocalTime.of(18, 0))))))

  private val testProvider =
      Provider(
          uid = testUserId,
          name = "Test Provider",
          service = Services.PLUMBER,
          imageUrl = "",
          companyName = "",
          phone = "",
          location = testLocation,
          description = "",
          popular = false,
          rating = 0.0,
          price = 0.0,
          deliveryTime = Timestamp.now(),
          languages = listOf(Language.ENGLISH),
          schedule = testSchedule)

  private lateinit var authRepository: AuthRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var mockUser: User
  private lateinit var userFlow: MutableStateFlow<User?>
  private lateinit var requestsFlow: MutableStateFlow<List<ServiceRequest>>
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var calendarViewModel: ProviderCalendarViewModel

  // Test data for service requests
  private val testDate = LocalDateTime.of(2024, 1, 8, 10, 0) // A Monday at 10:00
  private val assignedRequest =
      ServiceRequest(
          uid = "request1",
          title = "Fix leaky faucet",
          type = Services.PLUMBER,
          description = "Test request",
          userId = "seeker1",
          providerId = testUserId, // Assigned to our test provider
          dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = 100.0,
          status = ServiceRequestStatus.PENDING)

  private val unassignedRequest =
      ServiceRequest(
          uid = "request2",
          title = "Unassigned plumbing job",
          type = Services.PLUMBER,
          description = "Unassigned request",
          userId = "seeker2",
          providerId = null, // Not assigned to any provider
          dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = null,
          status = ServiceRequestStatus.PENDING)

  private val otherProviderRequest =
      ServiceRequest(
          uid = "request3",
          title = "Other provider job",
          type = Services.PLUMBER,
          description = "Other provider request",
          userId = "seeker3",
          providerId = "other_provider_id", // Assigned to a different provider
          dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = 150.0,
          status = ServiceRequestStatus.PENDING)

  private lateinit var noMeetingRequest: ServiceRequest

  @Before
  fun setUp() {
    // Set up mocks
    authRepository = mock()
    providerRepository = mock()
    mockUser = mock()
    serviceRequestViewModel = mock()

    // Configure mock user
    whenever(mockUser.uid).thenReturn(testUserId)

    // Set up user flow before initializing AuthViewModel
    userFlow = MutableStateFlow(mockUser)
    whenever(authRepository.init(any())).then {
      val callback = it.getArgument<(User?) -> Unit>(0)
      callback(mockUser)
      Unit
    }

    // Configure auth view model with mock user
    authViewModel = AuthViewModel(authRepository)

    // Configure service request view model with test data
    requestsFlow = MutableStateFlow(emptyList())
    whenever(serviceRequestViewModel.requests)
        .thenReturn(requestsFlow as StateFlow<List<ServiceRequest>>)
    doAnswer {
          requestsFlow.value = listOf(assignedRequest, unassignedRequest, otherProviderRequest)
          Unit
        }
        .whenever(serviceRequestViewModel)
        .getServiceRequests()

    // Configure provider repository mock
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
      Unit
    }

    // Initialize view model with mocked dependencies
    calendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)

    // Wait for provider to be loaded by triggering getProvider
    verify(providerRepository, timeout(1000)).getProvider(eq(testUserId), any(), any())
  }

  @Test
  fun testServiceRequests() = runBlocking {
    // Verify that getServiceRequests is called during initialization
    verify(serviceRequestViewModel).getServiceRequests()

    // Test that the flow only emits requests assigned to the current provider
    val requests = calendarViewModel.serviceRequests.first()
    assertEquals("Should only contain requests for current provider", 1, requests.size)
    assertEquals("Should be the assigned request", assignedRequest, requests[0])

    // Test filtering out requests with null meeting date
    noMeetingRequest = assignedRequest.copy(meetingDate = null)
    requestsFlow.value = listOf(noMeetingRequest, unassignedRequest, otherProviderRequest)
    val filteredRequests = calendarViewModel.serviceRequests.first()
    assertTrue("Should filter out requests with null meeting date", filteredRequests.isEmpty())

    // Test filtering out unassigned requests
    requestsFlow.value = listOf(unassignedRequest)
    val unassignedRequests = calendarViewModel.serviceRequests.first()
    assertTrue("Should filter out unassigned requests", unassignedRequests.isEmpty())

    // Test filtering out requests for other providers
    requestsFlow.value = listOf(otherProviderRequest)
    val wrongProviderRequests = calendarViewModel.serviceRequests.first()
    assertTrue("Should filter out requests for other providers", wrongProviderRequests.isEmpty())
  }

  @Test
  fun testServiceRequestsErrorHandling() = runBlocking {
    // Create a request that will cause an error when accessing providerId
    val errorRequest = mock<ServiceRequest>()
    whenever(errorRequest.meetingDate)
        .thenReturn(Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0))
    whenever(errorRequest.providerId).thenThrow(RuntimeException("Test error"))

    // Update the flow with the error-causing request
    requestsFlow.value = listOf(errorRequest)

    // Verify that the error is handled gracefully
    val requests = calendarViewModel.serviceRequests.first()
    assertTrue("Should filter out requests that cause errors", requests.isEmpty())
  }

  @Test
  fun testIsAvailable() {
    // Test regular Monday slot (should be available)
    val regularMonday = LocalDateTime.of(2024, 1, 8, 10, 0) // Second Monday of 2024
    assertTrue(
        "Should be available during regular Monday hours",
        calendarViewModel.isAvailable(regularMonday))

    // Test regular Monday lunch break (should not be available)
    val mondayLunch = LocalDateTime.of(2024, 1, 8, 12, 30)
    assertFalse(
        "Should not be available during lunch break", calendarViewModel.isAvailable(mondayLunch))

    // Test exception Monday (should be available according to exception hours)
    val exceptionMonday = LocalDateTime.of(2024, 1, 1, 15, 0)
    assertTrue(
        "Should be available during exception hours",
        calendarViewModel.isAvailable(exceptionMonday))

    // Test regular Wednesday slot (should be available)
    val wednesday = LocalDateTime.of(2024, 1, 3, 11, 0)
    assertTrue(
        "Should be available during Wednesday hours", calendarViewModel.isAvailable(wednesday))

    // Test Tuesday (should not be available)
    val tuesday = LocalDateTime.of(2024, 1, 2, 10, 0)
    assertFalse("Should not be available on Tuesday", calendarViewModel.isAvailable(tuesday))
  }

  @Test
  fun testGetAvailableSlots() {
    // Test regular Monday slots
    val regularMonday = LocalDateTime.of(2024, 1, 8, 0, 0)
    val regularMondaySlots = calendarViewModel.getAvailableSlots(regularMonday)
    assertEquals("Should have 2 slots on regular Monday", 2, regularMondaySlots.size)
    assertEquals(LocalTime.of(9, 0), regularMondaySlots[0].start)
    assertEquals(LocalTime.of(12, 0), regularMondaySlots[0].end)
    assertEquals(LocalTime.of(13, 0), regularMondaySlots[1].start)
    assertEquals(LocalTime.of(17, 0), regularMondaySlots[1].end)

    // Test exception Monday slots
    val exceptionMonday = LocalDateTime.of(2024, 1, 1, 0, 0)
    val exceptionSlots = calendarViewModel.getAvailableSlots(exceptionMonday)
    assertEquals("Should have 1 slot on exception Monday", 1, exceptionSlots.size)
    assertEquals(LocalTime.of(14, 0), exceptionSlots[0].start)
    assertEquals(LocalTime.of(18, 0), exceptionSlots[0].end)

    // Test Wednesday slots
    val wednesday = LocalDateTime.of(2024, 1, 3, 0, 0)
    val wednesdaySlots = calendarViewModel.getAvailableSlots(wednesday)
    assertEquals("Should have 1 slot on Wednesday", 1, wednesdaySlots.size)
    assertEquals(LocalTime.of(10, 0), wednesdaySlots[0].start)
    assertEquals(LocalTime.of(16, 0), wednesdaySlots[0].end)

    // Test Tuesday (no slots)
    val tuesday = LocalDateTime.of(2024, 1, 2, 0, 0)
    val tuesdaySlots = calendarViewModel.getAvailableSlots(tuesday)
    assertTrue("Should have no slots on Tuesday", tuesdaySlots.isEmpty())
  }

  @Test
  fun testLoadProviderNoUserLoggedIn() {
    // Create new auth view model with null user
    val nullAuthRepo = mock<AuthRepository>()
    val nullUser = mock<User>()
    whenever(nullUser.uid).thenReturn(null)
    whenever(nullAuthRepo.init(any())).then {
      val callback = it.getArgument<(User?) -> Unit>(0)
      callback(nullUser)
      Unit
    }
    val nullAuthViewModel = AuthViewModel(nullAuthRepo)

    // Create new provider repository for this test
    val testProviderRepo = mock<ProviderRepository>()

    // Create new view model with null user
    val viewModel =
        ProviderCalendarViewModel(testProviderRepo, nullAuthViewModel, serviceRequestViewModel)

    // Verify that getProvider was not called
    verify(testProviderRepo, never()).getProvider(any(), any(), any())
  }

  @Test
  fun testLoadProviderFailure() {
    // Create new provider repository that fails
    val failingRepo = mock<ProviderRepository>()
    whenever(failingRepo.getProvider(eq(testUserId), any(), any())).then {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(Exception("Test failure"))
      Unit
    }

    // Create new view model with failing repository
    val viewModel = ProviderCalendarViewModel(failingRepo, authViewModel, serviceRequestViewModel)

    // Verify that getProvider was called
    verify(failingRepo, timeout(1000)).getProvider(eq(testUserId), any(), any())
  }

  @Test
  fun testLoadProviderNullProvider() {
    // Create new provider repository that returns null
    val nullRepo = mock<ProviderRepository>()
    whenever(nullRepo.getProvider(eq(testUserId), any(), any())).then {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      onSuccess(null)
      Unit
    }

    // Create new view model with null-returning repository
    val viewModel = ProviderCalendarViewModel(nullRepo, authViewModel, serviceRequestViewModel)

    // Verify that getProvider was called
    verify(nullRepo, timeout(1000)).getProvider(eq(testUserId), any(), any())
  }
}
