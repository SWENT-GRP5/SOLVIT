package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatAssistantDialogTest {

  private lateinit var chatAssistantViewModel: ChatAssistantViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    chatAssistantViewModel = ChatAssistantViewModel()
    composeTestRule.setContent { ChatAssistantDialog(chatAssistantViewModel, true, {}, {}) }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("aiLogo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tonesRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("generateButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("modeSelectionRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("generationText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("translationText").assertIsDisplayed()
  }

  @Test
  fun modeIsSwitchable() {
    composeTestRule.onNodeWithTag("tonesRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("modeSwitch").performClick()
    composeTestRule.onNodeWithTag("languageSelection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageSelectionFrom").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageSelectionTo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("messageField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("modeSwitch").performClick()
    composeTestRule.onNodeWithTag("tonesRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputField").assertIsDisplayed()
  }

  @Test
  fun tonesAreSelectable() {
    for (tone in TONES_LIST) {
      if (composeTestRule.onNodeWithTag("toneItem$tone").isNotDisplayed()) {
        composeTestRule.onNodeWithTag("toneItem$tone").performScrollTo()
      }
      composeTestRule.onNodeWithTag("toneItem$tone").assertIsDisplayed()
      composeTestRule.onNodeWithTag("toneItem$tone").performClick()
      composeTestRule.onNodeWithTag("toneItem$tone").performClick()
    }
  }

  @Test
  fun inputFieldIsEditable() {
    composeTestRule.onNodeWithTag("inputField").performClick()
    composeTestRule.onNodeWithTag("inputField").performTextInput("test")
    composeTestRule.onNodeWithTag("inputField").assertTextContains("test")
  }

  @Test
  fun languagesAreSelectable() {
    composeTestRule.onNodeWithTag("modeSwitch").performClick()
    composeTestRule.onNodeWithTag("languageSelectionFrom").performClick()
    composeTestRule.onNodeWithTag("languageItemENGLISH").performClick()
    composeTestRule.onNodeWithTag("languageSelectionTo").performClick()
    composeTestRule.onNodeWithTag("languageItemENGLISH").performClick()
    composeTestRule.onNodeWithTag("modeSwitch").performClick()
  }

  @Test
  fun messageFieldIsEditable() {
    composeTestRule.onNodeWithTag("modeSwitch").performClick()
    composeTestRule.onNodeWithTag("messageField").performClick()
    composeTestRule.onNodeWithTag("messageField").performTextInput("test")
    composeTestRule.onNodeWithTag("messageField").assertTextContains("test")
    composeTestRule.onNodeWithTag("modeSwitch").performClick()
  }
}
