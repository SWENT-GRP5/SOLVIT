package com.android.solvit.shared.model.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

  private var receiverUid: String? = null
  private var chatId: String? = null

  private val _coMessage = MutableStateFlow<List<ChatMessage.TextMessage>>(emptyList())
  val coMessage: StateFlow<List<ChatMessage.TextMessage>> = _coMessage

  private val _allMessages = MutableStateFlow<Map<String?, ChatMessage.TextMessage>>(emptyMap())
  val allMessages: StateFlow<Map<String?, ChatMessage.TextMessage>> = _allMessages

  private val _receiver = MutableStateFlow<Any>("")
  val receiver: StateFlow<Any> = _receiver

  // Create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(
                ChatRepositoryFirestore(FirebaseAuth.getInstance(), FirebaseDatabase.getInstance()))
                as T
          }
        }
  }

  init {
    getAllLastMessages()
  }

  fun setReceiverUid(uid: String) {
    receiverUid = uid
  }

  fun setChatId(uid: String) {
    chatId = uid
  }

  fun setReceiver(receiver: Any) {
    _receiver.value = receiver
  }

  fun initChat() {
    receiverUid?.let {
      repository.initChat(
          onSuccess = { uid ->
            Log.e("initChat", "onSuccess $uid")
            chatId = uid
          },
          it)
    }
  }

  fun sendMessage(message: ChatMessage.TextMessage) {
    Log.e("sendMessage", "$chatId")
    chatId?.let {
      Log.e("chatId", "notNull")
      repository.sendMessage(
          chatRoomId = it,
          message,
          onSuccess = {
            Log.e("send Message", "Message \"${message.message}\" is succesfully sent")
          },
          onFailure = { Log.e("send Message", "Failed") })
    }
  }

  fun getConversation() {
    chatId?.let {
      repository.listenForMessages(
          it, onSuccess = { list -> _coMessage.value = list }, onFailure = {})
    }
  }

  fun getAllLastMessages() {

    repository.listenForLastMessages(
        onSuccess = { unsortedMap ->
          _allMessages.value =
              unsortedMap.toList().sortedByDescending { it.second.timestamp }.toMap()
        },
        onFailure = { Log.e("ChatViewModel", "Failed to get All messages") })
  }
}
