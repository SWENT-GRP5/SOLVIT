package com.android.solvit.shared.model.chat

sealed class ChatMessage(
    val id: String,
    open val senderName: String,
    open val senderId: String,
    open val timestamp: Long = System.currentTimeMillis(),
    open val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
) {
  data class TextMessage(
      val message: String = "",
      override val senderName: String = "",
      override val senderId: String = "",
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, senderName, timestamp, status) {
    // Provide a no-argument constructor
    companion object {
      @JvmStatic fun default() = TextMessage()
    }
  }

  data class ImageMessage(
      val imageUrl: String = "",
      override val senderName: String = "",
      override val senderId: String = "",
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, senderName, timestamp, status) {
    companion object {
      @JvmStatic fun default() = ImageMessage()
    }
  }
}
