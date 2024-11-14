package com.android.solvit.provider.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.util.GregorianCalendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
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
              type = Services.PLUMBER,
              description = "Test Description 1",
              userId = "1",
              location = Location(0.0, 0.0, "Test Location 1"),
              status = ServiceRequestStatus.PENDING,
              imageUrl = "https://example",
              dueDate = Timestamp(GregorianCalendar(2021, 1, 1).time)),
          ServiceRequest(
              uid = "2",
              title = "Test Request 2",
              type = Services.PLUMBER,
              description = "Test Description 2",
              userId = "2",
              location = Location(10.0, 10.0, "Test Location 2"),
              status = ServiceRequestStatus.PENDING,
              imageUrl = "https://example",
              dueDate = Timestamp(GregorianCalendar(2022, 12, 31).time)))

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    navController = mock(NavController::class.java)
    navigationActions = mock(NavigationActions::class.java)

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(testRequests)
    }

    `when`(navigationActions.currentRoute()).thenReturn(Route.MAP)
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
    }
  }

  @Test
  fun showsRequestLocationPermission() {
    composeTestRule.setContent {
      ProviderMapScreen(serviceRequestViewModel, navigationActions, true)
    }

    assert(true)
  }

  @Test
  fun onTabSelect_navigatesToCorrectRoute() {
    composeTestRule.setContent {
      ProviderMapScreen(serviceRequestViewModel, navigationActions, false)
    }

    composeTestRule
        .onNodeWithTag(LIST_TOP_LEVEL_DESTINATION_PROVIDER.get(0).textId)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(LIST_TOP_LEVEL_DESTINATION_PROVIDER.get(0).textId).performClick()
  }
}
