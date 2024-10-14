package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class OpeningScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = Mockito.mock(NavigationActions::class.java)

  @Test
  fun testOpeningScreen_displaysAllComponents() {
    composeTestRule.setContent { OpeningScreen(mockNavigationActions) }

    // Test the display of UI components
    composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tagline").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ctaButton").assertIsDisplayed()
  }

  @Test
  fun testTapToContinue_navigatesToSignInScreen() {
    composeTestRule.setContent { OpeningScreen(mockNavigationActions) }

    // Perform click on "Tap to Continue" and verify navigation to Sign In screen
    composeTestRule.onNodeWithTag("ctaButton").performClick()

    // Verify that the navigation action was triggered
    Mockito.verify(mockNavigationActions).navigateTo(Screen.SIGN_IN)
  }
}
