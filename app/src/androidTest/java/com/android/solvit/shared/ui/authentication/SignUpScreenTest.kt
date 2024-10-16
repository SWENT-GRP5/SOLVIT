package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = mock(NavigationActions::class.java)

  @Test
  fun testAllTheTest() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    // Check if all components are displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpIllustration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("facebookSignUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleSignUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appleSignUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logInLink").assertIsDisplayed()
  }

  @Test
  fun testAllPerformClick() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.onNodeWithTag("facebookSignUpButton").performClick()
    // composeTestRule.onNodeWithTag("googleSignUpButton").performClick()
    composeTestRule.onNodeWithTag("appleSignUpButton").performClick()
    composeTestRule.onNodeWithTag("emailInputField").performClick()
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    composeTestRule.onNodeWithTag("logInLink").performClick()

    verify(mockNavigationActions).goBack()
  }
}
