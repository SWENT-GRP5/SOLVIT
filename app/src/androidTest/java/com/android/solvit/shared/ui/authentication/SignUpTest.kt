package com.android.solvit.shared.ui.authentication

import android.widget.Toast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
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
  fun signUp_testAllTheTest() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpIllustration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleSignUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("passwordInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmPasswordInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logInLink").assertIsDisplayed()
    composeTestRule.onNodeWithTag("generatePasswordButton").assertIsDisplayed()
  }

  @Test
  fun signUp_testAllPerformClick() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("passwordInputField").performClick()
    composeTestRule.onNodeWithTag("confirmPasswordInputField").performClick()
    composeTestRule.onNodeWithTag("emailInputField").performClick()
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    composeTestRule.onNodeWithTag("logInLink").performClick()
  }

  @Test
  fun signUp_emailAndPasswordInput() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    // Test email input
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("passwordInputField").performTextInput("password123")
  }

  @Test
  fun signUp_errorShowInEmailField() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("emailErrorMessage").isNotDisplayed()
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@example")
    composeTestRule.onNodeWithTag("emailErrorMessage").isDisplayed()
    composeTestRule.onNodeWithTag("emailInputField").performTextClearance()
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("emailErrorMessage").isNotDisplayed()
  }

  @Test
  fun signUp_errorShowInPasswordField() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("passwordErrorMessage").isNotDisplayed()
    composeTestRule.onNodeWithTag("passwordInputField").performTextInput("12345")
    composeTestRule.onNodeWithTag("passwordErrorMessage").isDisplayed()
    composeTestRule.onNodeWithTag("passwordInputField").performTextClearance()
    composeTestRule.onNodeWithTag("passwordInputField").performTextInput("123456")
    composeTestRule.onNodeWithTag("passwordErrorMessage").isNotDisplayed()
  }

  @Test
  fun signUp_errorShowInConfirmPasswordField() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("confirmPasswordErrorMessage").isNotDisplayed()
    composeTestRule.onNodeWithTag("confirmPasswordInputField").performTextInput("12345")
    composeTestRule.onNodeWithTag("confirmPasswordErrorMessage").isDisplayed()
    composeTestRule.onNodeWithTag("confirmPasswordInputField").performTextClearance()
    composeTestRule.onNodeWithTag("confirmPasswordInputField").performTextInput("123456")
    composeTestRule.onNodeWithTag("confirmPasswordErrorMessage").isNotDisplayed()
  }

  @Test
  fun signUp_emailInput() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    // Test email input
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@example.com")
  }

  @Test
  fun signUp_googleSignInReturnsValidActivityResult() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }
    composeTestRule.onNodeWithTag("googleSignUpButton").performClick()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    Intents.intended(IntentMatchers.toPackage("com.google.android.gms"))
  }

  @Test
  fun signUp_formCompleteEnablesButton() {
    runBlocking {
      composeTestRule.setContent { SignUpScreen(mockNavigationActions) }
      composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@test.com")
      composeTestRule.onNodeWithTag("passwordInputField").performTextInput("password")
      composeTestRule.onNodeWithTag("confirmPasswordInputField").performTextInput("password")

      composeTestRule.onNodeWithTag("signUpButton").assertIsEnabled()
    }
  }

  @Test
  fun signUp_signUpButtonNavigatesToChooseRoleScreen() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }
    composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@test.com")
    composeTestRule.onNodeWithTag("passwordInputField").performTextInput("password")
    composeTestRule.onNodeWithTag("confirmPasswordInputField").performTextInput("password")

    composeTestRule.onNodeWithTag("signUpButton").performClick()
    verify(mockNavigationActions).navigateTo(Screen.SIGN_UP_CHOOSE_ROLE)
  }
}

class SignUpButtonTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    // Mock the Toast.makeText function to intercept the Toast messages
    mockkStatic(Toast::class)
    every { Toast.makeText(any(), any<String>(), any()) } answers { mockk(relaxed = true) }
  }

  @Test
  fun signUpButton_testShowToastWhenFieldsIncomplete() {
    composeTestRule.setContent {
      SignUpButton(
          onClick = {},
          isComplete = false,
          goodFormEmail = true,
          passwordLengthComplete = true,
          samePassword = true)
    }
    composeTestRule.onNodeWithTag("signUpButton").performClick()

    verify { Toast.makeText(any(), "Please fill in all required fields", Toast.LENGTH_SHORT) }
  }

  @Test
  fun signUpButton_testShowToastForInvalidEmailFormat() {
    composeTestRule.setContent {
      SignUpButton(
          onClick = {},
          isComplete = true,
          goodFormEmail = false,
          passwordLengthComplete = true,
          samePassword = true)
    }
    composeTestRule.onNodeWithTag("signUpButton").performClick()

    verify { Toast.makeText(any(), "Your email must have \"@\" and \".\"", Toast.LENGTH_SHORT) }
  }

  @Test
  fun signUpButton_testShowToastForNonMatchingPasswords() {
    composeTestRule.setContent {
      SignUpButton(
          onClick = {},
          isComplete = true,
          goodFormEmail = true,
          passwordLengthComplete = true,
          samePassword = false)
    }
    composeTestRule.onNodeWithTag("signUpButton").performClick()

    verify {
      Toast.makeText(any(), "Password and Confirm Password must be the same", Toast.LENGTH_SHORT)
    }
  }

  @Test
  fun signUpButton_testShowToastForShortPassword() {
    composeTestRule.setContent {
      SignUpButton(
          onClick = {},
          isComplete = true,
          goodFormEmail = true,
          passwordLengthComplete = false,
          samePassword = true)
    }
    composeTestRule.onNodeWithTag("signUpButton").performClick()

    verify {
      Toast.makeText(any(), "Your password must have at least 6 characters", Toast.LENGTH_SHORT)
    }
  }

  @Test
  fun signUpButton_testShowToastForSuccessfulSignUp() {
    composeTestRule.setContent {
      SignUpButton(
          onClick = {},
          isComplete = true,
          goodFormEmail = true,
          passwordLengthComplete = true,
          samePassword = true)
    }
    composeTestRule.onNodeWithTag("signUpButton").performClick()

    verify { Toast.makeText(any(), "You are Signed up!", Toast.LENGTH_SHORT) }
  }
}
