package com.android.solvit.shared.model.chat

import android.net.Uri

interface ChatRepository {
  fun initChat(
      isIaMessage: Boolean,
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
      isIaMessage: Boolean,
      chatRoomId: String,
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun listenForMessages(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: (List<ChatMessage>) -> Unit,
      onFailure: () -> Unit
  )

  fun listenForLastMessages(
      currentUserUid: String?,
      onSuccess: (Map<String?, ChatMessage>) -> Unit,
      onFailure: () -> Unit
  )

  fun clearConversation(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun uploadChatImagesToStorage(
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun seekerShouldCreateRequest(
      chatRoomId: String,
      shouldCreateRequest: Boolean,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  fun getShouldCreateRequest(
      chatRoomId: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: () -> Unit
  )
}
