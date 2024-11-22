package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.chat.MESSAGE_STATUS
import com.android.solvit.shared.ui.navigation.NavigationActions
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
  @get:Rule val composeTestRule = createComposeRule()

  private val email = "test2@provider.ch"
  private val password = "password"

  @Before
  fun SetUp() {

    // Initialize Firebase App

    chatRepository = mock(ChatRepository::class.java)
    authRep = mock(AuthRep::class.java)
    chatViewModel = ChatViewModel(chatRepository)
    authViewModel = AuthViewModel(authRep)

    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    // Create a list of TextMessage instances
    val testMessages =
        listOf(
            ChatMessage.TextMessage(
                message = "Hello, this is the first test message.",
                senderName = "Alice",
                senderId = "user_alice",
                timestamp = 1620000000000L,
                status = MESSAGE_STATUS.SENT),
            ChatMessage.TextMessage(
                message = "This is the second test message.",
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

    var message: ChatMessage.TextMessage? = null

    `when`(chatRepository.listenForLastMessages(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ChatMessage.TextMessage>) -> Unit>(0)
      onSuccess(listOf(testMessages[0])) // Simulate success
    }

    `when`(chatRepository.listenForMessages(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ChatMessage.TextMessage>) -> Unit>(1)
      onSuccess(testMessages) // Simulate success
    }
    chatViewModel.getConversation()
  }

  @Test
  fun AllComponentsAreDisplayed() {

    composeTestRule.setContent { ChatScreen(navigationActions, chatViewModel, authViewModel) }

    chatViewModel.getConversation()
    composeTestRule.onNodeWithTag("ChatHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SendMessageBar").assertIsDisplayed()
  }
}
