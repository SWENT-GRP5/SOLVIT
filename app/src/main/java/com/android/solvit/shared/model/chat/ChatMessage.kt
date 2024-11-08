package com.android.solvit.shared.model.chat

sealed class ChatMessage(
    val id: String,
    open val senderId: String,
    open val timestamp: Long = System.currentTimeMillis(),
    open val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
) {
  data class TextMessage(
      val message: String,
      override val senderId: String,
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, timestamp, status)

  data class ImageMessage(
      val imageUrl: String,
      override val senderId: String,
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, timestamp, status)
}
