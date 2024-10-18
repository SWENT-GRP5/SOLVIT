package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.OpeningScreenObject
import com.android.solvit.shared.ui.authentication.OpeningScreen
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
class OpeningScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun openingScreenTest() = run {
    step("Set up the Opening screen") {
      // Set up the Opening screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        OpeningScreen(navigationActions)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<OpeningScreenObject>(composeTestRule) {
        ctaButton.assertIsDisplayed()
      }
    }

    step("Perform the click action") {
      // Perform the action
      ComposeScreen.onComposeScreen<OpeningScreenObject>(composeTestRule) {
        ctaButton.performClick()
      }
    }
  }
}
