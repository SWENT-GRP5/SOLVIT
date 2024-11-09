package com.android.solvit.seeker.ui.request

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.provider.ui.request.ListRequestsFeedScreen
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

// Test class for ListServicesRequestsScreen functionality
class ListServicesRequestsScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  @get:Rule val composeTestRule = createComposeRule()

  // Example service request data used for testing
  val request =
      listOf(
          ServiceRequest(
              title = "Bathtub leak",
              description = "I hit my bath too hard and now it's leaking",
              userId = "1",
              dueDate = Timestamp(Calendar.getInstance().time),
              location =
                  Location(
                      48.8588897,
                      2.3200410217200766,
                      "Paris, Île-de-France, France métropolitaine, France"),
              status = ServiceRequestStatus.PENDING,
              uid = "gIoUWJGkTgLHgA7qts59",
              type = Services.PLUMBER,
              imageUrl =
                  "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"),
          ServiceRequest(
              title = "Bathtub leak",
              description = "I hit my bath too hard and now it's leaking",
              userId = "2",
              dueDate = Timestamp(Calendar.getInstance().time),
              location =
                  Location(
                      48.8588897,
                      2.3200410217200766,
                      "Paris, Île-de-France, France métropolitaine, France"),
              status = ServiceRequestStatus.PENDING,
              uid = "gIoUWJGkTgLHgA7qts59",
              type = Services.TUTOR,
              imageUrl =
                  "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"))

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    navController = mock(NavController::class.java)
    navigationActions = mock(NavigationActions::class.java)
    // Mocking the getServiceRequests function to return the pre-defined request list
    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(request) // Simulate success
    }
    `when`(navigationActions.currentRoute()).thenReturn(Route.REQUESTS_FEED)
    composeTestRule.setContent {
      ListRequestsFeedScreen(serviceRequestViewModel, navigationActions)
    }

    // Fetch service requests via the ViewModel
    serviceRequestViewModel.getServiceRequests()
  }

  // Test to check if all important UI components are displayed
  @Test
  fun allComponentsAreDisplayed() {
    // Verify if the main screen container is displayed
    composeTestRule.onNodeWithTag("ListRequestsScreen").isDisplayed()

    // Check if the top bar is displayed
    composeTestRule.onNodeWithTag("RequestsTopBar").isDisplayed()

    // Verify the presence of the main screen content
    composeTestRule.onNodeWithTag("ScreenContent")
    composeTestRule.onNodeWithTag("MenuOption").isDisplayed()
    composeTestRule.onNodeWithTag("SloganIcon").isDisplayed()

    // Ensure the search bar is visible
    composeTestRule.onNodeWithTag("SearchBar").isDisplayed()

    // Ensure that title is visible
    composeTestRule.onNodeWithTag("TitleScreen").isDisplayed()

    // Ensure that services requests are displayed
    composeTestRule.onNodeWithTag("requests").isDisplayed()

    // Ensure that filtering bar is displayed
    composeTestRule.onNodeWithTag("FilterBar").isDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").isDisplayed()
  }

  // Test the functionality of the search bar
  @Test
  fun testSearchBar() {
    // Simulate user input in the search bar with the text "French"
    composeTestRule.onNodeWithTag("SearchBar").performTextInput("French")
    // TODO: complete the test when implement searching functionnality
  }

  @Test
  fun testFilterServices() {
    assert(composeTestRule.onAllNodesWithTag("FilterBar").fetchSemanticsNodes().isNotEmpty())
    // Perform Service Filtering
    composeTestRule.onNodeWithTag("ServiceChip").isDisplayed()
    composeTestRule.onNodeWithTag("ServiceChip").performClick()
    // Choose to keep only tutors
    composeTestRule.onNodeWithTag("TUTOR").isDisplayed()
    composeTestRule.onNodeWithTag("TUTOR").performClick()

    // Check that only tutor service request is displayed on the screen
    assert(composeTestRule.onAllNodesWithTag("ServiceRequest").fetchSemanticsNodes().size == 1)
  }
}
