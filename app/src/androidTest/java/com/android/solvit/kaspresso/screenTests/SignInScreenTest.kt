package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.SignInScreenObject
import com.android.solvit.shared.ui.authentication.SignInScreen
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
class SignInScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signInScreenTest() = run {
    step("Set up the Sign-In screen") {
      // Set up the Sign-In screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        SignInScreen(navigationActions)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<SignInScreenObject>(composeTestRule) {
        emailInput.assertIsDisplayed()
        passwordInput.assertIsDisplayed()
        signInButton.assertIsDisplayed()
        googleSignInButton.assertIsDisplayed()
        signUpLink.assertIsDisplayed()
      }
    }

    step("Perform the Sign-In action") {
      // Perform the Sign-In action
      ComposeScreen.onComposeScreen<SignInScreenObject>(composeTestRule) {
        emailInput.performTextInput("jzedj@gmail.com")
        passwordInput.performTextInput("password")
        signInButton.performClick()
      }
    }

    step("Perform Sign-In with Google action") {
      // Perform Sign-In with Google action
      ComposeScreen.onComposeScreen<SignInScreenObject>(composeTestRule) {
        googleSignInButton.performClick()
      }
    }

    step("Perform Sign-Up action") {
      // Perform Sign-Up action
      ComposeScreen.onComposeScreen<SignInScreenObject>(composeTestRule) {
        signUpLink.performClick()
      }
    }
  }
}
