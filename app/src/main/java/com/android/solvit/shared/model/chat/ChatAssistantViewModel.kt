package com.android.solvit.shared.model.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.BuildConfig
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatAssistantViewModel() : ViewModel() {

  private val _messageContext = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messageContext: StateFlow<List<ChatMessage>> = _messageContext

  private val _requestContext = MutableStateFlow<ServiceRequest?>(null)
  val requestContext: StateFlow<ServiceRequest?> = _requestContext

  private val _selectedTones = MutableStateFlow<List<String>>(emptyList())
  val selectedTones: StateFlow<List<String>> = _selectedTones

  private val _senderName = MutableStateFlow<String>("")
  val senderName: StateFlow<String> = _senderName

  private val _receiverName = MutableStateFlow<String>("")
  val receiverName: StateFlow<String> = _receiverName

  private val model =
      GenerativeModel(
          modelName = "gemini-1.5-flash",
          apiKey = BuildConfig.GOOGLE_AI_API_KEY,
          generationConfig =
              generationConfig {
                temperature = 0.15f
                topK = 32
                topP = 1f
                maxOutputTokens = 4096
              },
          safetySettings =
              listOf(
                  SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                  SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                  SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                  SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)))

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatAssistantViewModel() as T
          }
        }
  }

  /**
   * Set the context for the chat assistant to generate a response
   *
   * @param messageContext List of previous messages in the conversation
   * @param senderName Name of the sender
   * @param receiverName Name of the receiver
   * @param requestContext (Optional) The concerned service request
   */
  fun setContext(
      messageContext: List<ChatMessage>,
      senderName: String,
      receiverName: String,
      requestContext: ServiceRequest? = null
  ) {
    _messageContext.value = messageContext
    _senderName.value = senderName
    _receiverName.value = receiverName
    _requestContext.value = requestContext
  }

  /**
   * Add a new message to the context
   *
   * @param message The new message to add
   */
  fun updateMessageContext(message: ChatMessage) {
    _messageContext.value += message
  }

  /**
   * Update the selected tones of the response
   *
   * @param tones List of selected tones
   */
  fun updateSelectedTones(tones: List<String>) {
    _selectedTones.value = tones
  }

  /** Clear all the context values */
  fun clear() {
    _messageContext.value = emptyList()
    _senderName.value = ""
    _receiverName.value = ""
    _requestContext.value = null
    _selectedTones.value = emptyList()
  }

  /**
   * Build the prompt for the chat assistant
   *
   * @param input Additional infos to add to the prompt
   * @return The generated prompt
   */
  fun buildPrompt(input: String): String {
    var prompt = "Write a single message response"
    if (senderName.value.isNotEmpty()) {
      prompt += " from " + senderName.value
    }
    if (receiverName.value.isNotEmpty()) {
      prompt += " to " + receiverName.value
    }
    if (_messageContext.value.isNotEmpty()) {
      prompt += ", based on the following conversation:\n"
      prompt +=
          messageContext.value.joinToString("\n") {
            when (it) {
              is ChatMessage.TextMessage -> it.senderName + ": " + it.message
              is ChatMessage.ImageMessage -> it.senderName + ": [image]"
              is ChatMessage.TextImageMessage -> it.senderName + ": [image]" + it.text
            }
          }
    }
    if (_requestContext.value != null) {
      prompt += ", based on the following service request:\n"
      prompt += requestContext.value!!.title + ": " + requestContext.value!!.description
    }
    if (_selectedTones.value.isNotEmpty()) {
      prompt += ", with the following tones:\n"
      prompt += selectedTones.value.joinToString(", ")
    }
    if (input.isNotEmpty()) {
      prompt += ", with the following infos provided by the sender:\n"
      prompt += input
    }
    return prompt
  }

  /**
   * Generate a response message from the chat assistant
   *
   * @param input Additional infos to add to the prompt
   * @param onResponse Callback to handle the response
   */
  fun generateMessage(input: String, onResponse: (String) -> Unit) {
    val prompt = buildPrompt(input)
    Log.d("ChatAssistantViewModel", "model prompted")
    viewModelScope.launch {
      try {
        val response = model.generateContent(prompt)
        response.text?.let { onResponse(it) }
      } catch (e: Exception) {
        Log.e("ChatAssistantViewModel", "Error generating response", e)
        onResponse("Sorry, I couldn't process that.")
      }
    }
  }
}
