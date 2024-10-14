package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = Mockito.mock(NavigationActions::class.java)

  @Test
  fun testSignInScreen_displaysAllComponents() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test the display of UI components
    composeTestRule.onNodeWithTag("loginImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SignInText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("passwordInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rememberMeText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("forgotPasswordText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signInButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Bottom signUp").assertIsDisplayed()
  }

  @Test
  fun testSignInScreen_emailAndPasswordInput() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test email input
    composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password123")
  }

  @Test
  fun testRememberMeCheckbox_togglesCorrectly() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test that the checkbox is clickable and can toggle between states
    composeTestRule.onNodeWithTag("rememberMeText").performClick()
    composeTestRule.onNodeWithTag("rememberMeText").assertIsDisplayed()
  }

  @Test
  fun testForgotPasswordClickableText_isClickable() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test Forgot Password clickable text
    composeTestRule.onNodeWithTag("forgotPasswordText").performClick()

    // No action is verified, as the VM is not implemented
  }

  @Test
  fun testGoBackButton_isClickable() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Perform click on Go Back button
    composeTestRule.onNodeWithTag("goBackButton", useUnmergedTree = true).performClick()

    // No action is verified, as the VM is not yet implemented
  }
}
