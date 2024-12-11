package com.android.solvit.shared.model.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

  private var receiverUid: String? = null
  private var chatId: String? = null

  private val _coMessage = MutableStateFlow<List<ChatMessage>>(emptyList())
  val coMessage: StateFlow<List<ChatMessage>> = _coMessage

  private val _allMessages = MutableStateFlow<Map<String?, ChatMessage>>(emptyMap())
  val allMessages: StateFlow<Map<String?, ChatMessage>> = _allMessages

  private val _receiver = MutableStateFlow<Any>("")
  val receiver: StateFlow<Any> = _receiver

  private val _isReadyToNavigate = MutableStateFlow(false)
  val isReadyToNavigate: StateFlow<Boolean> = _isReadyToNavigate

  private val _chatRequest = MutableStateFlow<ServiceRequest?>(null)
  val chatRequest: StateFlow<ServiceRequest?> = _chatRequest

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading

  private val _shouldCreateRequest = MutableStateFlow<Boolean>(false)
  val shouldCreateRequest: StateFlow<Boolean> = _shouldCreateRequest

  private val _isLoadingMessageBox = MutableStateFlow(true)
  val isLoadingMessageBox: StateFlow<Boolean> = _isLoadingMessageBox

  // Create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(
                ChatRepositoryFirestore(
                    FirebaseDatabase.getInstance(),
                    FirebaseStorage.getInstance(),
                    FirebaseFirestore.getInstance()))
                as T
          }
        }
  }

  fun prepareForChat(
      isIaConversation: Boolean,
      currentUserUid: String?,
      receiverId: String,
      receiver: Any?,
      requestUid: String = ""
  ) {
    viewModelScope.launch {
      _isReadyToNavigate.value = false
      setReceiverUid(receiverId)
      initChat(isIaConversation, currentUserUid, requestUid)
      receiver?.let { setReceiver(receiver) }
      getConversation(false)
      _isReadyToNavigate.value = true
    }
  }

  fun prepareForIaChat(
      isIaConversation: Boolean,
      currentUserUid: String?,
      receiverId: String,
      receiver: Any?
  ) {
    viewModelScope.launch {
      _isLoading.value = true
      setReceiverUid(receiverId)
      initChat(isIaConversation, currentUserUid)
      receiver?.let { setReceiver(receiver) }
      getConversation(isIaConversation)
      getShouldCreateRequest()
      _isLoading.value = false
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

  suspend fun initChat(
      isIaConversation: Boolean,
      currentUserUid: String?,
      requestUid: String = ""
  ): String? {
    // Suspend Coroutine to make sure that get conversation is not called before init chat
    return suspendCoroutine { continuation ->
      receiverUid?.let {
        repository.initChat(
            isIaConversation,
            currentUserUid,
            onSuccess = { uid ->
              Log.e("initChat", "onSuccess $uid")
              chatId = uid
              if (requestUid.isNotEmpty()) {
                repository.linkChatToRequest(
                    uid,
                    requestUid,
                    onSuccess = { Log.e("linkChatToRequest", "Success") },
                    onFailure = { Log.e("linkChatToRequest", "Failed") })
              }
              continuation.resume(uid)
            },
            onFailure = { continuation.resume(null) },
            it)
      }
    }
  }

  fun getChatRequest(onSuccess: (String) -> Unit) {
    chatId?.let {
      repository.getChatRequest(
          it, onSuccess = onSuccess, onFailure = { Log.e("getChatRequest", "Failed") })
    }
  }

  fun setChatRequest(serviceRequest: ServiceRequest) {
    _chatRequest.value = serviceRequest
  }

  /**
   * Set for a chat with AI solver if given the problem, the seeker should create a request
   *
   * @param shouldCreateRequest AI response determine if it's true or false
   */
  fun setShouldCreateRequest(shouldCreateRequest: Boolean) {
    chatId?.let {
      repository.seekerShouldCreateRequest(
          it,
          shouldCreateRequest,
          onSuccess = { _shouldCreateRequest.value = shouldCreateRequest },
          onFailure = { Log.e("Chat View Model", "Failed to set should create request flag") })
    }
  }

  /** Get the should create flag given an conversation with AI solver chatbot */
  fun getShouldCreateRequest() {
    chatId?.let {
      repository.getShouldCreateRequest(
          it,
          onSuccess = { flag -> _shouldCreateRequest.value = flag },
          onFailure = { Log.e("Chat View Model", "Failed to get should create Request flag") })
    }
  }

  fun sendMessage(isIaConversation: Boolean, message: ChatMessage) {
    Log.e("sendMessage", "$chatId")
    chatId?.let {
      Log.e("chatId", "notNull")
      repository.sendMessage(
          isIaConversation,
          chatRoomId = it,
          message,
          onSuccess = {
            Log.e("send Message", "Message \"${message}\" is succesfully sent")
            getShouldCreateRequest()
          },
          onFailure = { Log.e("send Message", "Failed") })
    }
  }

  fun getConversation(isIaConversation: Boolean) {

    chatId?.let {
      repository.listenForMessages(
          isIaConversation, it, onSuccess = { list -> _coMessage.value = list }, onFailure = {})
    }
  }

  fun getAllLastMessages(currentUserUid: String?) {

    viewModelScope.launch {
      _isLoadingMessageBox.value = true
      repository.listenForLastMessages(
          currentUserUid,
          onSuccess = { unsortedMap ->
            _allMessages.value =
                unsortedMap.toList().sortedByDescending { it.second.timestamp }.toMap()
            Log.e("ChatVM", "${allMessages.value}")
            _isLoadingMessageBox.value = false
            Log.e("Chat VM is loading Success", "${_isLoadingMessageBox.value}")
          },
          onFailure = {
            _isLoadingMessageBox.value = false
            Log.e("ChatViewModel", "Failed to get All messages")
          })
      _isLoadingMessageBox.value = false

      Log.e("Chat VM is loading", "${_isLoadingMessageBox.value}")
    }
  }

  fun clearConversation(isIaConversation: Boolean) {
    Log.e("clear", "$chatId")
    chatId?.let {
      repository.clearConversation(
          isIaConversation,
          it,
          onSuccess = {
            Log.e("Chat View model", "Conversation of $chatId is successfully deleted")
          },
          onFailure = { Log.e("Chat VM", "Failed to delete conversation of $chatId") })
    }
  }

  suspend fun uploadImagesToStorage(imageUri: Uri): String? {
    return suspendCoroutine { continuation ->
      repository.uploadChatImagesToStorage(
          imageUri = imageUri,
          onSuccess = { imageUrl ->
            Log.e("AiSolverScreen", "Image Uri succesfully uploaded")
            continuation.resume(imageUrl)
          },
          onFailure = { e ->
            Log.e("ChatViewModel", "Failed to upload image to storage $e")
            continuation.resume(null)
          })
    }
  }
}
