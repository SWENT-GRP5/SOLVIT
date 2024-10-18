package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.CreateRequestScreenObject
import com.android.solvit.seeker.ui.request.CreateRequestScreen
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class CreateRequestScreenTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun createRequestScreenTest() = run {
    step("Set up the Create Request screen") {
      // Set up the Create Request screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        val serviceRequestRepository = mock(ServiceRequestRepository::class.java)
        val serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
        `when`(navigationActions.currentRoute()).thenReturn(Route.CREATE_REQUEST)
        `when`(serviceRequestRepository.getNewUid()).thenReturn("123")
        CreateRequestScreen(
            navigationActions = navigationActions, requestViewModel = serviceRequestViewModel)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<CreateRequestScreenObject>(composeTestRule) {
        inputRequestTitle.assertIsDisplayed()
        inputRequestDescription.assertIsDisplayed()
        inputRequestDate.assertIsDisplayed()
        inputRequestType.assertIsDisplayed()
        inputRequestAddress.assertIsDisplayed()
        imagePickerButton.assertIsDisplayed()
        submitButton.assertIsDisplayed()
      }
    }

    step("Perform the Create Request action") {
      // Perform the Create Request action
      ComposeScreen.onComposeScreen<CreateRequestScreenObject>(composeTestRule) {
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
  }
}
