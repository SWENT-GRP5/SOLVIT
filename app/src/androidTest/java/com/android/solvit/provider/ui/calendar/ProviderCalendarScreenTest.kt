package com.android.solvit.provider.ui.calendar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.shared.model.authentication.*
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.*
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ProviderCalendarScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var viewModel: ProviderCalendarViewModel
  private lateinit var authRep: AuthRep
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel

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
    // Set up repositories and view models
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    authRep = mock(AuthRep::class.java)
    authViewModel = AuthViewModel(authRep)
    navigationActions = mock()

    // Mock auth repository to return test user
    whenever(authRep.init(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as (User?) -> Unit
      callback(User(testUserId, "test@example.com", "Provider"))
    }

    // Mock service request repository to return test request
    whenever(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<ServiceRequest>) -> Unit
      val onFailure = invocation.arguments[1] as (Exception) -> Unit
      onSuccess(listOf(testRequest))
    }

    // Create view model with mocked dependencies
    viewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)

    // Set up compose test rule
    composeTestRule.setContent {
      ProviderCalendarScreen(navigationActions = navigationActions, viewModel = viewModel)
    }
  }

  @Test
  fun testInitialCalendarView() {
    composeTestRule.onNodeWithTag("calendarTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("menuButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarViewToggle").assertIsDisplayed()
  }

  @Test
  fun testCalendarViewNavigation() {
    // Test Month View
    composeTestRule.onNodeWithTag("toggleButton_month").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggleButton_month").performClick()

    composeTestRule.onNodeWithTag("calendarColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }

    // Test Week View
    composeTestRule.onNodeWithTag("toggleButton_week").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggleButton_week").performClick()

    composeTestRule.onNodeWithTag("calendarColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }

    // Test Day View
    composeTestRule.onNodeWithTag("toggleButton_day").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggleButton_day").performClick()

    composeTestRule.onNodeWithTag("calendarColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }
  }

  @Test
  fun testDatePicker() {
    // Click the month header to open date picker
    composeTestRule.onNodeWithTag("monthHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthHeader").performClick()

    composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()

    // Select a date and confirm
    composeTestRule.onNodeWithTag("confirmDateButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmDateButton").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()

    // Open date picker again and cancel
    composeTestRule.onNodeWithTag("monthHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthHeader").performClick()

    composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelDateButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelDateButton").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()
  }
}
