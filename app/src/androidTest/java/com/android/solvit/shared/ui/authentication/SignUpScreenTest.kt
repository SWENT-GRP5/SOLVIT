package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val intentsTestRule = IntentsRule()

  private val mockNavigationActions = mock(NavigationActions::class.java)

  @Test
  fun signUpScreen_testAllTheTest() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    // Check if all components are displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpIllustration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleSignUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("passwordInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmPasswordInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logInLink").assertIsDisplayed()
  }

  @Test
  fun signUpScreen_testAllPerformClick() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.onNodeWithTag("passwordInput").performClick()
    composeTestRule.onNodeWithTag("confirmPasswordInput").performClick()
    composeTestRule.onNodeWithTag("emailInputField").performClick()
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    composeTestRule.onNodeWithTag("logInLink").performClick()
  }

  @Test
  fun signUpScreen_emailInput() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test email input
    composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }
    composeTestRule.onNodeWithTag("googleSignUpButton").performClick()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    Intents.intended(IntentMatchers.toPackage("com.google.android.gms"))
  }

  @Test
  fun formCompleteEnablesButton() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@test.com")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password")
    composeTestRule.onNodeWithTag("confirmPasswordInput").performTextInput("password")

    composeTestRule.onNodeWithTag("signUpButton").assertIsEnabled()
  }

  @Test
  fun signUpButtonNavigatesToChooseRoleScreen() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@test.com")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password")
    composeTestRule.onNodeWithTag("confirmPasswordInput").performTextInput("password")

    composeTestRule.onNodeWithTag("signUpButton").performClick()
    verify(mockNavigationActions).navigateTo(Screen.SIGN_UP_CHOOSE_ROLE)
  }
}
