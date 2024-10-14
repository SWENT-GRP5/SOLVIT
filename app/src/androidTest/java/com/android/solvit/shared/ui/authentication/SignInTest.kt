package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertTrue
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
    composeTestRule.setContent {
      SignInScreen(mockNavigationActions)
    }

    // Test that the checkbox is clickable and can toggle between states
    composeTestRule.onNodeWithTag("rememberMeCheckbox").performClick()
    composeTestRule.onNodeWithTag("forgotPasswordLink").performClick()
    composeTestRule.onNodeWithTag("signInButton").performClick()
    composeTestRule.onNodeWithTag("googleSignInButton").performClick() }

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

  private val mockGoogleSignInAccount = Mockito.mock(GoogleSignInAccount::class.java)

  @Test
  fun testsigninerrorwithFirebase() = runBlocking {
    val auth = Mockito.mock(FirebaseAuth::class.java)
    val mockCredential = Mockito.mock(AuthCredential::class.java)

    // Mock the account ID token
    Mockito.`when`(mockGoogleSignInAccount.idToken).thenReturn("test_token")
    val firebaseAuthException =
        FirebaseAuthInvalidCredentialsException(
            "ERROR",
            "The supplied auth credential is incorrect, malformed or has expired. [ Unable to parse Google id_token: test_token ]")
    // Create a variable to track the result
    var result: AuthResult? = null
    var exception: Exception? = null

    // Mock the Task to throw an exception
    val mockTask = Mockito.mock(Task::class.java) as Task<AuthResult>
    Mockito.`when`(mockTask.isSuccessful).thenReturn(false)
    Mockito.`when`(mockTask.exception).thenReturn(firebaseAuthException)
    Mockito.`when`(auth.signInWithCredential(mockCredential)).thenReturn(mockTask)

    signInWithFirebase(
        account = mockGoogleSignInAccount,
        onAuthComplete = { authResult -> result = authResult },
        onAuthError = { e -> exception = e })
    // Assert that an exception was thrown and no auth result was received
    assertEquals(null, result)
    assertTrue(exception is FirebaseAuthInvalidCredentialsException)
    assertEquals(firebaseAuthException.message, exception?.message)
  }
}
