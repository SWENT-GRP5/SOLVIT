package com.android.solvit.seeker.ui.request

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class RequestsOverviewScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Route.REQUESTS_OVERVIEW)
  }

  @Test
  fun displayTextWhenEmpty() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).then {
      it.getArgument<(List<ServiceRequest>) -> Unit>(0)(listOf())
    }
    serviceRequestViewModel.getServiceRequests()

    composeTestRule.onNodeWithTag("noServiceRequestsScreen").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun clickOnRequestNavigatesToEditScreen() {
    val request =
        ServiceRequest(
            "uid",
            "title",
            Services.CLEANER,
            "description",
            "assigneeName",
            Timestamp(GregorianCalendar(2024, 0, 1).time),
            Location(37.7749, -122.4194, "San Francisco"),
            "imageUrl",
            ServiceRequestStatus.PENDING)

    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).then {
      it.getArgument<(List<ServiceRequest>) -> Unit>(0)(listOf(request))
    }
    serviceRequestViewModel.getServiceRequests()

    composeTestRule.onNodeWithText("title").performClick()
    verify(navigationActions).navigateTo(Route.EDIT_REQUEST)
  }

  @Test
  fun todoItemDisplaysCorrectData() {
    val request =
        ServiceRequest(
            "uid",
            "title",
            Services.PLUMBER,
            "description",
            "assigneeName",
            Timestamp(GregorianCalendar(2024, 0, 1).time),
            Location(37.7749, -122.4194, "San Francisco"),
            "imageUrl",
            ServiceRequestStatus.PENDING)

    composeTestRule.setContent { RequestItemRow(request = request, onClick = {}) }

    composeTestRule.onNodeWithText("title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Plumber").assertIsDisplayed()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val expectedDate = dateFormat.format(request.dueDate.toDate())
    composeTestRule.onNodeWithText("Deadline: $expectedDate").assertIsDisplayed()
  }

  @Test
  fun todoItemDisplaysCorrectStatus() {
    val request =
        ServiceRequest(
            "uid",
            "title",
            Services.CLEANER,
            "description",
            "assigneeName",
            Timestamp(GregorianCalendar(2024, 0, 1).time),
            Location(37.7749, -122.4194, "San Francisco"),
            "imageUrl",
            ServiceRequestStatus.PENDING)

    composeTestRule.setContent { RequestItemRow(request = request, onClick = {}) }

    composeTestRule.onNodeWithText("Pending").assertIsDisplayed()
  }

  @Test
  fun todoItemRowClickCallsOnClick() {
    val request =
        ServiceRequest(
            "uid",
            "title",
            Services.OTHER,
            "description",
            "assigneeName",
            Timestamp(GregorianCalendar(2024, 0, 1).time),
            Location(37.7749, -122.4194, "San Francisco"),
            "imageUrl",
            ServiceRequestStatus.PENDING)

    val onClickMock = mock<() -> Unit>()

    composeTestRule.setContent { RequestItemRow(request = request, onClick = onClickMock) }

    composeTestRule.onNodeWithTag("requestListItem").performClick()
    verify(onClickMock).invoke()
  }
}
