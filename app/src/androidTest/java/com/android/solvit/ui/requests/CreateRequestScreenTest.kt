package com.android.solvit.ui.requests

import android.net.Uri
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
import com.android.solvit.model.map.Location
import com.android.solvit.model.map.LocationRepository
import com.android.solvit.model.map.LocationViewModel
import com.android.solvit.model.requests.ServiceRequestRepository
import com.android.solvit.model.requests.ServiceRequestViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never

class CreateRequestScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var validImageUri: Uri

  @get:Rule val composeTestRule = createComposeRule()

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  @Before
  fun setUp() {
    serviceRequestRepository = Mockito.mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    locationRepository = Mockito.mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)

    validImageUri = Uri.parse("content://com.android.solvit.provider/image/1")

    `when`(locationRepository.search(anyString(), anyOrNull(), anyOrNull())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
      onSuccess(locations)
    }

    `when`(serviceRequestRepository.getNewUid()).thenReturn("1")
    `when`(serviceRequestRepository.saveServiceRequestWithImage(any(), any(), any(), any()))
        .thenAnswer {}
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertTextEquals("Create a new request")
    composeTestRule.onNodeWithTag("requestSubmit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestSubmit").assertTextEquals("Submit Request")

    composeTestRule.onNodeWithTag("inputRequestTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputServiceType").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestAddress").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("imagePickerButton").assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputRequestDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput("notadate")
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }

  @Test
  fun locationMenuExpandsWithInput() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[2].assertIsDisplayed()
  }

  @Test
  fun locationSelectionFromDropdown() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
    assert(locationViewModel.locationSuggestions.value == locations)
    assert(locationViewModel.query.value == "San Francisco")
  }

  @Test
  fun serviceTypeDropdown_showsFilteredResults() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertExists()
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Plumbing"))
  }

  @Test
  fun serviceTypeDropdown_closesOnSelection() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun serviceTypeDropdown_showsNoResultsMessage() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("NonExistentType")
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Other"))
  }

  @Test
  fun serviceTypeDropdown_closesOnFocusLost() {
    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("inputRequestTitle").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun saveButton_showsErrorForInvalidDate() {
    val invalidDate = "99/99/2024"

    composeTestRule.setContent { CreateRequestScreen(serviceRequestViewModel, locationViewModel) }

    // Input invalid date
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput(invalidDate)

    // Click save
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    // Verify no save is attempted
    Mockito.verify(serviceRequestRepository, never())
        .saveServiceRequestWithImage(any(), any(), any(), any())
  }
}