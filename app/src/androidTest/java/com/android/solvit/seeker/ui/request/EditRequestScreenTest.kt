package com.android.solvit.seeker.ui.request

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never

class EditRequestScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  @Before
  fun setUp() {
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)

    Mockito.`when`(locationRepository.search(anyString(), anyOrNull(), anyOrNull())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
      onSuccess(locations)
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertTextEquals("Edit your request")
    composeTestRule.onNodeWithTag("requestSubmit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestSubmit").assertTextEquals("Save Edits")
    composeTestRule.onNodeWithTag("deleteRequestButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteRequestButton").assertTextEquals("Delete")

    composeTestRule.onNodeWithTag("inputRequestTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestAddress").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("imagePickerButton").assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput("notadate")
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }

  @Test
  fun locationMenuExpandsWithInput() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[2].assertIsDisplayed()
  }

  @Test
  fun locationSelectionFromDropdown() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
    assert(locationViewModel.locationSuggestions.value == locations)
    assert(locationViewModel.query.value == "San Francisco")
  }

  @Test
  fun serviceTypeDropdown_showsFilteredResults() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextClearance()
    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertExists()
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Plumbing"))
  }

  @Test
  fun serviceTypeDropdown_closesOnSelection() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun serviceTypeDropdown_showsNoResultsMessage() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("NonExistentType")
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Other"))
  }

  @Test
  fun serviceTypeDropdown_closesOnFocusLost() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("inputRequestTitle").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun deleteButton_triggersDeleteAction() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("deleteRequestButton").performClick()

    Mockito.verify(serviceRequestRepository).deleteServiceRequestById(any(), any(), any())
  }

  @Test
  fun deleteButton_logsErrorOnFailure() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    Mockito.`when`(serviceRequestRepository.deleteServiceRequestById(any(), any(), any()))
        .thenAnswer { invocation ->
          val onError = invocation.getArgument<(String) -> Unit>(2)
          onError("Error")
        }

    composeTestRule.onNodeWithTag("deleteRequestButton").performClick()

    Mockito.verify(serviceRequestRepository).deleteServiceRequestById(any(), any(), any())
  }

  @Test
  fun doesNotSubmitWithInvalidTitle() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestTitle").performTextClearance()
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }

  @Test
  fun submitWithValidData() {
    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestTitle").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestTitle").performTextInput("Title")
    composeTestRule.onNodeWithTag("inputRequestDescription").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestDescription").performTextInput("Description")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.onNodeWithTag("inputRequestDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput("01/01/2022")
    composeTestRule.onNodeWithTag("inputServiceType").performTextClearance()
    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository).saveServiceRequest(any(), any(), any())
  }
}
