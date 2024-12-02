package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AiSolverChat {
  @get:Rule val composeTestRule = createComposeRule()

  @Before fun SetUp() {}

  @Test
  fun AllComponentsAreDisplayedAiWelcomeScreen() {
    composeTestRule.setContent { AiSolverWelcomeScreen() }
    composeTestRule.onNodeWithTag("AiGetStartedScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("getStartedButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("getStartedButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("image").assertIsDisplayed()
    composeTestRule.onNodeWithTag("title").assertIsDisplayed()
  }

  @Test
  fun AllComponentsAreDisplayedAiChatScreen() {
    composeTestRule.setContent { AiSolverScreen() }

    composeTestRule.onNodeWithTag("AiSolverScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AiChatHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SendMessageBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadImageButton").assertHasClickAction()
  }
}
