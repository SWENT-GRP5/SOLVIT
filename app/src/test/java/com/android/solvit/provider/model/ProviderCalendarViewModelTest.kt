package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.ExceptionType
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.Schedule
import com.android.solvit.shared.model.provider.ScheduleException
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")
  private lateinit var testDispatcher: TestDispatcher

  // Test dates
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
  private var testProvider =
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
          nbrOfJobs = 0.0,
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

  // Dependencies
  private lateinit var providerRepository: ProviderRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var calendarViewModel: ProviderCalendarViewModel
  private lateinit var mockUser: User
  private var providerFlow = MutableStateFlow<List<Provider>>(emptyList())
  private var requestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
  private var acceptedRequestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
  private lateinit var userFlow: MutableStateFlow<User?>

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    // Initialize mocks
    providerRepository = mock()
    authViewModel = mock()
    serviceRequestViewModel = mock()

    // Set up test provider
    val schedule = Schedule(regularHours = mutableMapOf(), exceptions = mutableListOf())
    testProvider =
        Provider(
            uid = testUserId,
            name = "Test Provider",
            service = Services.TUTOR,
            location = Location(0.0, 0.0, ""),
            schedule = schedule)

    // Set up flows
    providerFlow = MutableStateFlow(listOf(testProvider))
    requestsFlow = MutableStateFlow(emptyList())
    acceptedRequestsFlow = MutableStateFlow(emptyList())
    mockUser =
        User(
            uid = testUserId,
            role = "provider",
            userName = "Test User",
            email = "test@test.com",
            locations = listOf(Location(0.0, 0.0, "")))
    userFlow = MutableStateFlow(mockUser)

    // Mock behavior
    whenever(authViewModel.user).thenReturn(userFlow)
    whenever(serviceRequestViewModel.requests).thenReturn(requestsFlow)
    whenever(serviceRequestViewModel.acceptedRequests).thenReturn(acceptedRequestsFlow)
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }
    whenever(providerRepository.addListenerOnProviders(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Provider>) -> Unit>(0)
      onSuccess(providerFlow.value)
    }

    // Create view model
    calendarViewModel =
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)
    testDispatcher.scheduler.advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testIsAvailable() = runTest {
    // Given a provider with regular hours and exceptions
    val testDate = LocalDateTime.of(2024, 1, 1, 10, 0) // Monday
    val regularHours =
        mutableMapOf(
            DayOfWeek.MONDAY.name to
                mutableListOf(
                    TimeSlot(startHour = 9, startMinute = 0, endHour = 12, endMinute = 0),
                    TimeSlot(startHour = 14, startMinute = 0, endHour = 17, endMinute = 0)))
    val exceptions =
        mutableListOf(
            ScheduleException(
                date = testDate,
                slots =
                    listOf(TimeSlot(startHour = 10, startMinute = 0, endHour = 11, endMinute = 0)),
                type = ExceptionType.OFF_TIME))

    // Create new provider with schedule
    testProvider =
        Provider(
            uid = testUserId,
            name = "Test Provider",
            service = Services.TUTOR,
            location = Location(0.0, 0.0, ""),
            schedule = Schedule(regularHours = regularHours, exceptions = exceptions))

    // Create new flow with updated provider
    providerFlow = MutableStateFlow(listOf(testProvider))

    // Create new view model to pick up the updated provider
    calendarViewModel =
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)

    // Wait for the view model to initialize
    testDispatcher.scheduler.advanceUntilIdle()

    // Test cases
    val testCases =
        listOf(
            Triple(
                LocalDateTime.of(2024, 1, 1, 9, 0), // During regular hours, no exception
                true,
                "Provider should be available during regular hours with no exception"),
            Triple(
                LocalDateTime.of(2024, 1, 1, 10, 30), // During off-time exception
                false,
                "Provider should not be available during off-time exception"),
            Triple(
                LocalDateTime.of(2024, 1, 1, 13, 0), // Outside regular hours
                false,
                "Provider should not be available outside regular hours"),
            Triple(
                LocalDateTime.of(2024, 1, 1, 14, 30), // During second regular hours slot
                true,
                "Provider should be available during second regular hours slot"),
            Triple(
                LocalDateTime.of(2024, 1, 2, 10, 0), // Different day
                false,
                "Provider should not be available on a day without regular hours"))

    // Test each case
    testCases.forEach { (dateTime, expectedAvailable, message) ->
      val isAvailable = calendarViewModel.isAvailable(dateTime)
      if (expectedAvailable) {
        assertTrue(message, isAvailable)
      } else {
        assertFalse(message, isAvailable)
      }
    }
  }

  @Test
  fun testAddExtraTimeException() = runTest {
    // Given
    val extraTimeSlot = TimeSlot(18, 0, 20, 0)
    val date = LocalDateTime.of(2024, 1, 15, 0, 0) // Unique date to avoid merging
    var successResult = false
    var feedbackMessage = ""

    // Mock provider repository update to succeed
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // When
    calendarViewModel.addExtraTimeException(date, listOf(extraTimeSlot)) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Schedule exception added successfully", feedbackMessage)
  }

  @Test
  fun testCheckServiceRequestConflict() = runTest {
    // Initialize with regular hours for Monday only
    val mondayHours = listOf(TimeSlot(9, 0, 17, 0))
    testProvider =
        Provider(
            uid = testUserId,
            schedule =
                Schedule(regularHours = mutableMapOf("MONDAY" to mondayHours.toMutableList())))
    providerFlow.value = listOf(testProvider) // Important: update flow
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }
    whenever(serviceRequestViewModel.acceptedRequests).thenReturn(MutableStateFlow(emptyList()))
    testDispatcher.scheduler.advanceUntilIdle() // Important: advance scheduler

    // Make sure the provider is initialized
    calendarViewModel.onDateSelected(LocalDate.of(2024, 1, 1))
    testDispatcher.scheduler.advanceUntilIdle()

    // Test within regular hours
    val mondayRequest =
        assignedRequest.copy(
            meetingDate =
                Timestamp(
                    LocalDateTime.of(2024, 1, 1, 10, 0) // Monday at 10:00
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .epochSecond,
                    0))
    val result1 = calendarViewModel.checkServiceRequestConflict(mondayRequest)
    assertFalse(result1.hasConflict)
    assertEquals("No conflicts", result1.reason)

    // Test early morning (before working hours)
    val earlyRequest =
        assignedRequest.copy(
            meetingDate =
                Timestamp(
                    LocalDateTime.of(2024, 1, 1, 8, 0) // Monday at 8:00
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .epochSecond,
                    0))
    val result2 = calendarViewModel.checkServiceRequestConflict(earlyRequest)
    assertTrue(result2.hasConflict)
    assertEquals("This time slot is outside your regular working hours", result2.reason)

    // Test off-time conflict
    val offTimeDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val offTimeSlot = TimeSlot(10, 0, 12, 0)
    testProvider.schedule.addException(offTimeDate, listOf(offTimeSlot), ExceptionType.OFF_TIME)
    providerFlow.value = listOf(testProvider) // Important: update flow
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }
    testDispatcher.scheduler.advanceUntilIdle() // Important: advance scheduler

    val result3 = calendarViewModel.checkServiceRequestConflict(mondayRequest)
    assertTrue(result3.hasConflict)
    assertEquals("This time slot conflicts with your off-time schedule", result3.reason)
  }

  @Test
  fun testCheckServiceRequestConflictComprehensive() {
    // Initialize with regular hours for Monday only
    val mondayHours = listOf(TimeSlot(9, 0, 17, 0))
    testProvider =
        Provider(
            uid = testUserId,
            schedule =
                Schedule(regularHours = mutableMapOf("MONDAY" to mondayHours.toMutableList())))
    providerFlow.value = listOf(testProvider) // Important: update flow
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }
    whenever(serviceRequestViewModel.acceptedRequests).thenReturn(MutableStateFlow(emptyList()))
    testDispatcher.scheduler.advanceUntilIdle() // Important: advance scheduler

    // Make sure the provider is initialized
    calendarViewModel.onDateSelected(LocalDate.of(2024, 1, 1))
    testDispatcher.scheduler.advanceUntilIdle()

    // Test within regular hours
    val mondayRequest =
        assignedRequest.copy(
            meetingDate =
                Timestamp(
                    LocalDateTime.of(2024, 1, 1, 10, 0) // Monday at 10:00
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .epochSecond,
                    0))
    val result1 = calendarViewModel.checkServiceRequestConflict(mondayRequest)
    assertFalse(result1.hasConflict)
    assertEquals("No conflicts", result1.reason)

    // Test early morning (before working hours)
    val earlyRequest =
        assignedRequest.copy(
            meetingDate =
                Timestamp(
                    LocalDateTime.of(2024, 1, 1, 8, 0) // Monday at 8:00
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .epochSecond,
                    0))
    val result2 = calendarViewModel.checkServiceRequestConflict(earlyRequest)
    assertTrue(result2.hasConflict)
    assertEquals("This time slot is outside your regular working hours", result2.reason)

    // Test off-time conflict
    val offTimeDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val offTimeSlot = TimeSlot(10, 0, 12, 0)
    testProvider.schedule.addException(offTimeDate, listOf(offTimeSlot), ExceptionType.OFF_TIME)
    providerFlow.value = listOf(testProvider) // Important: update flow
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }
    testDispatcher.scheduler.advanceUntilIdle() // Important: advance scheduler

    val result3 = calendarViewModel.checkServiceRequestConflict(mondayRequest)
    assertTrue(result3.hasConflict)
    assertEquals("This time slot conflicts with your off-time schedule", result3.reason)
  }

  @Test
  fun testServiceRequestsFiltering() = runTest {
    // Given a service request for a specific date
    val requestDate = LocalDateTime.of(2024, 1, 15, 10, 0)
    val serviceRequest =
        ServiceRequest(
            uid = "test_request",
            userId = "client1",
            providerId = testUserId,
            type = Services.TUTOR,
            status = ServiceRequestStatus.PENDING,
            meetingDate =
                Timestamp(Date.from(requestDate.atZone(ZoneId.systemDefault()).toInstant())))

    // Update the flow with our test request
    requestsFlow.value = listOf(serviceRequest)
    userFlow.value = mockUser

    // When getting time slots for that date
    calendarViewModel.onDateSelected(requestDate.toLocalDate())
    testDispatcher.scheduler.advanceUntilIdle()

    // Then the time slots should contain the service request
    val timeSlots = calendarViewModel.timeSlots.value
    assertTrue(timeSlots[requestDate.toLocalDate()]?.any { it.uid == serviceRequest.uid } == true)

    // When getting time slots for a different date
    val differentDate = requestDate.plusDays(1)
    calendarViewModel.onDateSelected(differentDate.toLocalDate())
    testDispatcher.scheduler.advanceUntilIdle()

    // Then the time slots should not contain the service request
    val differentTimeSlots = calendarViewModel.timeSlots.value
    assertFalse(
        differentTimeSlots[differentDate.toLocalDate()]?.any { it.uid == serviceRequest.uid } ==
            true)
  }

  @Test
  fun testCalendarViewManagement() = runTest {
    // Test initial state
    assertEquals(LocalDate.now(), calendarViewModel.selectedDate.value)
    assertEquals(LocalDate.now(), calendarViewModel.currentViewDate.value)
    assertEquals(CalendarView.WEEK, calendarViewModel.calendarView.value)
    assertTrue(calendarViewModel.timeSlots.value.isEmpty())
    assertFalse(calendarViewModel.isLoading.value)

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
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // When
    calendarViewModel.addOffTimeException(date, listOf(offTimeSlot)) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Schedule exception added successfully", feedbackMessage)
  }

  @Test
  fun testScheduleUpdates() = runTest {
    // Test schedule update through adding exceptions
    var successResult = false
    var feedbackMessage = ""

    // Test successful update
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    calendarViewModel.addExtraTimeException(
        LocalDateTime.of(2024, 1, 15, 0, 0), listOf(TimeSlot(17, 0, 19, 0))) { success, feedback ->
          successResult = success
          feedbackMessage = feedback
        }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(successResult)
    assertEquals("Schedule exception added successfully", feedbackMessage)

    // Test failed update
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
        }

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
    // Given a day and time slots
    val dayOfWeek = DayOfWeek.MONDAY.name
    val timeSlots =
        listOf(
            TimeSlot(startHour = 9, startMinute = 0, endHour = 12, endMinute = 0),
            TimeSlot(startHour = 14, startMinute = 0, endHour = 17, endMinute = 0))

    // Mock provider repository update
    whenever(providerRepository.updateProvider(any(), any(), any())).thenAnswer { invocation ->
      val provider = invocation.getArgument<Provider>(0)
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      testProvider = provider
      providerFlow.value = listOf(provider)
      onSuccess()
    }

    // Wait for the ViewModel to initialize
    testDispatcher.scheduler.advanceUntilIdle()

    // When setting regular hours
    var setHoursSuccess = false
    calendarViewModel.setRegularHours(dayOfWeek, timeSlots) { success -> setHoursSuccess = success }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue("Setting regular hours should succeed", setHoursSuccess)
    assertEquals(
        "Regular hours should be set correctly",
        timeSlots,
        calendarViewModel.currentProvider.value.schedule.regularHours[dayOfWeek])

    // When clearing regular hours
    var clearHoursSuccess = false
    calendarViewModel.clearRegularHours(dayOfWeek) { success -> clearHoursSuccess = success }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue("Clearing regular hours should succeed", clearHoursSuccess)
    assertTrue(
        "Regular hours should be empty after clearing",
        calendarViewModel.currentProvider.value.schedule.regularHours[dayOfWeek].isNullOrEmpty())

    // Verify provider repository was called
    verify(providerRepository, times(2)).updateProvider(any(), any(), any())
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
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // When
    calendarViewModel.updateException(date, timeSlots, ExceptionType.EXTRA_TIME) { success, feedback
      ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Schedule exception updated successfully", feedbackMessage)

    // Test failure case
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
        }

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
    // Set up test data
    val testDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val testSlot = TimeSlot(10, 0, 11, 0)
    val testException =
        ScheduleException(date = testDate, slots = listOf(testSlot), type = ExceptionType.OFF_TIME)

    // Create a schedule with the test exception
    val schedule =
        Schedule(regularHours = mutableMapOf(), exceptions = mutableListOf(testException))

    // Create a provider with this schedule
    val provider = testProvider.copy(schedule = schedule)
    providerFlow.value = listOf(provider)

    // Set up the view model
    calendarViewModel =
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)
    testDispatcher.scheduler.advanceUntilIdle()

    // Test getting exceptions for the test date
    val startDate = testDate
    val endDate = testDate.plusDays(1)
    val exceptions = calendarViewModel.getExceptions(startDate, endDate)

    assertEquals(1, exceptions.size, "Expected one exception in the date range")
    assertEquals(testException.type, exceptions[0].type, "Exception type should match")
    assertEquals(testException.timeSlots, exceptions[0].timeSlots, "Time slots should match")
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
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // Test add without merging
    calendarViewModel.addExtraTimeException(date, timeSlots) { _, feedback ->
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("Schedule exception added successfully", feedbackMessage)

    // Test add with merging
    val existingException = ScheduleException(date, timeSlots, ExceptionType.EXTRA_TIME)
    calendarViewModel.currentProvider.value.schedule.exceptions.add(existingException)

    calendarViewModel.addExtraTimeException(date, timeSlots) { _, feedback ->
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(feedbackMessage.contains("Schedule exception added and merged"))

    // Test failed operation
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
        }

    calendarViewModel.addExtraTimeException(date, timeSlots) { _, feedback ->
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testDateSelection() = runTest {
    // Given
    val testDate = LocalDateTime.of(2024, 2, 1, 0, 0)

    // When
    calendarViewModel.onDateSelected(testDate.toLocalDate())

    // Then
    assertEquals(testDate.toLocalDate(), calendarViewModel.selectedDate.value)
    assertEquals(testDate.toLocalDate(), calendarViewModel.currentViewDate.value)
  }

  @Test
  fun testViewDateChange() = runTest {
    // Given
    val initialDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val newDate = LocalDateTime.of(2024, 2, 1, 0, 0)

    // When - Set initial date
    calendarViewModel.onViewDateChanged(initialDate.toLocalDate())
    assertEquals(initialDate.toLocalDate(), calendarViewModel.currentViewDate.value)

    // When - Change view date
    calendarViewModel.onViewDateChanged(newDate.toLocalDate())

    // Then
    assertEquals(newDate.toLocalDate(), calendarViewModel.currentViewDate.value)
    // Selected date should remain unchanged
    assertNotEquals(newDate.toLocalDate(), calendarViewModel.selectedDate.value)
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
  fun `addAcceptedRequest calls repository`() = runTest {
    val mockRequest = mock<ServiceRequest>()

    calendarViewModel.addAcceptedRequest(mockRequest)

    verify(providerRepository).addAcceptedRequest(mockRequest)
  }

  @Test
  fun `removeAcceptedRequest calls repository`() = runTest {
    val mockRequest = mock<ServiceRequest>()

    calendarViewModel.removeAcceptedRequest(mockRequest)

    verify(providerRepository).removeAcceptedRequest(mockRequest)
  }

  @Test
  fun `startServiceRequestListener calls serviceRequestViewModel addListenerOnServiceRequests`() =
      runTest {
        calendarViewModel.startServiceRequestListener()

        verify(serviceRequestViewModel).addListenerOnServiceRequests(any())
      }

  @Test
  fun testUpdateExceptionFeedback() = runTest {
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
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // When
    calendarViewModel.updateException(date, timeSlots, ExceptionType.EXTRA_TIME) { success, feedback
      ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Schedule exception updated successfully", feedbackMessage)

    // Test failure case
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
        }

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
  fun testAddExtraTimeExceptionFeedback() = runTest {
    // Given
    val date = LocalDateTime.of(2024, 1, 15, 0, 0)
    val timeSlots = listOf(TimeSlot(14, 0, 18, 0))
    var successResult = false
    var feedbackMessage = ""

    // Mock successful provider update
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // When
    calendarViewModel.addExtraTimeException(date, timeSlots) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Schedule exception added successfully", feedbackMessage)

    // Test failure case
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
        }

    calendarViewModel.addExtraTimeException(date, timeSlots) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(successResult)
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testAddOffTimeExceptionFeedback() = runTest {
    // Given
    val date = LocalDateTime.of(2024, 1, 15, 0, 0)
    val timeSlots = listOf(TimeSlot(14, 0, 18, 0))
    var successResult = false
    var feedbackMessage = ""

    // Mock successful provider update
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }

    // When
    calendarViewModel.addOffTimeException(date, timeSlots) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    assertTrue(successResult)
    assertEquals("Schedule exception added successfully", feedbackMessage)

    // Test failure case
    whenever(
            providerRepository.updateProvider(
                any<Provider>(), any<() -> Unit>(), any<(Exception) -> Unit>()))
        .then { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Update failed"))
        }

    calendarViewModel.addOffTimeException(date, timeSlots) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertFalse(successResult)
    assertEquals("Failed to update schedule", feedbackMessage)
  }

  @Test
  fun testErrorHandlingDuringProviderUpdate() = runTest {
    // Simulate provider update failure
    whenever(providerRepository.updateProvider(any(), any(), any())).then { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(Exception("Update failed"))
    }

    // Try to update schedule
    val newSlot = TimeSlot(9, 0, 17, 0)
    calendarViewModel.setRegularHours(DayOfWeek.MONDAY.name, listOf(newSlot)) {
      // onComplete callback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that the provider was not updated
    assertEquals(
        emptyMap<String, List<TimeSlot>>(),
        calendarViewModel.currentProvider.value.schedule.regularHours)
  }

  @Test
  fun testEdgeCaseScheduleManagement() = runTest {
    // Test overlapping time slots
    val slot1 = TimeSlot(9, 0, 12, 0)
    val slot2 = TimeSlot(11, 0, 14, 0)
    val slot3 = TimeSlot(13, 0, 17, 0)

    // Mock successful provider update
    whenever(providerRepository.updateProvider(any(), any(), any())).then { invocation ->
      val provider = invocation.getArgument<Provider>(0)
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      testProvider = provider
      providerFlow.value = listOf(provider)
      onSuccess()
    }

    calendarViewModel.setRegularHours(DayOfWeek.MONDAY.name, listOf(slot1, slot2, slot3)) {
      // onComplete callback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that overlapping slots are handled
    val updatedSlots =
        calendarViewModel.currentProvider.value.schedule.regularHours[DayOfWeek.MONDAY.name]
    assertNotNull(updatedSlots)
    assertEquals(3, updatedSlots?.size)
  }

  @Test
  fun testStateManagementDuringProviderUpdates() = runTest {
    // Initial state
    assertEquals(false, calendarViewModel.isLoading.value)

    // Simulate slow provider update
    var updateCompleted = false
    whenever(providerRepository.updateProvider(any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      testDispatcher.scheduler.advanceTimeBy(1000) // Simulate delay
      updateCompleted = true
      onSuccess()
    }

    // Update provider
    val newSlot = TimeSlot(9, 0, 17, 0)
    calendarViewModel.setRegularHours(DayOfWeek.MONDAY.name, listOf(newSlot)) {
      // onComplete callback
    }

    // Verify loading state
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(updateCompleted)
  }

  @Test
  fun testExceptionHandlingWithInvalidTimeSlots() = runTest {
    var exceptionThrown = false
    try {
      // Attempt to create an invalid time slot
      TimeSlot(17, 0, 9, 0)
    } catch (e: IllegalArgumentException) {
      exceptionThrown = true
      assertEquals("Start time must be before end time", e.message)
    }
    assertTrue("Expected IllegalArgumentException for invalid time slot", exceptionThrown)

    // Test with valid time slot but outside regular hours
    val validSlot = TimeSlot(20, 0, 22, 0)

    calendarViewModel.setRegularHours(DayOfWeek.MONDAY.name, listOf(validSlot)) {
      // onComplete callback
    }
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that slot was not added since it's outside normal business hours
    val updatedSlots =
        calendarViewModel.currentProvider.value.schedule.regularHours[DayOfWeek.MONDAY.name]
    assertTrue(updatedSlots.isNullOrEmpty())
  }

  @Test
  fun testLogMessages() {
    // Test log messages for various error scenarios
    var successResult = false
    var feedbackMessage = ""

    // Test repository failure first
    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("Test failure"))
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // Need to use a valid time slot to get past initial validation
    calendarViewModel.addException(
        testDateTime, listOf(TimeSlot(10, 0, 11, 0)), ExceptionType.OFF_TIME) { success, feedback ->
          successResult = success
          feedbackMessage = feedback
        }
    testDispatcher.scheduler.advanceUntilIdle() // Important: advance the scheduler
    assertFalse(successResult)
    assertEquals("Failed to update schedule", feedbackMessage)

    // Reset mock behavior and provider state
    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    testProvider = Provider(uid = testUserId)
    providerFlow.value = listOf(testProvider)
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }

    // Test invalid time slots
    calendarViewModel.addException(testDateTime, emptyList(), ExceptionType.OFF_TIME) {
        success,
        feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    assertFalse(successResult)
    assertEquals("No time slots provided", feedbackMessage)
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

    // Configure mock to succeed and reset provider state
    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // Reset provider state
    testProvider = Provider(uid = testUserId)
    providerFlow.value = listOf(testProvider)
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
    }

    // Test successful addition
    val timeSlot = TimeSlot(10, 0, 11, 0)
    calendarViewModel.addException(testDateTime, listOf(timeSlot), ExceptionType.OFF_TIME) {
        success,
        feedback ->
      successResult = success
      feedbackMessage = feedback
    }
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(successResult)
    assertEquals("Schedule exception added successfully", feedbackMessage)
  }
}
