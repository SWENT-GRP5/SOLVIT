package com.android.solvit.shared.model.chat

interface ChatRepository {
  fun initChat(
      isIaMessage: Boolean,
      currentUserUid: String?,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit,
      receiverUid: String
  )

  fun sendMessage(
      isIaMessage: Boolean,
      chatRoomId: String,
      message: ChatMessage.TextMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun listenForMessages(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: (List<ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  )

  fun listenForLastMessages(
      currentUserUid: String?,
      onSuccess: (Map<String?, ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  )

  fun clearConversation(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )
}
