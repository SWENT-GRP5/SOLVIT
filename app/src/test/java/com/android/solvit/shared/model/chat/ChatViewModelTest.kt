package com.android.solvit.shared.model.chat

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChatViewModelTest {
  private lateinit var chatRepository: ChatRepository
  private lateinit var chatViewModel: ChatViewModel

  @Before
  fun setUp() {
    chatRepository = mock(ChatRepository::class.java)
    chatViewModel = ChatViewModel(chatRepository)
  }

  @Test
  fun initChat() = runTest {
    chatViewModel.setReceiverUid("receiver123")
    val currentUserUid = "currentUserUid"

    // Mock the behavior of repository.initChat
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (String) -> Unit
          val onFailure = invocation.arguments[2] as () -> Unit
          onSuccess("testChatId")
          null
        }
        .whenever(chatRepository)
        .initChat(any(), any(), any(), any())

    chatViewModel.initChat(currentUserUid)
    verify(chatRepository).initChat(eq(currentUserUid), any(), any(), eq("receiver123"))
  }

  @Test
  fun sendMessage() = runTest {
    chatViewModel.setReceiverUid("receiver123")
    val currentUserUid = "currentUserUid"

    // Mock the behavior of repository.initChat
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (String) -> Unit
          val onFailure = invocation.arguments[2] as () -> Unit
          onSuccess("testChatId")
          null
        }
        .whenever(chatRepository)
        .initChat(any(), any(), any(), any())

    chatViewModel.initChat("currentUserUid")

    chatViewModel.sendMessage(
        ChatMessage.TextMessage("Message 1", "senderName", "Hello", System.currentTimeMillis()))
    verify(chatRepository).sendMessage(any(), any(), any(), any())
  }

  @Test
  fun getMessages() = runTest {
    chatViewModel.setReceiverUid("receiver123")
    val currentUserUid = "currentUserUid"

    // Mock the behavior of repository.initChat
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (String) -> Unit
          val onFailure = invocation.arguments[2] as () -> Unit
          onSuccess("testChatId")
          null
        }
        .whenever(chatRepository)
        .initChat(any(), any(), any(), any())

    chatViewModel.initChat("currentUserUid")
    chatViewModel.getConversation()
    verify(chatRepository).listenForMessages(any(), any(), any())
  }

  @Test
  fun getAllMessages() {
    // Check that in the init it indeed retrive last messages
    chatViewModel.getAllLastMessages("currentUserUid")
    verify(chatRepository).listenForLastMessages(any(), any(), any())
  }

  @Test
  fun `setReceiver updates _receiver`() {
    // Act
    val testReceiver = "Test Receiver"
    chatViewModel.setReceiver(testReceiver)

    // Assert
    val receiver = chatViewModel.receiver.value
    assert(receiver == testReceiver)
  }
}
