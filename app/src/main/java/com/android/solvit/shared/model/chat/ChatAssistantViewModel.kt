package com.android.solvit.shared.model.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.BuildConfig
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

class ChatAssistantViewModel : ViewModel() {

  private val _messageContext = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messageContext: StateFlow<List<ChatMessage>> = _messageContext

  private val _requestContext = MutableStateFlow<ServiceRequest?>(null)
  val requestContext: StateFlow<ServiceRequest?> = _requestContext

  private val _selectedTones = MutableStateFlow<List<String>>(emptyList())
  val selectedTones: StateFlow<List<String>> = _selectedTones

  private val _senderName = MutableStateFlow("")
  val senderName: StateFlow<String> = _senderName

  private val _receiverName = MutableStateFlow("")
  val receiverName: StateFlow<String> = _receiverName

  private val _suggestions = MutableStateFlow<List<String>>(emptyList())
  val suggestions: StateFlow<List<String>> = _suggestions

  private val messagesModel =
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

  private val suggestionsModel =
      GenerativeModel(
          modelName = "gemini-1.5-flash",
          apiKey = BuildConfig.GOOGLE_AI_API_KEY,
          generationConfig =
              generationConfig {
                temperature = 0.15f
                topK = 32
                topP = 1f
                maxOutputTokens = 4096
                responseMimeType = "application/json"
                responseSchema =
                    Schema(
                        name = "suggestions",
                        description = "Short list of suggestions for a response",
                        type = FunctionType.ARRAY,
                        items =
                            Schema(
                                name = "suggestion",
                                description = "suggested response type",
                                type = FunctionType.STRING,
                                enum =
                                    listOf(
                                        "Confirm",
                                        "Refute",
                                        "Apologize",
                                        "Thank",
                                        "Explain",
                                        "Ask clarifications",
                                        "Reassure",
                                        "Joke",
                                        "Negotiate the price")))
              })

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

  private fun messageContextToPrompt(): String {
    return messageContext.value.joinToString("\n") {
      when (it) {
        is ChatMessage.TextMessage -> it.senderName + ": " + it.message
        is ChatMessage.ImageMessage -> it.senderName + ": [image]"
        is ChatMessage.TextImageMessage -> it.senderName + ": [image]" + it.text
      }
    }
  }

  /**
   * Build the prompt for the chat assistant
   *
   * @param input Additional infos to add to the prompt
   * @param isSeeker Flag to indicate if the user is a seeker
   * @return The generated prompt
   */
  fun buildMessagePrompt(input: String, isSeeker: Boolean): String {
    var prompt = "Write a single chat message response"
    if (senderName.value.isNotEmpty()) {
      prompt += " for " + senderName.value
    }
    if (receiverName.value.isNotEmpty()) {
      prompt += " to " + receiverName.value
    }
    if (_messageContext.value.isNotEmpty()) {
      prompt += ", based on the following conversation:\n"
      prompt += messageContextToPrompt()
    }
    if (_requestContext.value != null) {
      prompt +=
          ", based on the following service request posted by ${if (isSeeker) senderName.value else receiverName.value}:\n"
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
    prompt +=
        ". If it would not make sense to respond in the conversation, please don't respond: The message you provide should be usable right away."
    return prompt
  }

  /**
   * Generate a response message from the chat assistant
   *
   * @param input Additional infos to add to the prompt
   * @param isSeeker Flag to indicate if the user is a seeker
   * @param onResponse Callback to handle the response
   */
  fun generateMessage(input: String, isSeeker: Boolean, onResponse: (String) -> Unit) {
    val prompt = buildMessagePrompt(input, isSeeker)
    Log.d("ChatAssistantViewModel", "model prompted")
    viewModelScope.launch {
      try {
        val response = messagesModel.generateContent(prompt)
        response.text?.let { onResponse(it) }
      } catch (e: Exception) {
        Log.e("ChatAssistantViewModel", "Error generating response", e)
        onResponse("Sorry, I couldn't process that.")
      }
    }
  }

  /**
   * Build the prompt for the suggestions assistant
   *
   * @param isSeeker Flag to indicate if the user is a seeker
   * @return The generated prompt
   */
  fun buildSuggestionsPrompt(isSeeker: Boolean): String {
    var prompt = "Provide a list without repetitions of suggestions themes for a response"
    if (_messageContext.value.isNotEmpty()) {
      prompt += ", based on the following conversation:\n"
      prompt += messageContextToPrompt()
    }
    if (_requestContext.value != null) {
      prompt +=
          ", based on the following service request posted by ${if (isSeeker) senderName.value else receiverName.value}:\n"
      prompt += requestContext.value!!.title + ": " + requestContext.value!!.description
    }
    prompt +=
        ". Please make sure the suggestions are relevant to the conversation, otherwise don't provide any."
    return prompt
  }

  /**
   * Generate suggestions for a response message from the chat assistant
   *
   * @param isSeeker Flag to indicate if the user is a seeker
   * @param onResponse Callback to handle the response
   */
  fun generateSuggestions(isSeeker: Boolean, onResponse: () -> Unit) {
    val prompt = buildSuggestionsPrompt(isSeeker)
    Log.d("ChatAssistantViewModel", "model prompted")
    viewModelScope.launch {
      try {
        val response = suggestionsModel.generateContent(prompt)
        response.text?.let {
          val suggestionsResponse = Json.decodeFromString<List<String>>(it)
          _suggestions.value = suggestionsResponse
        }
      } catch (e: Exception) {
        Log.e("ChatAssistantViewModel", "Error generating suggestions", e)
        _suggestions.value = emptyList()
      }
    }
  }

  /**
   * Generate a message translation from the chat assistant
   *
   * @param message The message to translate
   * @param languageFrom The language to translate from
   * @param languageTo The language to translate to
   * @param onResponse Callback to handle the response
   */
  fun generateTranslation(
      message: String,
      languageFrom: String,
      languageTo: String,
      onResponse: (String) -> Unit
  ) {
    val prompt =
        "Translate the following message: $message, from $languageFrom to $languageTo, give a single translation that could be sent without modifications."
    Log.d("ChatAssistantViewModel", "model prompted")
    viewModelScope.launch {
      try {
        val response = messagesModel.generateContent(prompt)
        response.text?.let { onResponse(it) }
      } catch (e: Exception) {
        Log.e("ChatAssistantViewModel", "Error generating translation", e)
        onResponse("Sorry, I couldn't process that.")
      }
    }
  }
}
