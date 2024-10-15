package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
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
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("welcomeText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("password").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rememberMeCheckbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("forgotPasswordLink").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signInButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleSignInButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpLink").assertIsDisplayed()
  }

  @Test
  fun testSignInScreen_emailAndPasswordInput() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test email input
    composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("password").performTextInput("password123")
  }

  @Test
  fun performClick() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test that the checkbox is clickable and can toggle between states
    composeTestRule.onNodeWithTag("rememberMeCheckbox").performClick()
    composeTestRule.onNodeWithTag("forgotPasswordLink").performClick()
    composeTestRule.onNodeWithTag("signInButton").performClick()
    composeTestRule.onNodeWithTag("googleSignInButton").performClick()
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
