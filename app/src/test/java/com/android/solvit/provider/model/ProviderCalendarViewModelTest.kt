package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.*
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")

  private lateinit var mockUser: User
  private lateinit var userFlow: MutableStateFlow<User?>
  private lateinit var requestsFlow: MutableStateFlow<List<ServiceRequest>>
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var calendarViewModel: ProviderCalendarViewModel

  private val testDispatcher = StandardTestDispatcher()

  private val testDateTime = LocalDateTime.of(2024, 1, 8, 10, 0)
  private val testDate = testDateTime.toLocalDate()

  private val assignedRequest =
      ServiceRequest(
          uid = "request1",
          title = "Fix leaky faucet",
          type = Services.PLUMBER,
          description = "Test request",
          userId = "seeker1",
          providerId = testUserId,
          dueDate = Timestamp(testDateTime.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDateTime.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = 100.0,
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    mockUser = mock()
    whenever(mockUser.uid).thenReturn(testUserId)

    userFlow = MutableStateFlow(mockUser)
    authViewModel = mock()
    whenever(authViewModel.user).thenReturn(userFlow)

    serviceRequestViewModel = mock()
    requestsFlow = MutableStateFlow(listOf(assignedRequest))
    whenever(serviceRequestViewModel.requests).thenReturn(requestsFlow)

    // Mock getServiceRequests to do nothing since we control the flow directly
    doNothing().whenever(serviceRequestViewModel).getServiceRequests()

    calendarViewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)

    // Initialize with current date
    calendarViewModel.onViewDateChanged(testDate)
    testDispatcher.scheduler.advanceUntilIdle()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testServiceRequestsAssignedOnly() = runTest {
    // Initialize with one request
    requestsFlow.value = listOf(assignedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    val requests = timeSlots[testDate] ?: emptyList()
    assertEquals(1, requests.size)
    assertEquals(assignedRequest, requests.first())
  }

  @Test
  fun testFilterUnassignedRequests() = runTest {
    val unassignedRequest = assignedRequest.copy(uid = "request2", providerId = null)
    requestsFlow.value = listOf(unassignedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    assertTrue(timeSlots.isEmpty())
  }

  @Test
  fun testFilterOtherProvidersRequests() = runTest {
    val otherProviderRequest =
        assignedRequest.copy(uid = "request3", providerId = "other_provider_id")
    requestsFlow.value = listOf(otherProviderRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    assertTrue(timeSlots.isEmpty())
  }

  @Test
  fun testNoUserLoggedIn() = runTest {
    userFlow.value = null
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    assertTrue(timeSlots.isEmpty())
  }

  @Test
  fun testUpdateServiceRequestStatus() = runTest {
    // Set initial request
    requestsFlow.value = listOf(assignedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    // Update status
    val updatedRequest = assignedRequest.copy(status = ServiceRequestStatus.COMPLETED)
    requestsFlow.value = listOf(updatedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    val requests = timeSlots[testDate] ?: emptyList()
    assertEquals(1, requests.size)
    assertEquals(ServiceRequestStatus.COMPLETED, requests.first().status)
  }

  @Test
  fun testCalendarViewNavigation() = runTest {
    // Set initial request to ensure we have data
    requestsFlow.value = listOf(assignedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(CalendarView.WEEK, calendarViewModel.calendarView.first())

    calendarViewModel.onCalendarViewChanged(CalendarView.MONTH)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(CalendarView.MONTH, calendarViewModel.calendarView.first())

    calendarViewModel.onCalendarViewChanged(CalendarView.DAY)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(CalendarView.DAY, calendarViewModel.calendarView.first())
  }

  @Test
  fun testDateNavigation() = runTest {
    // Set initial request
    requestsFlow.value = listOf(assignedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    val initialDate = calendarViewModel.currentViewDate.first()
    val newDate = initialDate.plusDays(7)

    calendarViewModel.onViewDateChanged(newDate)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(newDate, calendarViewModel.currentViewDate.first())
  }

  @Test
  fun testTimeSlotSorting() = runTest {
    val earlyRequest =
        assignedRequest.copy(
            uid = "request6",
            meetingDate =
                Timestamp(testDateTime.withHour(8).toInstant(ZoneOffset.UTC).epochSecond, 0))
    val lateRequest =
        assignedRequest.copy(
            uid = "request7",
            meetingDate =
                Timestamp(testDateTime.withHour(18).toInstant(ZoneOffset.UTC).epochSecond, 0))

    requestsFlow.value = listOf(lateRequest, earlyRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    val requests = timeSlots[testDate] ?: emptyList()
    assertEquals(2, requests.size)
    assertEquals(listOf(earlyRequest, lateRequest), requests.sortedBy { it.meetingDate })
  }

  @Test
  fun testRequestStatusTransitions() = runTest {
    val statuses =
        listOf(
            ServiceRequestStatus.PENDING,
            ServiceRequestStatus.ACCEPTED,
            ServiceRequestStatus.SCHEDULED,
            ServiceRequestStatus.COMPLETED,
            ServiceRequestStatus.CANCELED,
            ServiceRequestStatus.ARCHIVED)

    for (status in statuses) {
      val updatedRequest = assignedRequest.copy(status = status)
      requestsFlow.value = listOf(updatedRequest)
      testDispatcher.scheduler.advanceUntilIdle()

      val timeSlots = calendarViewModel.timeSlots.first()
      val requests = timeSlots[testDate] ?: emptyList()
      assertEquals(1, requests.size)
      assertEquals(status, requests.first().status)
    }
  }

  @Test
  fun testConcurrentRequestsHandling() = runTest {
    val requests =
        List(5) { index ->
          assignedRequest.copy(
              uid = "request_$index",
              title = "Concurrent Request $index",
              meetingDate = Timestamp(testDateTime.toInstant(ZoneOffset.UTC).epochSecond, 0))
        }

    requestsFlow.value = requests
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    val dayRequests = timeSlots[testDate] ?: emptyList()
    assertEquals(5, dayRequests.size)
    assertEquals(1, timeSlots.size) // All requests should be grouped under the same date
  }

  @Test
  fun testDateSelection() = runTest {
    val newDate = testDate.plusDays(5)
    calendarViewModel.onDateSelected(newDate)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(newDate, calendarViewModel.selectedDate.first())
    assertEquals(newDate, calendarViewModel.currentViewDate.first())
  }

  @Test
  fun testMonthViewDateRange() = runTest {
    // Set to month view
    calendarViewModel.onCalendarViewChanged(CalendarView.MONTH)
    val monthStart = testDate.withDayOfMonth(1)
    calendarViewModel.onViewDateChanged(monthStart)
    testDispatcher.scheduler.advanceUntilIdle()

    // Create requests for start, middle and end of month
    val requests =
        listOf(
            assignedRequest.copy(
                uid = "start",
                meetingDate =
                    Timestamp(monthStart.atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond, 0)),
            assignedRequest.copy(
                uid = "middle",
                meetingDate =
                    Timestamp(
                        monthStart
                            .plusDays(15)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                            .epochSecond,
                        0)),
            assignedRequest.copy(
                uid = "end",
                meetingDate =
                    Timestamp(
                        monthStart
                            .plusMonths(1)
                            .minusDays(1)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                            .epochSecond,
                        0)))

    requestsFlow.value = requests
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    assertEquals(3, timeSlots.values.flatten().size)
  }

  @Test
  fun testWeekViewDateRange() = runTest {
    // Set to week view
    calendarViewModel.onCalendarViewChanged(CalendarView.WEEK)
    val monday = testDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    calendarViewModel.onViewDateChanged(monday)
    testDispatcher.scheduler.advanceUntilIdle()

    // Create requests for Monday, Wednesday and Sunday
    val requests =
        listOf(
            assignedRequest.copy(
                uid = "monday",
                meetingDate =
                    Timestamp(monday.atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond, 0)),
            assignedRequest.copy(
                uid = "wednesday",
                meetingDate =
                    Timestamp(
                        monday.plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond,
                        0)),
            assignedRequest.copy(
                uid = "sunday",
                meetingDate =
                    Timestamp(
                        monday.plusDays(6).atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond,
                        0)))

    requestsFlow.value = requests
    testDispatcher.scheduler.advanceUntilIdle()

    val timeSlots = calendarViewModel.timeSlots.first()
    assertEquals(3, timeSlots.values.flatten().size)
  }

  @Test
  fun testServiceRequestClick() = runTest {
    calendarViewModel.onServiceRequestClick(assignedRequest)
    verify(serviceRequestViewModel).selectRequest(assignedRequest)
  }

  @Test
  fun testLoadingStateTransitions() = runTest {
    // Set up a delayed response from the service request view model
    val delayedRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    whenever(serviceRequestViewModel.requests).thenReturn(delayedRequests)

    // Keep the existing user flow from setUp
    whenever(authViewModel.user).thenReturn(userFlow)

    // Create new viewModel to use delayed requests
    calendarViewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)

    // Set the view date to match our test request date
    calendarViewModel.onViewDateChanged(testDate)
    testDispatcher.scheduler.advanceUntilIdle()

    // Emit requests to trigger processing
    delayedRequests.value = listOf(assignedRequest)
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify loading completed
    assertFalse(calendarViewModel.isLoading.first())

    // Verify requests were processed
    val timeSlots = calendarViewModel.timeSlots.first()
    assertEquals(1, timeSlots.size)
    assertEquals(listOf(assignedRequest), timeSlots[testDate])
  }

  @Test
  fun testRequestProcessing() = runTest {
    // Test the complete flow of request processing
    val requestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    whenever(serviceRequestViewModel.requests).thenReturn(requestsFlow)

    // Keep the existing user flow from setUp
    whenever(authViewModel.user).thenReturn(userFlow)

    calendarViewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)
    testDispatcher.scheduler.advanceUntilIdle()

    // Initial state
    assertTrue(calendarViewModel.timeSlots.first().isEmpty())

    // Add requests during different view types
    val viewTypes = listOf(CalendarView.DAY, CalendarView.WEEK, CalendarView.MONTH)
    val currentDate = LocalDate.now()

    for (view in viewTypes) {
      calendarViewModel.onCalendarViewChanged(view)
      calendarViewModel.onViewDateChanged(currentDate)
      testDispatcher.scheduler.advanceUntilIdle()

      // Add requests for current date and next day
      val testRequests =
          listOf(
              assignedRequest.copy(
                  uid = "request1_${view.name}",
                  meetingDate =
                      Timestamp(
                          currentDate.atStartOfDay().toInstant(ZoneOffset.UTC).epochSecond, 0)),
              assignedRequest.copy(
                  uid = "request2_${view.name}",
                  meetingDate =
                      Timestamp(
                          currentDate
                              .plusDays(1)
                              .atStartOfDay()
                              .toInstant(ZoneOffset.UTC)
                              .epochSecond,
                          0)))

      requestsFlow.value = testRequests
      testDispatcher.scheduler.advanceUntilIdle()

      // Verify requests are processed according to view type
      val timeSlots = calendarViewModel.timeSlots.first()
      val expectedDates =
          when (view) {
            CalendarView.DAY -> 1 // Only current date visible
            CalendarView.WEEK -> 2 // Both dates visible in week view
            CalendarView.MONTH -> 2 // Both dates visible in month view
          }
      assertEquals(expectedDates, timeSlots.size)
    }
  }
}
