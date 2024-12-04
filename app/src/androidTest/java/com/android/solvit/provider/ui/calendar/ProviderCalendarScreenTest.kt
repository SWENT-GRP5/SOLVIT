package com.android.solvit.provider.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

class ProviderCalendarScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var viewModel: ProviderCalendarViewModel
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var userFlow: MutableStateFlow<User?>
  private lateinit var requestsFlow: MutableStateFlow<List<ServiceRequest>>

  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")
  private val testDate = LocalDateTime.of(2024, 1, 8, 10, 0)

  private val testRequest =
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

  @Before
  fun setUp() {
    // Set up navigation actions
    navigationActions = mock()

    // Set up auth view model
    authViewModel = mock()
    val mockUser: User = mock()
    whenever(mockUser.uid).thenReturn(testUserId)
    userFlow = MutableStateFlow(mockUser)
    whenever(authViewModel.user).thenReturn(userFlow)

    // Set up service request view model
    serviceRequestViewModel = mock()
    requestsFlow = MutableStateFlow(listOf(testRequest))
    whenever(serviceRequestViewModel.requests)
        .thenReturn(requestsFlow as StateFlow<List<ServiceRequest>>)

    // Set up calendar view model
    viewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)

    // Set up compose test rule
    composeTestRule.setContent {
      ProviderCalendarScreen(navigationActions = navigationActions, viewModel = viewModel)
    }
  }

  @Test
  fun testInitialCalendarView() {
    composeTestRule.onNodeWithTag("calendarTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("menuButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarViewToggle").assertIsDisplayed()
  }

  @Test
  fun testCalendarViewNavigation() {
    // Test Month View
    composeTestRule.onNodeWithTag("toggleButton_month").performClick()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }

    // Test Week View
    composeTestRule.onNodeWithTag("toggleButton_week").performClick()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }

    // Test Day View
    composeTestRule.onNodeWithTag("toggleButton_day").performClick()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }
  }

  @Test
  fun testTimeSlotClick() {
    // Find and click the time slot
    composeTestRule.onNodeWithTag("timeSlot_${testRequest.uid}").performClick()

    // Verify navigation to booking details
    verify(navigationActions).navigateTo(Route.BOOKING_DETAILS, testRequest.uid)
  }

  @Test
  fun testDatePicker() {
    // Open the date picker
    composeTestRule.onNodeWithTag("monthHeader").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()

    // Select a specific date
    composeTestRule.onNodeWithText("15").performClick()

    // Confirm the selection
    composeTestRule.onNodeWithTag("confirmDateButton").performClick()

    // Assert that the date picker dialog is no longer displayed
    composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()

    // Test cancelling the date picker
    composeTestRule.onNodeWithTag("monthHeader").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelDateButton").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()
  }
}
