package com.android.solvit.shared.model.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

  private var receiverUid: String? = null
  private var chatId: String? = null

  private val _coMessage = MutableStateFlow<List<ChatMessage.TextMessage>>(emptyList())
  val coMessage: StateFlow<List<ChatMessage.TextMessage>> = _coMessage

  private val _allMessages = MutableStateFlow<Map<String?, ChatMessage.TextMessage>>(emptyMap())
  val allMessages: StateFlow<Map<String?, ChatMessage.TextMessage>> = _allMessages

  private val _receiver = MutableStateFlow<Any>("")
  val receiver: StateFlow<Any> = _receiver

  private val _isReadyToNavigate = MutableStateFlow(false)
  val isReadyToNavigate: StateFlow<Boolean> = _isReadyToNavigate

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

  fun prepareForChat(currentUserUid: String?, receiverId: String, receiver: Any?) {
    viewModelScope.launch {
      _isReadyToNavigate.value = false
      setReceiverUid(receiverId)
      initChat(currentUserUid)
      receiver?.let { setReceiver(receiver) }
      _isReadyToNavigate.value = true
    }
  }

  fun setReceiverUid(uid: String) {
    receiverUid = uid
  }

  fun resetIsReadyToNavigate() {
    _isReadyToNavigate.value = false
  }

  fun setReceiver(receiver: Any) {
    _receiver.value = receiver
  }

  suspend fun initChat(currentUserUid: String?): String? {
    // Suspend Coroutine to make sure that get conversation is not called before init chat
    return suspendCoroutine { continuation ->
      receiverUid?.let {
        repository.initChat(
            currentUserUid,
            onSuccess = { uid ->
              Log.e("initChat", "onSuccess $uid")
              chatId = uid
              continuation.resume(uid)
            },
            onFailure = { continuation.resume(null) },
            it)
      }
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

  fun getAllLastMessages(currentUserUid: String?) {

    repository.listenForLastMessages(
        currentUserUid,
        onSuccess = { unsortedMap ->
          _allMessages.value =
              unsortedMap.toList().sortedByDescending { it.second.timestamp }.toMap()
        },
        onFailure = { Log.e("ChatViewModel", "Failed to get All messages") })
  }
}
