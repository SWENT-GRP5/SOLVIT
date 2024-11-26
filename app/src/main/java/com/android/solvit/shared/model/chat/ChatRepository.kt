package com.android.solvit.shared.model.chat

interface ChatRepository {
  fun initChat(onSuccess: (String) -> Unit, receiverUid: String)

  fun sendMessage(
      chatRoomId: String,
      message: ChatMessage.TextMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun listenForMessages(
      chatRoomId: String,
      onSuccess: (List<ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  )

  fun listenForLastMessages(
      onSuccess: (Map<String?, ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  )
}
