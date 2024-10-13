package com.android.solvit.provider.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestType
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ProviderMapScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val testRequests =
      listOf(
          ServiceRequest(
              uid = "1",
              title = "Test Request 1",
              type = ServiceRequestType.PLUMBING,
              description = "Test Description 1",
              assigneeName = "Assignee 1",
              location = Location(0.0, 0.0, "Test Location 1"),
              status = ServiceRequestStatus.PENDING,
              imageUrl = "https://example",
              dueDate = Timestamp(0, 0)),
          ServiceRequest(
              uid = "2",
              title = "Test Request 2",
              type = ServiceRequestType.PLUMBING,
              description = "Test Description 2",
              assigneeName = "Assignee 2",
              location = Location(10.0, 10.0, "Test Location 2"),
              status = ServiceRequestStatus.PENDING,
              imageUrl = "https://example",
              dueDate = Timestamp(0, 0)))

  @Before
  fun setUp() {
    serviceRequestRepository = Mockito.mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    navController = Mockito.mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(testRequests)
    }
  }

  @Test
  fun hasRequiredElements() {
    composeTestRule.setContent {
      ProviderMapScreen(serviceRequestViewModel, navigationActions, false)
    }

    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleMap").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun displaysRequestsOnMap() {
    composeTestRule.setContent {
      ProviderMapScreen(serviceRequestViewModel, navigationActions, false)
    }

    serviceRequestViewModel.requests.value.forEach { request ->
      composeTestRule.onNodeWithTag("requestMarker-${request.uid}").assertIsDisplayed()
      composeTestRule.onNodeWithText(request.title).assertIsDisplayed()
      composeTestRule.onNodeWithText(request.description).assertIsDisplayed()
        composeTestRule.onNodeWithText(request.assigneeName).assertIsDisplayed()
    }
  }

  @Test
  fun showsRequestLocationPermission() {
    composeTestRule.setContent {
      ProviderMapScreen(serviceRequestViewModel, navigationActions, true)
    }
  }
}
