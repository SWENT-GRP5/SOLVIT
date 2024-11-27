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

  private val _messageContext = MutableStateFlow<List<ChatMessage.TextMessage>>(emptyList())
  val messageContext: StateFlow<List<ChatMessage.TextMessage>> = _messageContext

  private val _requestContext = MutableStateFlow<ServiceRequest?>(null)
  val requestContext: StateFlow<ServiceRequest?> = _requestContext

  private val _selectedTones = MutableStateFlow<List<String>>(emptyList())
  val selectedTones: StateFlow<List<String>> = _selectedTones

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

  fun setContext(messageContext: List<ChatMessage.TextMessage>, requestContext: ServiceRequest) {
    _messageContext.value = messageContext
    _requestContext.value = requestContext
  }

  fun updateMessageContext(message: ChatMessage.TextMessage) {
    _messageContext.value += message
  }

  fun updateSelectedTones(tones: List<String>) {
    _selectedTones.value = tones
  }

  fun clear() {
    _messageContext.value = emptyList()
    _requestContext.value = null
    _selectedTones.value = emptyList()
  }

  fun generateMessage(input: String, onResponse: (String) -> Unit) {
    var prompt = "Write a single message response for the user"
    if (_messageContext.value.isNotEmpty()) {
      prompt += ", based on the following conversation:\n"
      prompt += messageContext.value.joinToString("\n") { it.senderName + ": " + it.message }
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
      prompt += ", with the following input:\n"
      prompt += input
    }
    Log.d("ChatAssistantViewModel", "Prompt: $prompt")
    viewModelScope.launch {
      val response = model.generateContent(prompt)
      response.text?.let { onResponse(it) }
    }
  }
}
