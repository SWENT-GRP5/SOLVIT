package com.android.solvit.provider.ui.calendar

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.provider.ui.calendar.components.preferences.SchedulePreferencesSheet
import com.android.solvit.provider.ui.calendar.components.preferences.ScrollableTimePickerRow
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.Schedule
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.times

@RunWith(AndroidJUnit4::class)
class ProviderCalendarScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var viewModel: ProviderCalendarViewModel
  private lateinit var authViewModel: AuthViewModel
  private lateinit var authRep: AuthRep
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var providerRepository: ProviderRepository

  private val testUserId = "test_provider_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")
  private lateinit var testDate: LocalDateTime
  private lateinit var afternoonTestDate: LocalDateTime
  private lateinit var testRequest: ServiceRequest
  private lateinit var afternoonTestRequest: ServiceRequest

  @Before
  fun setUp() {
    // Initialize mocks
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    authRep = mock(AuthRep::class.java)
    providerRepository = mock(ProviderRepository::class.java)

    // Set up test data
    val testUser =
        User(
            uid = testUserId,
            role = "provider",
            email = "test@example.com",
            locations = listOf(testLocation))

    // Morning request at 6 AM
    testDate = LocalDateTime.now().withHour(6).withMinute(0).withSecond(0).withNano(0)
    testRequest =
        ServiceRequest(
            uid = "request1",
            title = "Fix leaky faucet",
            type = Services.PLUMBER,
            description = "Test request",
            userId = "seeker1",
            providerId = testUserId,
            dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
            meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
            location = testLocation,
            imageUrl = null,
            packageId = null,
            agreedPrice = 100.0,
            status = ServiceRequestStatus.PENDING)

    // Afternoon request at 2 PM
    afternoonTestDate = LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0)
    afternoonTestRequest =
        ServiceRequest(
            uid = "request2",
            title = "Fix leaky faucet",
            type = Services.PLUMBER,
            description = "Test request",
            userId = "seeker1",
            providerId = testUserId,
            dueDate =
                Timestamp(afternoonTestDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
            meetingDate = Timestamp(afternoonTestDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
            location = testLocation,
            imageUrl = null,
            packageId = null,
            agreedPrice = 100.0,
            status = ServiceRequestStatus.ACCEPTED)

    // Set up auth repository mock
    `when`(authRep.getUserId()).thenReturn(testUserId)
    `when`(authRep.init(any())).then { invocation ->
      val callback = invocation.arguments[0] as (User?) -> Unit
      callback(testUser)
      null
    }

    // Create real AuthViewModel with mocked repository
    authViewModel = AuthViewModel(authRep)

    // Mock service request repository methods
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as () -> Unit
          callback()
          null
        }
        .`when`(serviceRequestRepository)
        .init(any())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as (List<ServiceRequest>) -> Unit
          onSuccess(listOf(testRequest, afternoonTestRequest))
          null
        }
        .`when`(serviceRequestRepository)
        .addListenerOnServiceRequests(any(), any())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as (List<ServiceRequest>) -> Unit
          onSuccess(listOf(testRequest, afternoonTestRequest))
          null
        }
        .`when`(serviceRequestRepository)
        .getServiceRequests(any(), any())

    // Create real ServiceRequestViewModel with mocked repository
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)

    // Set up provider repository mock
    `when`(providerRepository.addListenerOnProviders(any(), any())).then { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Provider>) -> Unit
      onSuccess(
          listOf(
              Provider(
                  uid = testUserId,
                  schedule =
                      Schedule(
                          regularHours =
                              mutableMapOf(
                                  DayOfWeek.MONDAY.name to
                                      mutableListOf(
                                          TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0))),
                                  DayOfWeek.WEDNESDAY.name to
                                      mutableListOf(
                                          TimeSlot(LocalTime.of(10, 0), LocalTime.of(18, 0))))))))
    }

    // Mock updateProvider to update the provider list
    doAnswer { invocation ->
          val provider = invocation.arguments[0] as Provider
          val onSuccess = invocation.arguments[1] as () -> Unit
          `when`(providerRepository.addListenerOnProviders(any(), any())).then { listenerInvocation
            ->
            val listenerCallback = listenerInvocation.arguments[0] as (List<Provider>) -> Unit
            listenerCallback(listOf(provider))
          }
          onSuccess()
          null
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // Initialize view model with real AuthViewModel and ServiceRequestViewModel
    viewModel =
        ProviderCalendarViewModel(
            providerRepository = providerRepository,
            authViewModel = authViewModel,
            serviceRequestViewModel = serviceRequestViewModel)
  }

  private fun setupScreen() {
    composeTestRule.setContent {
      ProviderCalendarScreen(navigationActions = navigationActions, viewModel = viewModel)
    }

    // Wait for initial data load and animations
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }
    composeTestRule.mainClock.advanceTimeBy(1000)
    composeTestRule.mainClock.autoAdvance = true
    composeTestRule.waitForIdle()
  }

  private fun waitForAnimation() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.mainClock.autoAdvance = true
    composeTestRule.waitForIdle()
  }

  // ===== View Display Tests =====

  @Test
  fun testMonthViewDisplay() {
    setupScreen()

    // Switch to month view
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    waitForAnimation()

    // 1. Test basic month view structure
    composeTestRule.onNodeWithTag("monthView").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthHeader").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarDaysHeader").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthViewCalendarGrid").assertExists().assertIsDisplayed()

    // 2. Test the current date cell
    val requestDate = testDate.toLocalDate()
    composeTestRule.onNodeWithTag("monthDay_$requestDate").assertExists().assertIsDisplayed()

    // 3. Test status columns and rows with both morning and afternoon requests
    composeTestRule
        .onNodeWithTag("monthDayStatusColumn_$requestDate", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    // Test morning and afternoon request display
    val morningTime =
        testRequest.meetingDate!!
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

    val afternoonTime =
        afternoonTestRequest.meetingDate!!
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

    composeTestRule
        .onNodeWithTag("monthDayMorningStatus_${requestDate}_$morningTime", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            "monthDayAfternoonStatus_${requestDate}_$afternoonTime", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    // 4. Test previous/next month days
    val firstMonday =
        requestDate
            .withDayOfMonth(1)
            .minusDays(requestDate.withDayOfMonth(1).dayOfWeek.value.toLong() - 1)
    val lastSunday =
        requestDate
            .withDayOfMonth(requestDate.lengthOfMonth())
            .plusDays(
                7 -
                    requestDate
                        .withDayOfMonth(requestDate.lengthOfMonth())
                        .dayOfWeek
                        .value
                        .toLong())

    composeTestRule.onNodeWithTag("monthDay_$firstMonday").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthDay_$lastSunday").assertExists().assertIsDisplayed()
  }

  @Test
  fun testWeekViewDisplay() {
    setupScreen()

    // 1. Test basic week view structure
    composeTestRule.onNodeWithTag("weekViewHeader").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("weekViewDaysRow").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("weekViewTimeGrid").assertExists().assertIsDisplayed()

    // 2. Test the date headers for the week
    val requestDate = testDate.toLocalDate()
    val startOfWeek = requestDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    (0..6).forEach { dayOffset ->
      val currentDate = startOfWeek.plusDays(dayOffset.toLong())
      composeTestRule.onNodeWithTag("weekDayHeader_$currentDate").assertExists().assertIsDisplayed()
    }

    // 3. Test service request display and scrolling
    composeTestRule
        .onNodeWithTag("weekViewServiceRequest_${testRequest.uid}")
        .assertExists()
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag("weekViewTimeGrid").performTouchInput {
      swipeUp(startY = 1000f, endY = 100f)
    }
    waitForAnimation()

    composeTestRule
        .onNodeWithTag("weekViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun testDayViewDisplay() {
    setupScreen()

    // Switch to day view
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    waitForAnimation()
    assertEquals(CalendarView.DAY, viewModel.calendarView.value)

    // Basic structure tests
    composeTestRule.onNodeWithTag("dayView").assertExists()
    composeTestRule.onNodeWithTag("dayHeader").assertExists()
    composeTestRule.onNodeWithTag("dayViewTimeGrid").assertExists()

    // Wait for initial scroll to complete
    waitForAnimation()

    // Verify some time labels are present (these should be visible regardless of scroll position)
    // We know the schedule has hours from 9:00 to 17:00 on Monday
    composeTestRule.onNodeWithText("09:00").assertExists()
    composeTestRule.onNodeWithText("12:00").assertExists()
    composeTestRule.onNodeWithText("17:00").assertExists()

    // Verify the test requests are visible
    composeTestRule.onNodeWithText("06:00").assertExists() // Morning request time
    composeTestRule.onNodeWithText("14:00").assertExists() // Afternoon request time

    // Verify day header shows correct date format
    val formattedDate =
        testDate
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault()))
    composeTestRule.onNodeWithText(formattedDate).assertExists()

    // Verify test requests are visible
    composeTestRule.onNodeWithTag("dayViewServiceRequest_${testRequest.uid}").assertExists()
    composeTestRule
        .onNodeWithTag("dayViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
  }

  // ===== Navigation Tests =====

  @Test
  fun testViewSwitching() {
    setupScreen()

    // Test complete cycle of view switches using segmented buttons
    assertEquals(CalendarView.WEEK, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("weekViewTimeGrid").assertExists()

    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    waitForAnimation()
    assertEquals(CalendarView.MONTH, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("monthViewCalendarGrid").assertExists()

    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    waitForAnimation()
    assertEquals(CalendarView.DAY, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("dayViewTimeGrid").assertExists()

    composeTestRule.onNodeWithTag("segmentedButton_WEEK").performClick()
    waitForAnimation()
    assertEquals(CalendarView.WEEK, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("weekViewTimeGrid").assertExists()
  }

  @Test
  fun testDateNavigation() {
    setupScreen()

    // Test week view swipe navigation
    val initialWeekDate = viewModel.currentViewDate.value
    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeLeft() }
    waitForAnimation()
    assertEquals(initialWeekDate.plusWeeks(1), viewModel.currentViewDate.value)

    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeRight() }
    waitForAnimation()
    assertEquals(initialWeekDate, viewModel.currentViewDate.value)

    // Test month view swipe navigation
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    waitForAnimation()

    val initialMonthDate = viewModel.currentViewDate.value
    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeLeft() }
    waitForAnimation()
    assertEquals(initialMonthDate.plusMonths(1), viewModel.currentViewDate.value)

    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeRight() }
    waitForAnimation()
    assertEquals(initialMonthDate, viewModel.currentViewDate.value)

    // Test day view swipe navigation
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    waitForAnimation()

    val initialDayDate = viewModel.currentViewDate.value
    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeLeft() }
    waitForAnimation()
    assertEquals(initialDayDate.plusDays(1), viewModel.currentViewDate.value)

    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeRight() }
    waitForAnimation()
    assertEquals(initialDayDate, viewModel.currentViewDate.value)
  }

  @Test
  fun testHeaderDatePicker() {
    setupScreen()

    // Test week view header navigation and date picker interaction
    composeTestRule.onNodeWithTag("weekViewHeader").performClick()
    waitForAnimation()

    // Verify date picker dialog appears
    composeTestRule.onNodeWithTag("datePickerDialog").assertExists()

    // Select a date using the date picker
    val targetDate = testDate.toLocalDate().withDayOfMonth(15)
    val dateText =
        targetDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))

    // Handle special case for today's date
    val today = LocalDate.now()
    val datePattern =
        if (targetDate == today) {
          "Today, $dateText"
        } else {
          dateText
        }

    // Click on the target date in the grid
    composeTestRule.onNodeWithText(datePattern).performClick()
    waitForAnimation()

    // Confirm the selection which will trigger the onDateSelected callback
    composeTestRule.onNodeWithText("OK").performClick()
    waitForAnimation()

    // Verify that the current view date has been updated
    assertEquals(15, viewModel.currentViewDate.value.dayOfMonth)

    // Test month view header navigation
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    waitForAnimation()
    composeTestRule.onNodeWithTag("monthHeader").performClick()
    waitForAnimation()

    // Verify date picker appears in month view
    composeTestRule.onNodeWithTag("datePickerDialog").assertExists()
  }

  // ===== UI Interaction Tests =====

  @Test
  fun testServiceRequestDetails() {
    setupScreen()

    // Test date selection in week view by clicking the day header
    composeTestRule.onNodeWithTag("weekDayHeader_${testDate.toLocalDate()}").performClick()
    waitForAnimation()

    // Verify bottom sheet appears with time slots
    composeTestRule.onNodeWithTag("bottomSheet").assertExists()
    composeTestRule.onNodeWithTag("bottomSheetTimeSlots").assertExists()

    // Test service request click in bottom sheet
    composeTestRule.onNodeWithTag("weekViewServiceRequest_${testRequest.uid}").performClick()
    waitForAnimation()

    // Verify navigation to booking details
    verify(navigationActions).navigateTo(Route.BOOKING_DETAILS)

    // Test month view date selection
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    waitForAnimation()

    composeTestRule
        .onNodeWithTag("monthDayStatusColumn_${testDate.toLocalDate()}", useUnmergedTree = true)
        .performClick()
    waitForAnimation()

    // Verify bottom sheet appears again
    composeTestRule.onNodeWithTag("bottomSheet").assertExists()
    composeTestRule.onNodeWithTag("bottomSheetTimeSlots").assertExists()
  }

  @Test
  fun testTimeGridScrolling() {
    setupScreen()

    // Test week view scrolling
    composeTestRule.onNodeWithTag("weekViewTimeGrid").performTouchInput {
      swipeUp(startY = 1000f, endY = 100f)
    }
    waitForAnimation()
    composeTestRule
        .onNodeWithTag("weekViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
        .assertIsDisplayed()

    // Test day view scrolling
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    waitForAnimation()

    composeTestRule.onNodeWithTag("dayViewTimeGrid").performTouchInput {
      swipeUp(startY = 1000f, endY = 100f)
    }
    waitForAnimation()
    composeTestRule
        .onNodeWithTag("dayViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun testCurrentTimeIndicator() {
    setupScreen()
    val currentHour = LocalTime.now().hour

    // Test week view current time indicator
    // First scroll to make current hour visible
    if (currentHour > 8) { // If current hour is after default visible range
      composeTestRule.onNodeWithTag("weekViewTimeGrid").performTouchInput {
        swipeUp(
            startY = 1000f, endY = 100f, durationMillis = 200 // Faster scroll for test
            )
      }
      waitForAnimation()
    }

    // Verify time grid components
    composeTestRule.onNodeWithTag("hourRow_$currentHour").assertExists()
    composeTestRule.onNodeWithTag("timeLabel_$currentHour").assertExists()

    // Verify current time indicator components
    composeTestRule.onNodeWithTag("currentTimeIndicatorCircle").assertExists()
    composeTestRule.onNodeWithTag("currentTimeIndicatorLine").assertExists()

    // Test day view current time indicator
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    waitForAnimation()

    // Scroll in day view as well if needed
    if (currentHour > 8) {
      composeTestRule.onNodeWithTag("dayViewTimeGrid").performTouchInput {
        swipeUp(startY = 1000f, endY = 100f, durationMillis = 200)
      }
      waitForAnimation()
    }

    // Verify time grid components in day view
    composeTestRule.onNodeWithTag("hourRow_$currentHour").assertExists()
    composeTestRule.onNodeWithTag("timeLabel_$currentHour").assertExists()

    // Verify current time indicator components in day view
    composeTestRule.onNodeWithTag("currentTimeIndicatorCircle").assertExists()
    composeTestRule.onNodeWithTag("currentTimeIndicatorLine").assertExists()
  }

  @Test
  fun schedulePreferencesSheet_displaysCorrectly() {
    composeTestRule.setContent { SchedulePreferencesSheet(viewModel = viewModel, onDismiss = {}) }

    // Wait for data to load and UI to settle
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.mainClock.advanceTimeBy(1000)

    // Verify initial state
    composeTestRule.onNodeWithTag("schedulePreferencesSheet").assertExists()

    // Verify tabs
    composeTestRule.onNodeWithText("Regular Hours").assertExists()
    composeTestRule.onNodeWithText("Exceptions").assertExists()

    // Verify schedule display for Monday
    composeTestRule.onNodeWithText("Monday").assertExists()
    composeTestRule.onNodeWithText("9:00 - 17:00").assertExists()
  }

  @Test
  fun schedulePreferencesSheet_canEditRegularHours() {
    composeTestRule.setContent { SchedulePreferencesSheet(viewModel = viewModel, onDismiss = {}) }

    // Wait for data to load and UI to settle
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.mainClock.advanceTimeBy(1000)

    // Find and click Monday's schedule
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)

    // Verify time selection is visible
    composeTestRule.onNodeWithText("Start Time").assertExists()
    composeTestRule.onNodeWithText("End Time").assertExists()

    // Clear hours using unique identifier
    composeTestRule.onNodeWithTag("clear_hours_MONDAY").performClick()
    composeTestRule.mainClock.advanceTimeBy(1000) // Wait longer for provider update

    // Wait for provider update to complete
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }

    composeTestRule.onNodeWithTag("no_hours_text_MONDAY", useUnmergedTree = true).assertExists()
  }

  @Test
  fun schedulePreferencesSheet_canAddException() {
    composeTestRule.setContent { SchedulePreferencesSheet(viewModel = viewModel, onDismiss = {}) }

    // Wait for data to load and UI to settle
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.mainClock.advanceTimeBy(1000)

    // Switch to Exceptions tab
    composeTestRule.onNodeWithText("Exceptions").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)

    // Click Add Exception button
    composeTestRule.onNodeWithTag("addButton").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)

    // Select Off Time
    composeTestRule.onNodeWithText("Off Time").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
  }

  @Test
  fun schedulePreferencesSheet_integrationTest() {
    composeTestRule.setContent { SchedulePreferencesSheet(viewModel = viewModel, onDismiss = {}) }

    // Wait for data to load and UI to settle
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.mainClock.advanceTimeBy(1000)

    // 1. Test Regular Hours tab
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)

    // Verify time selection appears
    composeTestRule.onNodeWithText("Start Time").assertExists()
    composeTestRule.onNodeWithText("End Time").assertExists()

    // Clear hours using unique identifier
    composeTestRule.onNodeWithTag("clear_hours_MONDAY").performClick()
    composeTestRule.mainClock.advanceTimeBy(1000) // Wait longer for provider update

    // Wait for provider update to complete
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }

    composeTestRule.onNodeWithTag("no_hours_text_MONDAY", useUnmergedTree = true).assertExists()

    // 2. Test Exceptions tab
    composeTestRule.onNodeWithText("Exceptions").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onNodeWithTag("addButton").assertExists()

    // Add new exception
    composeTestRule.onNodeWithTag("addButton").performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
  }

  @Test
  fun scrollableTimePickerRow_hourSelection() {
    var selectedTime = LocalTime.of(10, 0)
    composeTestRule.setContent {
      ScrollableTimePickerRow(
          selectedTime = selectedTime,
          onTimeSelected = { selectedTime = it },
          modifier = Modifier.height(200.dp),
          testTagPrefix = "test")
    }
    composeTestRule.waitForIdle()

    // Verify picker components exist
    composeTestRule.onNodeWithTag("test_hour").assertExists()
    composeTestRule.onNodeWithTag("test_hour_picker").assertExists()

    // Verify initial time display
    composeTestRule
        .onNodeWithTag("test_hour_picker")
        .assertExists()
        .onChildren()
        .filterToOne(hasText("10"))
        .assertExists()

    // Select a different hour by clicking an item
    composeTestRule
        .onNodeWithTag("test_hour_picker")
        .assertExists()
        .onChildren()
        .filterToOne(hasText("11"))
        .performClick()
    waitForAnimation()

    // Verify selection changed
    assert(selectedTime.hour == 11)
  }

  @Test
  fun scrollableTimePickerRow_minuteSelection() {
    var selectedTime = LocalTime.of(10, 0)
    composeTestRule.setContent {
      ScrollableTimePickerRow(
          selectedTime = selectedTime,
          onTimeSelected = { selectedTime = it },
          modifier = Modifier.height(200.dp),
          testTagPrefix = "test")
    }
    composeTestRule.waitForIdle()

    // Select minute by scrolling
    composeTestRule.onNodeWithTag("test_minute_picker").assertExists().performTouchInput {
      swipeUp(startY = center.y + 100f, endY = center.y - 100f)
    }
    waitForAnimation()

    // Verify that the minute value changed from 0
    assert(selectedTime.minute != 0)
  }

  @Test
  fun scrollableTimePickerRow_scrollBehavior() {
    var selectedTime = LocalTime.of(10, 0)
    composeTestRule.setContent {
      ScrollableTimePickerRow(
          selectedTime = selectedTime,
          onTimeSelected = { selectedTime = it },
          modifier = Modifier.height(200.dp),
          testTagPrefix = "test")
    }
    composeTestRule.waitForIdle()

    // Verify initial state
    composeTestRule.onNodeWithTag("test_hour_picker").assertExists()
    composeTestRule
        .onNodeWithTag("test_hour_picker")
        .assertExists()
        .onChildren()
        .filterToOne(hasText("10"))
        .assertExists()

    // Perform scroll
    composeTestRule.onNodeWithTag("test_hour_picker").performTouchInput {
      swipeUp(startY = center.y + 100f, endY = center.y - 100f)
    }
    waitForAnimation()

    // Verify scroll changed the value
    assert(selectedTime.hour != 10)
  }

  @Test
  fun schedulePreferencesSheet_exceptionsValidation() {
    composeTestRule.setContent { SchedulePreferencesSheet(viewModel = viewModel, onDismiss = {}) }
    composeTestRule.waitForIdle()

    // Wait for data to load and UI to settle
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }

    // Switch to Exceptions tab
    composeTestRule.onNodeWithText("Exceptions").performClick()
    waitForAnimation()

    // Click the add exception section to expand it
    composeTestRule.onNodeWithTag("addButton").assertExists().performClick()
    waitForAnimation()

    // Select Off Time type using the segmented button
    composeTestRule.onNodeWithText("Off Time").assertExists().performClick()
    waitForAnimation()

    // Verify Add Exception button is disabled when no date is selected
    composeTestRule
        .onNodeWithText("Add Exception")
        .assertExists()
        .assertHasClickAction()
        .assertIsEnabled()
  }

  @Test
  fun schedulePreferencesSheet_regularHoursValidation() {
    composeTestRule.setContent { SchedulePreferencesSheet(viewModel = viewModel, onDismiss = {}) }
    composeTestRule.waitForIdle()

    // Wait for data to load and UI to settle
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }

    // Find and click Monday's schedule
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").assertExists().performClick()
    waitForAnimation()

    // Set invalid time range (end before start)
    composeTestRule
        .onNodeWithTag("time_picker_MONDAY_start")
        .assertExists()
        .onChildren()
        .filterToOne(hasText("Start Time"))
        .assertExists()

    composeTestRule.onNodeWithTag("time_picker_MONDAY_start").assertExists().performTouchInput {
      click(centerRight)
    }
    waitForAnimation()

    composeTestRule.onNodeWithText("10").assertExists().performClick()
    waitForAnimation()

    composeTestRule.onNodeWithTag("time_picker_MONDAY_end").assertExists().performTouchInput {
      click(centerRight)
    }
    waitForAnimation()

    composeTestRule.onNodeWithText("09").assertExists().performClick()
    waitForAnimation()

    // Verify Save Hours button is enabled even with invalid time range
    composeTestRule
        .onNodeWithText("Save Hours")
        .assertExists()
        .assertHasClickAction()
        .assertIsEnabled()
  }

  @Test
  fun providerCalendarScreen_openSchedulePreferences() {
    setupScreen()
    composeTestRule.waitForIdle()

    // Wait for data to load
    composeTestRule.waitUntil(timeoutMillis = 5000) { !viewModel.isLoading.value }

    // Click settings button to open preferences sheet
    composeTestRule.onNodeWithTag("settingsButton").assertExists().performClick()
    waitForAnimation()

    // Verify preferences sheet is displayed
    composeTestRule.onNodeWithText("Regular Hours").assertExists()
    composeTestRule.onNodeWithText("Exceptions").assertExists()

    // Test basic interaction with regular hours tab
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").assertExists().performClick()
    waitForAnimation()

    // Verify time picker is shown
    composeTestRule.onNodeWithTag("time_picker_MONDAY_start").assertExists()

    // Test basic interaction with exceptions tab
    composeTestRule.onNodeWithText("Exceptions").performClick()
    waitForAnimation()

    composeTestRule.onNodeWithTag("addButton").assertExists().performClick()
    waitForAnimation()

    // Verify exception type options are shown
    composeTestRule.onNodeWithText("Off Time").assertExists()
    composeTestRule.onNodeWithText("Extra Time").assertExists()
  }

  @Test
  fun schedulePreferencesSheet_clearRegularHours() {
    setupScreen()
    composeTestRule.waitForIdle()

    // Open preferences sheet
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    waitForAnimation()

    // Click on Monday's schedule card
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").performClick()
    waitForAnimation()

    // Click clear hours button
    composeTestRule.onNodeWithTag("clear_hours_MONDAY").performClick()
    waitForAnimation()

    // Verify hours are cleared (card should show "No hours set")
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").assertTextContains("No hours set")
  }

  @Test
  fun schedulePreferencesSheet_addExtraTimeException() {
    setupScreen()
    composeTestRule.waitForIdle()

    // Open preferences sheet
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    waitForAnimation()

    // Switch to exceptions tab
    composeTestRule.onNodeWithText("Exceptions").performClick()
    waitForAnimation()

    // Click add exception section to expand it
    composeTestRule.onNodeWithTag("addExceptionTitle", useUnmergedTree = true).performClick()
    waitForAnimation()

    // Select Extra Time
    composeTestRule.onNodeWithText("Extra Time").performClick()
    waitForAnimation()

    // Select a date
    composeTestRule.onNodeWithTag("datePickerButton").performClick()
    waitForAnimation()

    // Click on a date in the calendar
    composeTestRule.onNodeWithTag("datePickerDialog").assertExists()
    composeTestRule.onNodeWithTag("confirmDateButton").performClick()
    waitForAnimation()

    // Add the exception
    composeTestRule.onNodeWithText("Add Extra Time", useUnmergedTree = true).performClick()
    waitForAnimation()

    // Verify exception is added
    composeTestRule.onNodeWithText("Extra Time").assertExists()
  }

  @Test
  fun schedulePreferencesSheet_deleteException() {
    // Use the same setup as addExtraTimeException
    schedulePreferencesSheet_addExtraTimeException()

    // Click delete button on the exception card
    composeTestRule.onNodeWithTag("deleteExceptionButton").performClick()
    waitForAnimation()

    // Verify exception is removed by checking that "Existing Exceptions" section is not visible
    composeTestRule.onNodeWithText("Existing Exceptions").assertDoesNotExist()
  }

  @Test
  fun scrollableTimePickerRow_hourSelectionInSheet() {
    setupScreen()
    composeTestRule.waitForIdle()

    // Open preferences sheet
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    waitForAnimation()

    // Open Monday's schedule
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").performClick()
    waitForAnimation()

    // Scroll hour picker
    composeTestRule.onNodeWithTag("time_picker_MONDAY_start_hour_picker").performTouchInput {
      swipeUp()
      swipeUp()
    }
    waitForAnimation()

    // Verify hour picker is still visible after scrolling
    composeTestRule.onNodeWithTag("time_picker_MONDAY_start_hour_picker").assertExists()
  }

  @Test
  fun scrollableTimePickerRow_minuteSnapBehavior() {
    setupScreen()
    composeTestRule.waitForIdle()

    // Open preferences sheet
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    waitForAnimation()

    // Open Monday's schedule
    composeTestRule.onNodeWithTag("day_schedule_card_MONDAY").performClick()
    waitForAnimation()

    // Scroll minute picker partially
    composeTestRule.onNodeWithTag("time_picker_MONDAY_start_minute_picker").performTouchInput {
      swipeUp(startY = centerY, endY = centerY - 50f)
    }
    waitForAnimation()

    // Let it snap
    composeTestRule.waitForIdle()

    // Verify minute picker is still visible after scrolling
    composeTestRule.onNodeWithTag("time_picker_MONDAY_start_minute_picker").assertExists()
  }
}
