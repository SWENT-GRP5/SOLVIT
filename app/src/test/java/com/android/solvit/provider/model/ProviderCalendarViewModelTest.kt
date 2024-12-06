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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")
  private val testDispatcher = StandardTestDispatcher()

  // Test dates
  private val testDate = LocalDate.of(2024, 1, 1)
  private val testDateTime = LocalDateTime.of(2024, 1, 1, 10, 0)

  // Create a test schedule with regular hours and exceptions
  private val testSchedule =
      Schedule(
          regularHours =
              mutableMapOf(
                  DayOfWeek.MONDAY.name to
                      mutableListOf(TimeSlot(9, 0, 12, 0), TimeSlot(13, 0, 17, 0)),
                  DayOfWeek.WEDNESDAY.name to mutableListOf(TimeSlot(10, 0, 16, 0))),
          exceptions =
              mutableListOf(
                  ScheduleException(
                      date = LocalDateTime.of(2024, 1, 1, 0, 0),
                      slots = mutableListOf(TimeSlot(14, 0, 18, 0)),
                      type = ExceptionType.EXTRA_TIME)))

  // Test provider with schedule
  private val testProvider =
      Provider(
          uid = testUserId,
          name = "Test Provider",
          service = Services.PLUMBER,
          imageUrl = "test_image_url",
          companyName = "Test Company",
          phone = "123-456-7890",
          location = testLocation,
          description = "Test description",
          popular = true,
          rating = 4.5,
          price = 100.0,
          deliveryTime = Timestamp.now(),
          languages = listOf(Language.ENGLISH),
          schedule = testSchedule)

  // Test service requests
  private val assignedRequest =
      ServiceRequest(
          uid = "request1",
          title = "Test Request",
          type = Services.PLUMBER,
          description = "Test description",
          userId = "test_customer",
          providerId = testUserId,
          dueDate = Timestamp.now(),
          meetingDate =
              Timestamp(testDateTime.atZone(ZoneId.systemDefault()).toInstant().epochSecond, 0),
          location = testLocation,
          status = ServiceRequestStatus.PENDING)

  private val unassignedRequest = assignedRequest.copy(uid = "request2", providerId = null)
  private val otherProviderRequest =
      assignedRequest.copy(uid = "request3", providerId = "other_provider_id")

  // Dependencies
  private lateinit var authRepository: AuthRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var calendarViewModel: ProviderCalendarViewModel
  private lateinit var mockUser: User
  private lateinit var requestsFlow: MutableStateFlow<List<ServiceRequest>>
  private lateinit var acceptedRequestsFlow: MutableStateFlow<List<ServiceRequest>>

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Set up mocks
    authRepository = mock()
    providerRepository = mock()
    mockUser = mock()
    serviceRequestViewModel = mock()

    // Configure mock user
    whenever(mockUser.uid).thenReturn(testUserId)

    // Set up user flow before initializing AuthViewModel
    whenever(authRepository.init(any())).then {
      val callback = it.getArgument<(User?) -> Unit>(0)
      callback(mockUser)
      Unit
    }

    // Configure auth view model with mock user
    authViewModel = AuthViewModel(authRepository)

    // Configure service request view model with test data
    requestsFlow = MutableStateFlow(emptyList())
    acceptedRequestsFlow = MutableStateFlow(emptyList())

    whenever(serviceRequestViewModel.requests)
        .thenReturn(requestsFlow as StateFlow<List<ServiceRequest>>)
    whenever(serviceRequestViewModel.acceptedRequests)
        .thenReturn(acceptedRequestsFlow as StateFlow<List<ServiceRequest>>)

    doAnswer {
          requestsFlow.value = listOf(assignedRequest, unassignedRequest, otherProviderRequest)
          acceptedRequestsFlow.value = listOf(assignedRequest)
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

    // Wait for initialization
    testDispatcher.scheduler.advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testIsAvailable() {
    // Test regular Monday slot (should be available)
    val regularMonday = LocalDateTime.of(2024, 1, 8, 10, 0)
    assertTrue(calendarViewModel.isAvailable(regularMonday))

    // Test regular Monday lunch break (should not be available)
    val mondayLunch = LocalDateTime.of(2024, 1, 8, 12, 30)
    assertFalse(calendarViewModel.isAvailable(mondayLunch))

    // Test exception Monday (should be available according to exception hours)
    val exceptionMonday = LocalDateTime.of(2024, 1, 1, 15, 0)
    assertTrue(calendarViewModel.isAvailable(exceptionMonday))
  }

  @Test
  fun testAddExtraTimeException() = runTest {
    // Given
    val extraTimeSlot = TimeSlot(18, 0, 20, 0)
    val date = LocalDateTime.of(2024, 1, 15, 0, 0) // Unique date to avoid merging
    var successResult = false
    var feedbackMessage = ""

    // Mock provider repository update to succeed
    whenever(providerRepository.updateProvider(any(), any(), any())).then {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
      Unit
    }

    // When
    calendarViewModel.addExtraTimeException(date, listOf(extraTimeSlot)) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Exception added successfully", feedbackMessage)
  }

  @Test
  fun testCheckServiceRequestConflict() = runTest {
    // Set up a clean schedule for Monday (our test date)
    val cleanSchedule =
        Schedule(
            regularHours =
                mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            exceptions = mutableListOf())
    val cleanProvider = testProvider.copy(schedule = cleanSchedule)

    // Clear any existing requests and set up clean flows
    requestsFlow = MutableStateFlow(emptyList())
    acceptedRequestsFlow = MutableStateFlow(emptyList())
    serviceRequestViewModel = mock {
      on { requests } doReturn requestsFlow
      on { acceptedRequests } doReturn acceptedRequestsFlow
    }

    // Mock provider repository to return our clean provider
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      println("Mocked provider schedule: ${cleanProvider.schedule}")
      onSuccess(cleanProvider)
      Unit
    }

    // Recreate view model with clean state
    calendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)
    testDispatcher.scheduler.advanceUntilIdle()

    // Create a request during regular hours
    val meetingDate = LocalDateTime.of(2024, 1, 1, 10, 0) // Monday at 10 AM
    val meetingTimestamp =
        Timestamp(meetingDate.atZone(ZoneId.systemDefault()).toInstant().epochSecond, 0)
    val dueDate =
        Timestamp(meetingDate.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().epochSecond, 0)
    println("Meeting date: $meetingDate (${meetingDate.dayOfWeek})")

    val request =
        ServiceRequest(
            uid = "test_request",
            title = "Test Request",
            type = Services.PLUMBER,
            description = "Test description",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = dueDate,
            meetingDate = meetingTimestamp,
            location = testLocation,
            status = ServiceRequestStatus.PENDING)

    // Check for conflicts
    val result = calendarViewModel.checkServiceRequestConflict(request)
    println("Conflict result: $result")
    println("Current provider schedule: ${calendarViewModel.currentProvider.value.schedule}")
    assertFalse(result.hasConflict, "Expected no conflict during regular hours")
    assertEquals("No conflicts", result.reason)
  }

  @Test
  fun testServiceRequestsFiltering() = runTest {
    // Set up a clean schedule for Monday
    val cleanSchedule =
        Schedule(
            regularHours =
                mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            exceptions = mutableListOf())
    val cleanProvider = testProvider.copy(schedule = cleanSchedule)

    // Use the predefined assignedRequest instead of creating a new one
    val request = assignedRequest

    // Set up the flows with our test data
    requestsFlow.value = listOf(request)
    acceptedRequestsFlow.value = emptyList()

    // Mock provider repository
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      onSuccess(cleanProvider)
      Unit
    }

    // Create view model and wait for initialization
    calendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)
    testDispatcher.scheduler.advanceUntilIdle()

    // Set the view to the test date
    calendarViewModel.onDateSelected(testDateTime.toLocalDate())
    calendarViewModel.onCalendarViewChanged(CalendarView.DAY)
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify the request appears in time slots
    val timeSlots = calendarViewModel.timeSlots.value
    val requests = timeSlots[testDateTime.toLocalDate()] ?: emptyList()
    assertEquals(1, requests.size, "Expected one request for the test date")
    assertEquals(request, requests[0], "Request should match the test request")
  }
}
