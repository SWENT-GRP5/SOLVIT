package com.android.solvit.shared.model.chat

/**
 * A sealed class representing different types of chat messages exchanged in a conversation. Each
 * message type contains relevant data, such as sender details, timestamps, and message-specific
 * content.
 *
 * @property id A unique identifier for the message.
 * @property senderName The name of the message sender.
 * @property senderId The unique ID of the message sender.
 * @property timestamp The time when the message was sent (defaults to the current time).
 * @property status The current delivery status of the message (default is `SENT`).
 */
sealed class ChatMessage(
    val id: String,
    open val senderName: String,
    open val senderId: String,
    open val timestamp: Long = System.currentTimeMillis(),
    open val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
) {
  /**
   * A data class representing a message containing both text and an image.
   *
   * @property text The textual content of the message.
   * @property imageUrl The URL of the attached image.
   * @property type A fixed string indicating the message type ("textAndImage").
   */
  data class TextImageMessage(
      val text: String = "",
      val imageUrl: String = "",
      val type: String = "textAndImage",
      override val senderName: String = "",
      override val senderId: String = "",
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, senderName, timestamp, status) {
    /** Provides a default instance of `TextImageMessage` for initialization or testing. */
    companion object {
      @JvmStatic fun default() = TextImageMessage()
    }
  }

  /**
   * A data class representing a message containing only text.
   *
   * @property message The textual content of the message.
   * @property type A fixed string indicating the message type ("text").
   */
  data class TextMessage(
      val message: String = "",
      val type: String = "text",
      override val senderName: String = "",
      override val senderId: String = "",
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, senderName, timestamp, status) {
    /** Provides a default instance of `TextMessage` for initialization or testing. */
    companion object {
      @JvmStatic fun default() = TextMessage()
    }
  }

  /**
   * A data class representing a message containing only an image.
   *
   * @property imageUrl The URL of the attached image.
   * @property type A fixed string indicating the message type ("image").
   */
  data class ImageMessage(
      val imageUrl: String = "",
      val type: String = "image",
      override val senderName: String = "",
      override val senderId: String = "",
      override val timestamp: Long = System.currentTimeMillis(),
      override val status: MESSAGE_STATUS = MESSAGE_STATUS.SENT
  ) : ChatMessage(id = "", senderId, senderName, timestamp, status) {
    /** Provides a default instance of `ImageMessage` for initialization or testing. */
    companion object {
      @JvmStatic fun default() = ImageMessage()
    }
  }
}
