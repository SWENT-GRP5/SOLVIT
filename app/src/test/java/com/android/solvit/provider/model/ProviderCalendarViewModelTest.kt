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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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

    // Initialize all mocks first
    authViewModel = mock()
    providerRepository = mock()
    serviceRequestViewModel = mock()

    // Mock user data
    val mockUser =
        User(
            uid = testUserId, // Use the class-level testUserId
            role = "provider",
            userName = "Test User",
            email = "test@test.com",
            locations = listOf(Location(0.0, 0.0, "")))
    val mockUserFlow = MutableStateFlow<User?>(mockUser)
    whenever(authViewModel.user).thenReturn(mockUserFlow)

    // Mock provider data
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).thenAnswer { invocation
      ->
      val successCallback = invocation.getArgument<(Provider?) -> Unit>(1)
      successCallback(testProvider) // Use the class-level testProvider
      Unit
    }

    // Mock service request data
    requestsFlow = MutableStateFlow(listOf(assignedRequest))
    acceptedRequestsFlow = MutableStateFlow(emptyList())

    whenever(serviceRequestViewModel.requests).thenReturn(requestsFlow)
    whenever(serviceRequestViewModel.acceptedRequests).thenReturn(acceptedRequestsFlow)

    // Initialize view model
    calendarViewModel =
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)

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
    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

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
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)
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
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)
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

  @Test
  fun testCalendarViewManagement() = runTest {
    // Test initial state
    assertEquals(LocalDate.now(), calendarViewModel.currentViewDate.value)
    assertEquals(LocalDate.now(), calendarViewModel.selectedDate.value)
    assertEquals(CalendarView.WEEK, calendarViewModel.calendarView.value)

    // Test changing view date
    val newViewDate = LocalDate.of(2024, 2, 1)
    calendarViewModel.onViewDateChanged(newViewDate)
    assertEquals(newViewDate, calendarViewModel.currentViewDate.value)

    // Test changing selected date
    val newSelectedDate = LocalDate.of(2024, 2, 15)
    calendarViewModel.onDateSelected(newSelectedDate)
    assertEquals(newSelectedDate, calendarViewModel.selectedDate.value)
    assertEquals(newSelectedDate, calendarViewModel.currentViewDate.value)

    // Test changing calendar view
    calendarViewModel.onCalendarViewChanged(CalendarView.MONTH)
    assertEquals(CalendarView.MONTH, calendarViewModel.calendarView.value)
    calendarViewModel.onCalendarViewChanged(CalendarView.DAY)
    assertEquals(CalendarView.DAY, calendarViewModel.calendarView.value)
  }

  @Test
  fun testAddOffTimeException() = runTest {
    // Given
    val offTimeSlot = TimeSlot(9, 0, 17, 0)
    val date = LocalDateTime.of(2024, 1, 15, 0, 0)
    var successResult = false
    var feedbackMessage = ""

    // Mock provider repository update to succeed
    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

    // When
    calendarViewModel.addOffTimeException(date, listOf(offTimeSlot)) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Exception added successfully", feedbackMessage)
  }

  @Test
  fun testScheduleUpdates() = runTest {
    // Test schedule update through adding exceptions
    var successResult = false
    var feedbackMessage = ""

    // Test successful update
    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

    calendarViewModel.addExtraTimeException(
        LocalDateTime.of(2024, 1, 15, 0, 0), listOf(TimeSlot(17, 0, 19, 0))) { success, feedback ->
          successResult = success
          feedbackMessage = feedback
        }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(successResult)
    assertEquals("Exception added successfully", feedbackMessage)

    // Test failed update
    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

    calendarViewModel.addOffTimeException(
        LocalDateTime.of(2024, 1, 15, 0, 0), listOf(TimeSlot(9, 0, 17, 0))) { success, feedback ->
          successResult = success
          feedbackMessage = feedback
        }
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(successResult)
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testSetAndClearRegularHours() = runTest {
    // Given
    val dayOfWeek = DayOfWeek.TUESDAY.name
    val timeSlots = listOf(TimeSlot(9, 0, 17, 0))
    var successResult = false

    // Mock successful provider update
    doAnswer { invocation ->
          val provider = invocation.getArgument<Provider>(0)
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // Test setting regular hours
    calendarViewModel.setRegularHours(dayOfWeek, timeSlots) { success -> successResult = success }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(successResult)

    // Test clearing regular hours
    successResult = false
    calendarViewModel.clearRegularHours(dayOfWeek) { success -> successResult = success }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(successResult)
    assertTrue(
        calendarViewModel.currentProvider.value.schedule.regularHours[dayOfWeek].isNullOrEmpty())

    // Test failure case
    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

    successResult = true
    calendarViewModel.setRegularHours(dayOfWeek, timeSlots) { success -> successResult = success }
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(successResult)
  }

  @Test
  fun testUpdateException() = runTest {
    // Given
    val date = LocalDateTime.of(2024, 1, 15, 0, 0)
    val timeSlots = listOf(TimeSlot(14, 0, 18, 0))
    var successResult = false
    var feedbackMessage = ""

    // Add an existing exception to update
    val existingException =
        ScheduleException(date, listOf(TimeSlot(9, 0, 12, 0)), ExceptionType.EXTRA_TIME)
    calendarViewModel.currentProvider.value.schedule.exceptions.add(existingException)

    // Mock successful provider update
    doAnswer { invocation ->
          val provider = invocation.getArgument<Provider>(0)
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // When
    calendarViewModel.updateException(date, timeSlots, ExceptionType.EXTRA_TIME) { success, feedback
      ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Exception updated successfully", feedbackMessage)

    // Test failure case
    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

    calendarViewModel.updateException(date, timeSlots, ExceptionType.EXTRA_TIME) { success, feedback
      ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(successResult)
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testGetExceptions() = runTest {
    // Given
    val startDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val endDate = LocalDateTime.of(2024, 1, 31, 23, 59)

    // Test getting all exceptions
    val allExceptions = calendarViewModel.getExceptions(startDate, endDate)
    assertEquals(1, allExceptions.size)
    assertEquals(ExceptionType.EXTRA_TIME, allExceptions[0].type)

    // Test filtering by type
    val extraTimeExceptions =
        calendarViewModel.getExceptions(startDate, endDate, ExceptionType.EXTRA_TIME)
    assertEquals(1, extraTimeExceptions.size)
    assertEquals(ExceptionType.EXTRA_TIME, extraTimeExceptions[0].type)

    val offTimeExceptions =
        calendarViewModel.getExceptions(startDate, endDate, ExceptionType.OFF_TIME)
    assertTrue(offTimeExceptions.isEmpty())

    // Test date range filtering
    val futureExceptions =
        calendarViewModel.getExceptions(
            LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59))
    assertTrue(futureExceptions.isEmpty())
  }

  @Test
  fun testOnServiceRequestClick() = runTest {
    // Given
    val request = assignedRequest

    // When
    calendarViewModel.onServiceRequestClick(request)

    // Then
    verify(serviceRequestViewModel).selectRequest(request)
  }

  @Test
  fun testAddExceptionFeedback() = runTest {
    // Given
    val date = LocalDateTime.of(2024, 1, 15, 0, 0)
    val timeSlots = listOf(TimeSlot(14, 0, 18, 0))
    var feedbackMessage = ""

    // Mock successful provider update
    doAnswer { invocation ->
          val provider = invocation.getArgument<Provider>(0)
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // Test add without merging
    calendarViewModel.addExtraTimeException(date, timeSlots) { _, feedback ->
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("Exception added successfully", feedbackMessage)

    // Test add with merging
    val existingException = ScheduleException(date, timeSlots, ExceptionType.EXTRA_TIME)
    calendarViewModel.currentProvider.value.schedule.exceptions.add(existingException)

    calendarViewModel.addExtraTimeException(date, timeSlots) { _, feedback ->
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(feedbackMessage.contains("Exception added and merged"))

    // Test failed operation
    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>())

    calendarViewModel.addExtraTimeException(date, timeSlots) { _, feedback ->
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testDateSelection() = runTest {
    // Given
    val testDate = LocalDate.of(2024, 2, 1)

    // When
    calendarViewModel.onDateSelected(testDate)

    // Then
    assertEquals(testDate, calendarViewModel.selectedDate.value)
    assertEquals(testDate, calendarViewModel.currentViewDate.value)
  }

  @Test
  fun testViewDateChange() = runTest {
    // Given
    val initialDate = LocalDate.of(2024, 1, 1)
    val newDate = LocalDate.of(2024, 2, 1)

    // When - Set initial date
    calendarViewModel.onViewDateChanged(initialDate)
    assertEquals(initialDate, calendarViewModel.currentViewDate.value)

    // When - Change view date
    calendarViewModel.onViewDateChanged(newDate)

    // Then
    assertEquals(newDate, calendarViewModel.currentViewDate.value)
    // Selected date should remain unchanged
    assertNotEquals(newDate, calendarViewModel.selectedDate.value)
  }

  @Test
  fun testInitialState() = runTest {
    // Verify initial state
    assertEquals(LocalDate.now(), calendarViewModel.selectedDate.value)
    assertEquals(LocalDate.now(), calendarViewModel.currentViewDate.value)
    assertEquals(CalendarView.WEEK, calendarViewModel.calendarView.value)
    assertTrue(calendarViewModel.timeSlots.value.isEmpty())
    assertFalse(calendarViewModel.isLoading.value)
  }

  @Test
  fun testLogMessages() {
    // Test log messages for various error scenarios
    var successResult = false
    var feedbackMessage = ""

    // Test invalid time slots
    calendarViewModel.addException(testDateTime, emptyList(), ExceptionType.OFF_TIME) {
        success,
        feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    assertFalse(successResult)
    assertEquals("No time slots provided", feedbackMessage)

    // Test repository failure
    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Test failure"))
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    calendarViewModel.addException(
        testDateTime, listOf(TimeSlot(10, 0, 11, 0)), ExceptionType.OFF_TIME) { success, feedback ->
          successResult = success
          feedbackMessage = feedback
        }
    assertFalse(successResult)
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testCheckServiceRequestConflictComprehensive() {
    // Test missing meeting date
    val requestWithoutDate = assignedRequest.copy(meetingDate = null)
    val resultNoDate = calendarViewModel.checkServiceRequestConflict(requestWithoutDate)
    assertTrue(resultNoDate.hasConflict)
    assertEquals("Meeting date not set", resultNoDate.reason)

    // Test request during regular hours
    val regularHoursRequest =
        assignedRequest.copy(
            meetingDate =
                Timestamp(
                    LocalDateTime.of(2024, 1, 1, 10, 0)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .epochSecond,
                    0))
    val resultRegularHours = calendarViewModel.checkServiceRequestConflict(regularHoursRequest)
    assertFalse(resultRegularHours.hasConflict)
    assertEquals("No conflicts", resultRegularHours.reason)

    // Test request with off-time conflict
    val offTimeSlot = TimeSlot(10, 0, 12, 0)
    testProvider.schedule.addException(
        LocalDateTime.of(2024, 1, 1, 0, 0), listOf(offTimeSlot), ExceptionType.OFF_TIME)
    val resultOffTime = calendarViewModel.checkServiceRequestConflict(regularHoursRequest)
    assertTrue(resultOffTime.hasConflict)
    assertEquals("This time slot conflicts with your off-time schedule", resultOffTime.reason)

    // Test request outside regular hours with extra time exception
    val outsideHoursRequest =
        assignedRequest.copy(
            meetingDate =
                Timestamp(
                    LocalDateTime.of(2024, 1, 1, 18, 0)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .epochSecond,
                    0))

    // First test without extra time exception
    val resultOutsideHours = calendarViewModel.checkServiceRequestConflict(outsideHoursRequest)
    assertTrue(resultOutsideHours.hasConflict)
    assertEquals("This time slot is outside your regular working hours", resultOutsideHours.reason)

    // Add extra time exception and test again
    testProvider.schedule.addException(
        LocalDateTime.of(2024, 1, 1, 0, 0),
        listOf(TimeSlot(18, 0, 20, 0)),
        ExceptionType.EXTRA_TIME)
    val resultWithExtraTime = calendarViewModel.checkServiceRequestConflict(outsideHoursRequest)
    assertFalse(resultWithExtraTime.hasConflict)
    assertEquals("No conflicts", resultWithExtraTime.reason)

    // Test conflict with another accepted request
    val conflictingTime = LocalDateTime.of(2024, 1, 1, 18, 0)
    val acceptedRequest =
        regularHoursRequest.copy(
            uid = "request1",
            meetingDate =
                Timestamp(
                    conflictingTime.atZone(ZoneId.systemDefault()).toInstant().epochSecond, 0))
    val acceptedRequestsFlow = MutableStateFlow(listOf(acceptedRequest))
    whenever(serviceRequestViewModel.acceptedRequests).thenReturn(acceptedRequestsFlow)
    val conflictingRequest = outsideHoursRequest.copy(uid = "conflicting_request")
    val resultConflict = calendarViewModel.checkServiceRequestConflict(conflictingRequest)
    assertTrue(resultConflict.hasConflict)
    assertEquals(
        "This time slot conflicts with another accepted service request", resultConflict.reason)
  }

  @Test
  fun testAddExceptionValidation() {
    var successResult = false
    var feedbackMessage = ""

    // Test empty time slots
    calendarViewModel.addException(testDateTime, emptyList(), ExceptionType.OFF_TIME) {
        success,
        feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    assertFalse(successResult)
    assertEquals("No time slots provided", feedbackMessage)

    // Test successful addition
    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          Unit
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    calendarViewModel.addException(
        testDateTime, listOf(TimeSlot(10, 0, 11, 0)), ExceptionType.OFF_TIME) { success, feedback ->
          successResult = success
          feedbackMessage = feedback
        }
    assertTrue(successResult)
    assertEquals("Exception added successfully", feedbackMessage)
  }
}
