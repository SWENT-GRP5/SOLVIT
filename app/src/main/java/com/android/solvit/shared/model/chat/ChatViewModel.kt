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

  private val _allMessages = MutableStateFlow<List<ChatMessage.TextMessage>>(emptyList())
  val allMessages: StateFlow<List<ChatMessage.TextMessage>> = _allMessages

  private val _receiverName = MutableStateFlow<String>("")
  val receiverName: StateFlow<String> = _receiverName

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
    Log.e("Debug Test", "Init")
    getAllLastMessages()
  }

  fun setReceiverUid(uid: String) {
    receiverUid = uid
  }

  fun setReceiverName(name: String) {
    _receiverName.value = name
  }

  fun initChat() {
    receiverUid?.let {
      Log.e("receiverUid", "notnNull")
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
        onSuccess = { list -> _allMessages.value = list },
        onFailure = { Log.e("ChatViewModel", "Failed to get All messages") })
  }
}
