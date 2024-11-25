package com.android.solvit.shared.ui.chat

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.chat.MESSAGE_STATUS
import com.android.solvit.shared.ui.navigation.NavigationActions
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class ChatScreenTest {
  private lateinit var chatRepository: ChatRepository
  private lateinit var authRep: AuthRep
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  private lateinit var chatViewModel: ChatViewModel
  private lateinit var authViewModel: AuthViewModel
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

  @Before
  fun SetUp() {

    // Initialize Firebase App

    chatRepository = mock(ChatRepository::class.java)
    authRep = mock(AuthRep::class.java)
    chatViewModel = ChatViewModel(chatRepository)
    authViewModel = AuthViewModel(authRep)

    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    var message: ChatMessage.TextMessage? = null

    // Mock the `init` method
    doAnswer { invocation ->
          val callback = invocation.getArgument<(User?) -> Unit>(0)
          callback(User(uid = "user_alice", role = "Provider")) // Pass the mocked user
        }
        .whenever(authRep)
        .init(any())

    `when`(chatRepository.listenForLastMessages(any(), any())).thenAnswer {
      println("je suis rentré")
      val onSuccess = it.getArgument<(List<ChatMessage.TextMessage>) -> Unit>(0)
      onSuccess(listOf(testMessages[0])) // Simulate success
    }

    `when`(chatRepository.listenForMessages(any(), any(), any())).thenAnswer {
      Log.e("Test", "Je suis rentré")
      val onSuccess = it.getArgument<(List<ChatMessage.TextMessage>) -> Unit>(1)
      onSuccess(testMessages) // Simulate success
    }
    `when`(chatRepository.initChat(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(String) -> Unit>(0)
      onSuccess("chatId") // Simulate success
    }
  }

  // Check that All Components are display and messages are retrieved correctly
  @Test
  fun AllComponentsAreDisplayed() {

    composeTestRule.setContent { ChatScreen(navigationActions, chatViewModel, authViewModel) }

    chatViewModel.setReceiverUid("1234")
    chatViewModel.initChat()
    chatViewModel.getConversation()

    composeTestRule.onNodeWithTag("ChatHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SendMessageBar").assertIsDisplayed()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("MessageItem").fetchSemanticsNodes().isNotEmpty()
    }

    assertEquals(
        testMessages.size,
        composeTestRule.onAllNodesWithTag("MessageItem").fetchSemanticsNodes().size)
  }
}
