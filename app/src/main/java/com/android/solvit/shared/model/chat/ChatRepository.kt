package com.android.solvit.shared.model.chat

import android.net.Uri

/**
 * Interface defining the operations for managing chat functionality within the application. This
 * includes chat initialization, message handling, linking chats to requests, and listening for chat
 * updates in real time.
 */
interface ChatRepository {
  /**
   * Initializes a chat between users or with the AI system.
   *
   * @param isIaMessage Indicates if the chat is AI-driven.
   * @param currentUserUid The UID of the current user initializing the chat.
   * @param onSuccess Callback invoked with the created chat room ID upon successful initialization.
   * @param onFailure Callback invoked upon failure.
   * @param receiverUid The UID of the user receiving the messages.
   */
  fun initChat(
      isIaMessage: Boolean,
      currentUserUid: String?,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit,
      receiverUid: String
  )

  /**
   * Links a chat room to a specific service request.
   *
   * @param chatRoomId The unique identifier for the chat room.
   * @param serviceRequestId The unique identifier for the service request.
   * @param onSuccess Callback invoked upon successful linking.
   * @param onFailure Callback invoked upon failure.
   */
  fun linkChatToRequest(
      chatRoomId: String,
      serviceRequestId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  /**
   * Retrieves the associated service request for a specific chat room.
   *
   * @param chatRoomId The unique identifier for the chat room.
   * @param onSuccess Callback invoked with the service request ID upon success.
   * @param onFailure Callback invoked upon failure.
   */
  fun getChatRequest(chatRoomId: String, onSuccess: (String) -> Unit, onFailure: () -> Unit)

  /**
   * Sends a chat message to the specified chat room.
   *
   * @param isIaMessage Indicates if the message is from the AI system.
   * @param chatRoomId The unique identifier for the chat room.
   * @param message The chat message to be sent.
   * @param onSuccess Callback invoked upon successful sending.
   * @param onFailure Callback invoked upon failure.
   */
  fun sendMessage(
      isIaMessage: Boolean,
      chatRoomId: String,
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  /**
   * Listens for incoming messages in a specified chat room.
   *
   * @param isIaConversation Indicates if the conversation involves AI.
   * @param chatRoomId The unique identifier for the chat room.
   * @param onSuccess Callback invoked with the list of new messages upon success.
   * @param onFailure Callback invoked upon failure.
   */
  fun listenForMessages(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: (List<ChatMessage>) -> Unit,
      onFailure: () -> Unit
  )

  /**
   * Listens for the last messages across all conversations for the current user.
   *
   * @param currentUserUid The UID of the current user.
   * @param onSuccess Callback invoked with the map of chat rooms and their last messages.
   * @param onFailure Callback invoked upon failure.
   */
  fun listenForLastMessages(
      currentUserUid: String?,
      onSuccess: (Map<String?, ChatMessage>) -> Unit,
      onFailure: () -> Unit
  )

  /**
   * Clears all messages from a specific conversation.
   *
   * @param isIaConversation Indicates if the conversation involves AI.
   * @param chatRoomId The unique identifier for the chat room.
   * @param onSuccess Callback invoked upon successful clearing.
   * @param onFailure Callback invoked upon failure.
   */
  fun clearConversation(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  /**
   * Uploads an image to cloud storage and returns its download URL.
   *
   * @param imageUri The URI of the image to be uploaded.
   * @param onSuccess Callback invoked with the download URL upon success.
   * @param onFailure Callback invoked upon failure.
   */
  fun uploadChatImagesToStorage(
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Sets whether the service request creation prompt should be displayed for a chat room.
   *
   * @param chatRoomId The unique identifier for the chat room.
   * @param shouldCreateRequest A flag indicating if the service request prompt should be shown.
   * @param onSuccess Callback invoked upon successful update.
   * @param onFailure Callback invoked upon failure.
   */
  fun seekerShouldCreateRequest(
      chatRoomId: String,
      shouldCreateRequest: Boolean,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  )

  /**
   * Retrieves the status of the service request creation flag for a chat room.
   *
   * @param chatRoomId The unique identifier for the chat room.
   * @param onSuccess Callback invoked with the flag value upon success.
   * @param onFailure Callback invoked upon failure.
   */
  fun getShouldCreateRequest(
      chatRoomId: String,
      onSuccess: (Boolean) -> Unit,
      onFailure: () -> Unit
  )
}
