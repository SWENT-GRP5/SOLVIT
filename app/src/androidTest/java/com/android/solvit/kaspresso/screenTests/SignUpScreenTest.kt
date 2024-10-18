package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.SignUpScreenObject
import com.android.solvit.shared.ui.authentication.SignUpScreen
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
class SignUpScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signUpScreenTest() = run {
    step("Set up the Sign-Up screen") {
      // Set up the Sign-Up screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        SignUpScreen(navigationActions)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<SignUpScreenObject>(composeTestRule) {
        googleSignUpButton.assertIsDisplayed()
        emailInput.assertIsDisplayed()
        passwordInput.assertIsDisplayed()
        confirmPasswordInput.assertIsDisplayed()
        signUpButton.assertIsDisplayed()
      }
    }

    step("Perform the Sign-Up action") {
      // Perform the Sign-Up action
      ComposeScreen.onComposeScreen<SignUpScreenObject>(composeTestRule) {
        emailInput.performTextInput("jadlka@gmail.com")
        passwordInput.performTextInput("password")
        confirmPasswordInput.performTextInput("password")
        signUpButton.performClick()
      }
    }

    step("Perform Sign-Up with Google action") {
      // Perform Sign-Up with Google action
      ComposeScreen.onComposeScreen<SignUpScreenObject>(composeTestRule) {
        googleSignUpButton.performClick()
      }
    }
  }
}
