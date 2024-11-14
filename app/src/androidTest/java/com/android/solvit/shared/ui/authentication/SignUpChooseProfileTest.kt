package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SignUpChooseProfileTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = mock(NavigationActions::class.java)

  @Test
  fun signUpChooseProfile_displaysAllComponents() {
    composeTestRule.setContent { SignUpChooseProfile(mockNavigationActions) }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("roleIllustration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpAsTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seekerButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providerButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("learnMoreLink").assertIsDisplayed()
  }

  @Test
  fun signUpChooseProfile_performClick() {
      composeTestRule.setContent { SignUpChooseProfile(mockNavigationActions) }

      composeTestRule.onNodeWithTag("goBackButton").performClick()
      composeTestRule.onNodeWithTag("seekerButton").performClick()
      composeTestRule.onNodeWithTag("providerButton").performClick()
      composeTestRule.onNodeWithTag("learnMoreLink").performClick()

      verify(mockNavigationActions).goBack()
  }
}
