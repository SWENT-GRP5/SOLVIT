package com.android.solvit.provider.ui.calendar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
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

    // Mock auth repository methods
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as (User?) -> Unit
          callback(testUser)
          null
        }
        .`when`(authRep)
        .init(any())

    doReturn(testUserId).`when`(authRep).getUserId()

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

    // Create real ServiceRequestViewModel with mocked repository and spy on it
    serviceRequestViewModel = spy(ServiceRequestViewModel(serviceRequestRepository))

    // Initialize calendar view model
    viewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)
  }

  private fun setupScreen() {
    composeTestRule.setContent {
      ProviderCalendarScreen(navigationActions = navigationActions, viewModel = viewModel)
    }
    // Wait for initial data load and animations
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.mainClock.advanceTimeBy(1000)
    composeTestRule.mainClock.autoAdvance = true
    composeTestRule.waitForIdle()
  }

  // ===== View Display Tests =====

    @Test
    fun testInitialCalendarView() {
        composeTestRule.onNodeWithTag("calendarTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menuButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendarViewToggle").assertIsDisplayed()
    }

  @Test
  fun testMonthViewDisplay() {
    setupScreen()

    // Switch to month view
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    composeTestRule.waitForIdle()

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
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("weekViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun testDayViewDisplay() {
    setupScreen()

    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    composeTestRule.waitForIdle()

    // 1. Test basic day view structure
    composeTestRule.onNodeWithTag("dayView").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("dayViewHeader").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("dayViewTimeGrid").assertExists().assertIsDisplayed()

    // 2. Test date header format
    val requestDate = testDate.toLocalDate()
    val formattedDate =
        requestDate.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault()))
    composeTestRule.onNodeWithText(formattedDate).assertExists().assertIsDisplayed()

    // 3. Test service request display and scrolling
    composeTestRule
        .onNodeWithTag("dayViewServiceRequest_${testRequest.uid}")
        .assertExists()
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag("dayViewTimeGrid").performTouchInput {
      swipeUp(startY = 1000f, endY = 100f)
    }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("dayViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
        .assertIsDisplayed()
  }

  // ===== Navigation Tests =====

  @Test
  fun testViewSwitching() {
    setupScreen()

    // Test complete cycle of view switches using segmented buttons
    assertEquals(CalendarView.WEEK, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("weekViewTimeGrid").assertExists()

    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CalendarView.MONTH, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("monthViewCalendarGrid").assertExists()

    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CalendarView.DAY, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("dayViewTimeGrid").assertExists()

    composeTestRule.onNodeWithTag("segmentedButton_WEEK").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CalendarView.WEEK, viewModel.calendarView.value)
    composeTestRule.onNodeWithTag("weekViewTimeGrid").assertExists()
  }

  @Test
  fun testDateNavigation() {
    setupScreen()

    // Test week view swipe navigation
    val initialWeekDate = viewModel.currentViewDate.value
    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()
    assertEquals(initialWeekDate.plusWeeks(1), viewModel.currentViewDate.value)

    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()
    assertEquals(initialWeekDate, viewModel.currentViewDate.value)

    // Test month view swipe navigation
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    composeTestRule.waitForIdle()

    val initialMonthDate = viewModel.currentViewDate.value
    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()
    assertEquals(initialMonthDate.plusMonths(1), viewModel.currentViewDate.value)

    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()
    assertEquals(initialMonthDate, viewModel.currentViewDate.value)

    // Test day view swipe navigation
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    composeTestRule.waitForIdle()

    val initialDayDate = viewModel.currentViewDate.value
    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()
    assertEquals(initialDayDate.plusDays(1), viewModel.currentViewDate.value)

    composeTestRule.onNodeWithTag("swipeableCalendarContainer").performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()
    assertEquals(initialDayDate, viewModel.currentViewDate.value)
  }

  @Test
  fun testHeaderDatePicker() {
    setupScreen()

    // Test week view header navigation and date picker interaction
    composeTestRule.onNodeWithTag("weekViewHeader").performClick()
    composeTestRule.waitForIdle()

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
    composeTestRule.waitForIdle()

    // Confirm the selection which will trigger the onDateSelected callback
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()

    // Verify that the current view date has been updated
    assertEquals(15, viewModel.currentViewDate.value.dayOfMonth)

    // Test month view header navigation
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("monthHeader").performClick()
    composeTestRule.waitForIdle()

    // Verify date picker appears in month view
    composeTestRule.onNodeWithTag("datePickerDialog").assertExists()
  }

  // ===== UI Interaction Tests =====

  @Test
  fun testServiceRequestDetails() {
    setupScreen()

    // Test date selection in week view by clicking the day header
    composeTestRule.onNodeWithTag("weekDayHeader_${testDate.toLocalDate()}").performClick()
    composeTestRule.waitForIdle()

    // Verify bottom sheet appears with time slots
    composeTestRule.onNodeWithTag("bottomSheet").assertExists()
    composeTestRule.onNodeWithTag("bottomSheetTimeSlots").assertExists()

    // Test service request click in bottom sheet
    composeTestRule.onNodeWithTag("weekViewServiceRequest_${testRequest.uid}").performClick()
    composeTestRule.waitForIdle()

    // Verify navigation to booking details
    verify(navigationActions).navigateTo(Route.BOOKING_DETAILS)

    // Test month view date selection
    composeTestRule.onNodeWithTag("segmentedButton_MONTH").performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("monthDayStatusColumn_${testDate.toLocalDate()}", useUnmergedTree = true)
        .performClick()
    composeTestRule.waitForIdle()

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
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag("weekViewServiceRequest_${afternoonTestRequest.uid}")
        .assertExists()
        .assertIsDisplayed()

    // Test day view scrolling
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dayViewTimeGrid").performTouchInput {
      swipeUp(startY = 1000f, endY = 100f)
    }
    composeTestRule.waitForIdle()
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
      composeTestRule.waitForIdle()
    }

    // Verify time grid components
    composeTestRule.onNodeWithTag("hourRow_$currentHour").assertExists()
    composeTestRule.onNodeWithTag("timeLabel_$currentHour").assertExists()

    // Verify current time indicator components
    composeTestRule.onNodeWithTag("currentTimeIndicatorCircle").assertExists()
    composeTestRule.onNodeWithTag("currentTimeIndicatorLine").assertExists()

    // Test day view current time indicator
    composeTestRule.onNodeWithTag("segmentedButton_DAY").performClick()
    composeTestRule.waitForIdle()

    // Scroll in day view as well if needed
    if (currentHour > 8) {
      composeTestRule.onNodeWithTag("dayViewTimeGrid").performTouchInput {
        swipeUp(startY = 1000f, endY = 100f, durationMillis = 200)
      }
      composeTestRule.waitForIdle()
    }

    // Verify time grid components in day view
    composeTestRule.onNodeWithTag("hourRow_$currentHour").assertExists()
    composeTestRule.onNodeWithTag("timeLabel_$currentHour").assertExists()

    // Verify current time indicator components in day view
    composeTestRule.onNodeWithTag("currentTimeIndicatorCircle").assertExists()
    composeTestRule.onNodeWithTag("currentTimeIndicatorLine").assertExists()
  }
}
