package com.android.solvit.shared.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.chat.MESSAGE_STATUS
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class MessageBoxTest {
  private lateinit var chatRepository: ChatRepository
  private lateinit var authRep: AuthRep
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  private lateinit var providerRepositoryFirestore: ProviderRepository
  private lateinit var userRepository: UserRepository

  private lateinit var chatViewModel: ChatViewModel
  private lateinit var authViewModel: AuthViewModel
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  @get:Rule val composeTestRule = createComposeRule()

  // Create a list of TextMessage instances
  private val testMessages =
      mapOf(
          "Bob" to
              ChatMessage.TextMessage(
                  message = "Hello, this is the first test message.",
                  senderName = "Alice",
                  senderId = "user_alice",
                  timestamp = 1620000000000L,
                  status = MESSAGE_STATUS.SENT),
          "Bob" to
              ChatMessage.TextMessage(
                  message = "This is the second test message.",
                  senderName = "Bob",
                  senderId = "user_bob",
                  timestamp = 1620000005000L,
                  status = MESSAGE_STATUS.DELIVERED),
          "Bob" to
              ChatMessage.TextMessage(
                  message = "Third message in our test list.",
                  senderName = "Charlie",
                  senderId = "user_charlie",
                  timestamp = 1620000010000L,
                  status = MESSAGE_STATUS.READ),
          "Bob" to
              ChatMessage.TextMessage(
                  message = "Fourth message, testing purposes.",
                  senderName = "Alice",
                  senderId = "user_alice",
                  timestamp = 1620000015000L,
                  status = MESSAGE_STATUS.SENT),
          "Bob" to
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
    userRepository = mock(UserRepository::class.java)
    providerRepositoryFirestore = mock(ProviderRepository::class.java)
    authRep = mock(AuthRep::class.java)
    chatViewModel = ChatViewModel(chatRepository)
    authViewModel = AuthViewModel(authRep)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    listProviderViewModel = ListProviderViewModel(providerRepositoryFirestore)

    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    var message: ChatMessage.TextMessage? = null

    `when`(chatRepository.initChat(any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(String) -> Unit>(2)
      onSuccess("chatId") // Simulate success
    }
  }

  @Test
  fun emptyMessageListDisplaysNoMessagesScreen() = runTest {
    `when`(chatRepository.listenForLastMessages(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (Map<String, ChatMessage>) -> Unit
      onSuccess(emptyMap()) // Simulate successful response
      null
    }

    // Act
    //
    // advanceUntilIdle()

    // Set up the UI with the ViewModel
    composeTestRule.setContent {
      MessageBox(
          chatViewModel,
          navigationActions,
          authViewModel,
          listProviderViewModel,
          seekerProfileViewModel)
    }
    chatViewModel.getAllLastMessages("currentUserUid")
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag("InboxTopAppBar").isDisplayed()
    }

    composeTestRule.onNodeWithTag("InboxTopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ChatListItem").assertDoesNotExist()
    composeTestRule.onNodeWithText("No messages yet").assertIsDisplayed()
    composeTestRule.onNodeWithText("Send your first message").assertIsDisplayed()
  }

  // Check that overview of all user messages is well displayed
  @Test
  fun messagesAreDisplayedCorrectly() {

    `when`(chatRepository.listenForLastMessages(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Map<String, ChatMessage>) -> Unit>(1)
      onSuccess(testMessages) // Simulate success
    }

    composeTestRule.setContent {
      MessageBox(
          chatViewModel,
          navigationActions,
          authViewModel,
          listProviderViewModel,
          seekerProfileViewModel)
    }
    chatViewModel.getAllLastMessages("currentUserId")

    composeTestRule.onNodeWithTag("InboxTopAppBar").assertIsDisplayed()
    assertEquals(
        composeTestRule.onAllNodesWithTag("ChatListItem").fetchSemanticsNodes().size,
        testMessages.size)
  }
}
