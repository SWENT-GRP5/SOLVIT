package com.android.solvit.shared.model.chat

interface ChatRepository {
  fun initChat(onSuccess: (String) -> Unit, receiverUid: String)

  fun sendMessage(
      chatRoomId: String,
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun listenForMessages(
      chatRoomId: String,
      onSuccess: (List<ChatMessage>) -> Unit,
      onFailure: () -> Unit
  )

  fun listenForAllMessages(onSuccess: (List<ChatMessage>) -> Unit, onFailure: () -> Unit)
}
