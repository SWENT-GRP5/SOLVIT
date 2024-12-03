package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class AiSolverChat {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun SetUp() {
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
  }

  @Test
  fun AllComponentsAreDisplayedAiWelcomeScreen() {
    composeTestRule.setContent { AiSolverWelcomeScreen(navigationActions) }
    composeTestRule.onNodeWithTag("AiGetStartedScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("getStartedButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("getStartedButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("image").assertIsDisplayed()
    composeTestRule.onNodeWithTag("title").assertIsDisplayed()
  }

  @Test
  fun AllComponentsAreDisplayedAiChatScreen() {
    composeTestRule.setContent { AiSolverScreen(navigationActions) }
    composeTestRule.onNodeWithTag("AiSolverScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AiChatHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SendMessageBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadImageButton").assertHasClickAction()
  }
}
