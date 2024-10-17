package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.EditRequestScreenObject
import com.android.solvit.seeker.ui.request.EditRequestScreen
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestType
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import java.util.GregorianCalendar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class EditRequestScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  private val request =
      ServiceRequest(
          "uid",
          "title",
          ServiceRequestType.CLEANING,
          "description",
          "assigneeName",
          Timestamp(GregorianCalendar(2024, 0, 1).time),
          Location(37.7749, -122.4194, "San Francisco"),
          "imageUrl",
          ServiceRequestStatus.PENDING)

  @Test
  fun editRequestScreenTest() = run {
    step("Set up the Create Request screen") {
      // Set up the Create Request screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        val serviceRequestRepository = mock(ServiceRequestRepository::class.java)
        val serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
        `when`(navigationActions.currentRoute()).thenReturn(Route.CREATE_REQUEST)
        `when`(serviceRequestRepository.getNewUid()).thenReturn("123")
        serviceRequestViewModel.selectRequest(request)
        EditRequestScreen(
            navigationActions = navigationActions, requestViewModel = serviceRequestViewModel)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<EditRequestScreenObject>(composeTestRule) {
        inputRequestTitle.assertIsDisplayed()
        inputRequestDescription.assertIsDisplayed()
        inputRequestDate.assertIsDisplayed()
        inputRequestType.assertIsDisplayed()
        inputRequestAddress.assertIsDisplayed()
        imagePickerButton.assertIsDisplayed()
        submitButton.assertIsDisplayed()
        deleteButton.assertIsDisplayed()
      }
    }

    step("Perform the Create Request action") {
      // Perform the Create Request action
      ComposeScreen.onComposeScreen<EditRequestScreenObject>(composeTestRule) {
        inputRequestTitle.performTextInput("Request Title")
        inputRequestDescription.performTextInput("Request Description")
        inputRequestDate.performTextInput("12/31/2021")
        inputRequestType.performTextInput("Plumbing")
        serviceTypeResult.performClick()
        inputRequestAddress.performTextInput("Request Address")
        imagePickerButton.performClick()
        submitButton.performClick()
      }
    }

    step("Perform the Delete Request action") {
      // Perform the Delete Request action
      ComposeScreen.onComposeScreen<EditRequestScreenObject>(composeTestRule) {
        deleteButton.performClick()
      }
    }
  }
}
