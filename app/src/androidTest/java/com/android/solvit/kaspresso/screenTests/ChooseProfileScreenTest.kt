package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.ChooseProfileScreenObject
import com.android.solvit.shared.ui.authentication.SignUpChooseProfile
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ChooseProfileScreenTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun chooseProfileScreenTest() = run {
    step("Set up the Choose Profile screen") {
      // Set up the Choose Profile screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        SignUpChooseProfile(navigationActions)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<ChooseProfileScreenObject>(composeTestRule) {
        customerButton.assertIsDisplayed()
        providerButton.assertIsDisplayed()
        learnMoreLink.assertIsDisplayed()
      }
    }

    step("Perform the Choose Seeker profile action") {
      // Perform the Choose Profile action
      ComposeScreen.onComposeScreen<ChooseProfileScreenObject>(composeTestRule) {
        customerButton.performClick()
      }
    }

    step("Perform the Choose Provider profile action") {
      // Perform the Choose Profile action
      ComposeScreen.onComposeScreen<ChooseProfileScreenObject>(composeTestRule) {
        providerButton.performClick()
      }
    }

    step("Perform the Learn More action") {
      // Perform the Learn More action
      ComposeScreen.onComposeScreen<ChooseProfileScreenObject>(composeTestRule) {
        learnMoreLink.performClick()
      }
    }
  }
}
