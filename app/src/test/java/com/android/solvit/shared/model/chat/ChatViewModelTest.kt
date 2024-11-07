package com.android.solvit.shared.model.chat

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify

class ChatViewModelTest {
  private lateinit var chatRepository: ChatRepository
  private lateinit var chatViewModel: ChatViewModel

  @Before
  fun setUp() {
    chatRepository = mock(ChatRepository::class.java)
    chatViewModel = ChatViewModel(chatRepository)
  }

  @Test
  fun initChat() {
    chatViewModel.setReceiverUid("1234")
    chatViewModel.initChat()
    verify(chatRepository).initChat(any(), any())
  }

  @Test
  fun sendMessage() {
    chatViewModel.setReceiverUid("1234")

    // Simulate initChat behavior to set chatId
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as (String) -> Unit
          onSuccess("testChatId")
          null
        }
        .`when`(chatRepository)
        .initChat(any(), any())

    chatViewModel.initChat()

    chatViewModel.sendMessage(ChatMessage("Message 1", "Hello", System.currentTimeMillis()))
    verify(chatRepository).sendMessage(any(), any(), any(), any())
  }

  @Test
  fun getMessages() {
    chatViewModel.setReceiverUid("1234")

    // Simulate initChat behavior to set chatId
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as (String) -> Unit
          onSuccess("testChatId")
          null
        }
        .`when`(chatRepository)
        .initChat(any(), any())

    chatViewModel.initChat()
    chatViewModel.getConversation()
    verify(chatRepository).listenForMessages(any(), any(), any())
  }
}
