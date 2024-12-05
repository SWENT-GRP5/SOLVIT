package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.AiSolverViewModel
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class AiSolverChat {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  private lateinit var authRep: AuthRep
  private lateinit var chatRepository: ChatRepository

  private lateinit var authViewModel: AuthViewModel
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var aiSolverViewModel: AiSolverViewModel

  @Before
  fun SetUp() {
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    authRep = mock(AuthRep::class.java)
    authViewModel = AuthViewModel(authRep)

    chatRepository = mock(ChatRepository::class.java)
    chatViewModel = ChatViewModel(chatRepository)

    aiSolverViewModel = AiSolverViewModel()
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
    composeTestRule.setContent {
      AiSolverScreen(navigationActions, authViewModel, chatViewModel, aiSolverViewModel)
    }
    composeTestRule.onNodeWithTag("AiSolverScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AiChatHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SendMessageBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadImageButton").assertHasClickAction()
  }
}
