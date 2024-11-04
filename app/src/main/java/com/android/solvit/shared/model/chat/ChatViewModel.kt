package com.android.solvit.shared.model.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

  private val currentUser = FirebaseAuth.getInstance().currentUser

  private var receiverUid: String? = null
  private var chatId: String? = null

  private val _coMessage = MutableStateFlow<List<ChatMessage>>(emptyList())
  val coMessage: StateFlow<List<ChatMessage>> = _coMessage

  private val _allMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val allMessages: StateFlow<List<ChatMessage>> = _allMessages

  private

  // Create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(ChatRepositoryFirestore(FirebaseDatabase.getInstance())) as T
          }
        }
  }

  fun setReceiverUid(uid: String) {
    receiverUid = uid
  }

  fun initChat() {
    receiverUid?.let { repository.initChat(onSuccess = { uid -> chatId = uid }, it) }
  }

  fun sendMessage(message: ChatMessage) {
    chatId?.let {
      repository.sendMessage(
          chatRoomId = it,
          message,
          onSuccess = {
            Log.e("send Message", "Message \"${message.message}\" is succesfully sent")
          },
          onFailure = {})
    }
  }

  fun getAllLastMessages() {}

  fun getConversation() {
    chatId?.let {
      repository.listenForMessages(
          it, onSuccess = { list -> _coMessage.value = list }, onFailure = {})
    }
  }
}
