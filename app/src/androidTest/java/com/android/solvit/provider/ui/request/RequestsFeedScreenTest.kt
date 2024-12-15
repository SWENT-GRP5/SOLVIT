package com.android.solvit.provider.ui.request

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalRepository
import com.android.solvit.shared.model.packages.PackageProposalViewModel
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

// Test class for RequestsFeedScreen functionality
class RequestsFeedScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var packageProposalRepository: PackageProposalRepository
  private lateinit var packageProposalViewModel: PackageProposalViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  @get:Rule val composeTestRule = createComposeRule()

  // Example service request data used for testing
  private val requests =
      listOf(
          ServiceRequest(
              title = "French Tutor",
              description = "I need a tutor to help me with my French",
              userId = "1",
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
              type = Services.PLUMBER,
              imageUrl =
                  "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"))

  private val emptyPackages = emptyList<PackageProposal>()
  private val packages =
      listOf(
          PackageProposal(
              uid = "1",
              packageNumber = 1.0,
              providerId = "provider1",
              title = "Package 1",
              description = "Description 1",
              price = 99.99,
              bulletPoints = listOf("Point 1", "Point 2")),
          PackageProposal(
              uid = "2",
              packageNumber = 2.0,
              providerId = "provider1",
              title = "Package 2",
              description = "Description 2",
              price = 199.99,
              bulletPoints = listOf("Point 1", "Point 2")))

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    packageProposalRepository = mock(PackageProposalRepository::class.java)
    packageProposalViewModel = PackageProposalViewModel(packageProposalRepository)
    navController = mock(NavController::class.java)
    navigationActions = mock(NavigationActions::class.java)
    // Mocking the getServiceRequests function to return the pre-defined request list
    `when`(serviceRequestRepository.getPendingServiceRequests(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(requests) // Simulate success
    }

    `when`(navigationActions.currentRoute()).thenReturn(Route.REQUESTS_FEED)

    // Fetch service requests via the ViewModel
    serviceRequestViewModel.getServiceRequests()
  }

  // Test to check if all important UI components are displayed
  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.setContent {
      RequestsFeedScreen(serviceRequestViewModel, packageProposalViewModel, navigationActions)
    }
    // Verify if the main screen container is displayed
    composeTestRule.onNodeWithTag("ListRequestsScreen").isDisplayed()

    // Check if the top bar is displayed
    composeTestRule.onNodeWithTag("RequestsTopBar").isDisplayed()

    // Verify the presence of the main screen content
    composeTestRule.onNodeWithTag("ScreenContent")
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
    composeTestRule.setContent {
      RequestsFeedScreen(serviceRequestViewModel, packageProposalViewModel, navigationActions)
    }

    composeTestRule.onNodeWithTag("SearchBar").performTextInput("French")

    // Check that no service request is displayed when the search query does not match any service
    // request
    composeTestRule.onNodeWithTag("SearchBar").performTextInput("Plumber")
    assert(composeTestRule.onAllNodesWithTag("ServiceRequest").fetchSemanticsNodes().isEmpty())
  }

  @Test
  fun testFilterServices() {
    composeTestRule.setContent {
      RequestsFeedScreen(serviceRequestViewModel, packageProposalViewModel, navigationActions)
    }
    // Perform Service Filtering
    composeTestRule.onNodeWithTag("ServiceFilter").isDisplayed()
    composeTestRule.onNodeWithTag("ServiceFilter").performClick()
    // Choose to keep only tutors
    composeTestRule.onNodeWithTag("Tutor").isDisplayed()
    composeTestRule.onNodeWithTag("Tutor").performClick()
  }

  @Test
  fun proposePackageDialogProposesSelectedPackage() {
    val showDialog = mutableStateOf(true)
    val request = requests[0]

    composeTestRule.setContent {
      ProposePackageDialog(
          providerId = "provider1",
          request = request,
          packages = packages,
          showDialog = showDialog,
          requestViewModel = serviceRequestViewModel)
    }

    composeTestRule.onNodeWithTag("packagesScrollableList").assertExists()
    composeTestRule.onAllNodesWithTag("PackageCard")[0].performClick()
    composeTestRule.onNodeWithText("Propose Package").performClick()
  }

  @Test
  fun proposePackageDialogDisplaysErrorForInvalidPrice() {
    val showDialog = mutableStateOf(true)
    val request = requests[0]

    composeTestRule.setContent {
      ProposePackageDialog(
          providerId = "provider1",
          request = request,
          packages = emptyPackages,
          showDialog = showDialog,
          requestViewModel = serviceRequestViewModel)
    }

    composeTestRule
        .onNodeWithText("No packages available. Please enter a price for this service:")
        .assertExists()
    composeTestRule.onNodeWithTag("PriceInput").performTextInput(".99")
    composeTestRule.onNodeWithText("Please enter a valid number (e.g., 99 or 99.99)").assertExists()
    composeTestRule.onNodeWithTag("PriceInput").performTextClearance()
    composeTestRule.onNodeWithTag("PriceInput").performTextInput("99.9")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()
  }
}
