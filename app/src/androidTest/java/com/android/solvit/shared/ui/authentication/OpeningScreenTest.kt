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
  fun testOpeningScreen_Portrait_displaysAllComponents() {
    composeTestRule.setContent { OpeningScreenPortrait(mockNavigationActions) }

    // Test the display of UI components
    composeTestRule.onNodeWithTag("appLogoPortrait").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appNamePortrait").assertIsDisplayed()
    composeTestRule.onNodeWithTag("taglinePortrait").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ctaButtonPortrait").assertIsDisplayed()
  }

  @Test
  fun testOpeningScreen_Landscape_displaysAllComponents() {
    composeTestRule.setContent { OpeningScreenLandscape(mockNavigationActions) }

    composeTestRule.onNodeWithTag("appLogoLandscape").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appNameLandscape").assertIsDisplayed()
    composeTestRule.onNodeWithTag("taglineLandscape").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ctaButtonLandscape").assertIsDisplayed()
  }

  @Test
  fun testTapToContinue_Portrait_navigatesToSignInScreen() {
    composeTestRule.setContent { OpeningScreenPortrait(mockNavigationActions) }

    // Perform click on "Tap to Continue" and verify navigation to Sign In screen
    composeTestRule.onNodeWithTag("ctaButtonPortrait").performClick()

    // Verify that the navigation action was triggered
    Mockito.verify(mockNavigationActions).navigateTo(Screen.SIGN_IN)
  }

  @Test
  fun testTapToContinue_Landscape_navigatesToSignInScreen() {
    composeTestRule.setContent { OpeningScreenLandscape(mockNavigationActions) }

    // Perform click on "Tap to Continue" and verify navigation to Sign In screen
    composeTestRule.onNodeWithTag("ctaButtonLandscape").performClick()

    // Verify that the navigation action was triggered
    Mockito.verify(mockNavigationActions).navigateTo(Screen.SIGN_IN)
  }
}
