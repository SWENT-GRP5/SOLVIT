package com.android.solvit.shared.model.chat

interface ChatRepository {
  fun initChat(
      currentUserUid: String?,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit,
      receiverUid: String
  )

  fun linkChatToRequest(
      chatRoomId: String,
      serviceRequestId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun getChatRequest(chatRoomId: String, onSuccess: (String) -> Unit, onFailure: () -> Unit)

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
      currentUserUid: String?,
      onSuccess: (Map<String?, ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  )
}
