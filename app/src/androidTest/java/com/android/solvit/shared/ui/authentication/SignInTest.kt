package com.android.solvit.shared.ui.authentication

import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val intentsTestRule = IntentsRule()

  private val mockNavigationActions = mock(NavigationActions::class.java)

  @Test
  fun testSignIn_displaysAllComponents() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test the display of UI components
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcomeText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("passwordInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rememberMeCheckbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("forgotPasswordLink").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signInButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleSignInButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpLink").assertIsDisplayed()
    composeTestRule.onNodeWithTag("portraitLayout").assertIsDisplayed()
  }

  @Test
  fun testSignIn_emailAndPasswordInput() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test email input
    composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password123")
  }

  @Test
  fun testSignIn_errorShowInEmailField() {
      runBlocking {
          composeTestRule.setContent { SignInScreen(mockNavigationActions) }

          composeTestRule.onNodeWithTag("emailErrorMessage").isNotDisplayed()
          composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example")
          composeTestRule.onNodeWithTag("emailErrorMessage").isDisplayed()
          composeTestRule.onNodeWithTag("emailInput").performTextClearance()
          composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
          composeTestRule.onNodeWithTag("emailErrorMessage").isNotDisplayed()
      }
  }

  @Test
  fun testSignIn_errorShowInPasswordField() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("passwordErrorMessage").isNotDisplayed()
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("12345")
    composeTestRule.onNodeWithTag("passwordErrorMessage").isDisplayed()
    composeTestRule.onNodeWithTag("passwordInput").performTextClearance()
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("123456")
    composeTestRule.onNodeWithTag("passwordErrorMessage").isNotDisplayed()
  }

  @Test
  fun testSignIn_performClick() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test that the checkbox is clickable and can toggle between states
    composeTestRule.onNodeWithTag("rememberMeCheckbox").performClick()
    composeTestRule.onNodeWithTag("forgotPasswordLink").performClick()
    composeTestRule.onNodeWithTag("signInButton").performClick()
    composeTestRule.onNodeWithTag("googleSignInButton").performClick()
  }

  @Test
  fun testSignIn_googleSignInReturnsValidActivityResult() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }
    composeTestRule.onNodeWithTag("googleSignInButton").performClick()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    Intents.intended(IntentMatchers.toPackage("com.google.android.gms"))
  }

  suspend fun signInWithFirebase(
      account: GoogleSignInAccount,
      onAuthComplete: (AuthResult) -> Unit,
      onAuthError: (Exception) -> Unit
  ) {
    try {
      val idToken = account.idToken ?: throw IllegalArgumentException("Google ID token is null")
      val credential = GoogleAuthProvider.getCredential(idToken, null)
      val authResult = Firebase.auth.signInWithCredential(credential).await()
      onAuthComplete(authResult)
    } catch (e: Exception) {
      onAuthError(e)
    }
  }
}

class SignInButtonTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val authViewModel = mockk<AuthViewModel>(relaxed = true)
  private val onSuccess = {}
  private val onFailure = {}

  @Before
  fun setup() {
    // Mock the Toast.makeText function to intercept the Toast messages
    mockkStatic(Toast::class)
    every { Toast.makeText(any(), any<String>(), any()) } answers { mockk(relaxed = true) }
  }

  @Test
  fun testSignIn_testShowToastWhenFieldsIncomplete() {
    composeTestRule.setContent {
      SignInButton(
          email = "",
          password = "",
          isFormComplete = false,
          goodFormEmail = true,
          passwordLengthComplete = true,
          authViewModel = authViewModel,
          onSuccess = onSuccess,
          onFailure = onFailure)
    }

    composeTestRule.onNodeWithTag("signInButton").performClick()

    verify { Toast.makeText(any(), "Please fill in all required fields", Toast.LENGTH_SHORT) }
  }

  @Test
  fun testSignIn_testShowToastForInvalidEmailFormat() {
    composeTestRule.setContent {
      SignInButton(
          email = "invalidemail",
          password = "password123",
          isFormComplete = true,
          goodFormEmail = false,
          passwordLengthComplete = true,
          authViewModel = authViewModel,
          onSuccess = onSuccess,
          onFailure = onFailure)
    }

    composeTestRule.onNodeWithTag("signInButton").performClick()

    verify { Toast.makeText(any(), "Your email must have \"@\" and \".\"", Toast.LENGTH_SHORT) }
  }

  @Test
  fun testSignIn_testShowToastForShortPassword() {
    composeTestRule.setContent {
      SignInButton(
          email = "test@example.com",
          password = "123",
          isFormComplete = true,
          goodFormEmail = true,
          passwordLengthComplete = false,
          authViewModel = authViewModel,
          onSuccess = onSuccess,
          onFailure = onFailure)
    }

    composeTestRule.onNodeWithTag("signInButton").performClick()

    verify {
      Toast.makeText(any(), "Your password must have at least 6 characters", Toast.LENGTH_SHORT)
    }
  }

  @Test
  fun testSignIn_testAuthIsCalledOnValidForm() {
    composeTestRule.setContent {
      SignInButton(
          email = "test@example.com",
          password = "password123",
          isFormComplete = true,
          goodFormEmail = true,
          passwordLengthComplete = true,
          authViewModel = authViewModel,
          onSuccess = onSuccess,
          onFailure = onFailure)
    }

    composeTestRule.onNodeWithTag("signInButton").performClick()

    verify { authViewModel.setEmail("test@example.com") }
    verify { authViewModel.setPassword("password123") }
    verify { authViewModel.loginWithEmailAndPassword(onSuccess, onFailure) }
  }
}

class SignInScreenLandscapeTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val mockNavigationActions = mock(NavigationActions::class.java)

  @Before
  fun setUp() {
    composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
  }

  @Test
  fun testSignInLandscape_displaysAllComponents() {

    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test the display of UI components
    composeTestRule.onNodeWithTag("landscapeLayout").assertIsDisplayed()
    composeTestRule.onNodeWithTag("leftColumnLandScape").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rightColumnLandScape").assertIsDisplayed()
  }
}
