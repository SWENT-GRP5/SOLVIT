package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.chat.MESSAGE_STATUS
import com.android.solvit.shared.ui.navigation.NavigationActions
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ChatScreenTest {
  private lateinit var chatRepository: ChatRepository
  private lateinit var authRep: AuthRep
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  private lateinit var chatViewModel: ChatViewModel
  private lateinit var authViewModel: AuthViewModel
  private lateinit var chatAssistantViewModel: ChatAssistantViewModel
  @get:Rule val composeTestRule = createComposeRule()

  // Create a list of TextMessage instances
  private val testMessages =
      listOf(
          ChatMessage.TextMessage(
              message = "Hello, this is the first test message.",
              senderName = "Alice",
              senderId = "user_alice",
              timestamp = 1620000000000L,
              status = MESSAGE_STATUS.SENT),
          ChatMessage.TextImageMessage(
              imageUrl = "",
              text = "This is the second test message.",
              senderName = "Bob",
              senderId = "user_bob",
              timestamp = 1620000005000L,
              status = MESSAGE_STATUS.DELIVERED),
          ChatMessage.TextMessage(
              message = "Third message in our test list.",
              senderName = "Charlie",
              senderId = "user_charlie",
              timestamp = 1620000010000L,
              status = MESSAGE_STATUS.READ),
          ChatMessage.TextMessage(
              message = "Fourth message, testing purposes.",
              senderName = "Alice",
              senderId = "user_alice",
              timestamp = 1620000015000L,
              status = MESSAGE_STATUS.SENT),
          ChatMessage.TextMessage(
              message = "Fifth and final test message.",
              senderName = "Bob",
              senderId = "user_bob",
              timestamp = 1620000020000L,
              status = MESSAGE_STATUS.DELIVERED))

  @Before
  fun SetUp() {

    // Initialize Firebase App

    chatRepository = mock(ChatRepository::class.java)
    authRep = mock(AuthRep::class.java)
    chatViewModel = ChatViewModel(chatRepository)
    authViewModel = AuthViewModel(authRep)
    chatAssistantViewModel = ChatAssistantViewModel()

    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    var message: ChatMessage.TextMessage? = null

    `when`(chatRepository.initChat(any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(String) -> Unit>(2)
      onSuccess("chatId") // Simulate success
    }
    `when`(chatRepository.listenForLastMessages(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ChatMessage>) -> Unit>(2)
      onSuccess(listOf(testMessages[0])) // Simulate success
    }

    `when`(chatRepository.listenForMessages(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ChatMessage>) -> Unit>(2)
      onSuccess(testMessages) // Simulate success
    }
  }

  // Check that All Components are display and messages are retrieved correctly
  @Test
  fun AllComponentsAreDisplayed() = runTest {
    composeTestRule.setContent {
      ChatScreen(navigationActions, chatViewModel, authViewModel, chatAssistantViewModel)
    }

    chatViewModel.setReceiverUid("1234")
    chatViewModel.initChat(false, "123")
    chatViewModel.getConversation(false)

    composeTestRule.onNodeWithTag("ChatHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SendMessageBar").assertIsDisplayed()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("MessageItem").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("enterText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enterText").performTextInput("Hello")
    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    assertEquals(
        testMessages.size,
        composeTestRule.onAllNodesWithTag("MessageItem").fetchSemanticsNodes().size)
  }

  @Test
  fun typingIndicatorTest() {
    composeTestRule.setContent { TypingIndicator() }
    composeTestRule.onNodeWithTag("TypingIndicator").assertIsDisplayed()
  }
}
