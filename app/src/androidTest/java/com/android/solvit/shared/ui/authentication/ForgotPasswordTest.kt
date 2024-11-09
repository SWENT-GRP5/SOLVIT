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
class ForgotPasswordTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = mock(NavigationActions::class.java)

  @Test
  fun signUpChooseProfile_displaysAllComponents() {
    composeTestRule.setContent { ForgotPassword(mockNavigationActions) }

    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("forgotPasswordImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bigText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Send reset link").assertIsDisplayed()
  }

  @Test
  fun signUpChooseProfile_performClick() {
    composeTestRule.setContent { ForgotPassword(mockNavigationActions) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.onNodeWithTag("emailInputField").performClick()
    composeTestRule.onNodeWithTag("Send reset link").performClick()

    verify(mockNavigationActions).goBack()
  }
}
