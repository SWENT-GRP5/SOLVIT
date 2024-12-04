package com.android.solvit.shared.model.chat

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiSolverViewModel : ViewModel() {

  private val _messageContext = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messageContext: StateFlow<List<ChatMessage>> = _messageContext

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
            return AiSolverViewModel() as T
          }
        }
  }

  data class UserInput(val description: String, val bitmap: Bitmap?)

  /** set the MessageContext of conversation */
  fun setMessageContext(messages: List<ChatMessage>) {
    _messageContext.value = messages
  }

  /** Update Message Context With New Messages */
  fun updateMessageContext(message: ChatMessage) {
    _messageContext.value += message
  }
  /**
   * Build prompt for the AI problem's solver
   *
   * @param input represent the User Input in conversation
   * @return prompt
   */
  fun buildPrompt(input: UserInput): String {
    val conversationHistory =
        messageContext.value.joinToString("\n") { message ->
          "${message.senderName} : ${
                when (message){
                    is ChatMessage.TextMessage -> message.message
                    is ChatMessage.ImageMessage -> message.imageUrl
                }
             }"
        }

    var prompt =
        """
        You are an AI assistant in an application that helps users with problem-solving and decision-making. Your primary roles are:

        1. Helping users solve their problems when possible by providing clear, actionable advice or steps.
        2. Advising users to seek professional assistance when the problem requires specialized skills or tools.
        3. Responding politely and generally when the input is unclear or not directly related to a specific problem, guiding the user back to the app's features if necessary.

        ### Context
        - The user and assistant have the following ongoing conversation:
        $conversationHistory

        - The user has now added the following input: 
        "${input.description}"

    """
            .trimIndent()

    if (input.bitmap != null) {
      prompt += "\n- An image describing the problem is provided"
    }

    prompt +=
        """
        
        ### Instructions
        1. Analyze the user's input in the context of the entire conversation.
        2. If the input describes a specific problem you can help with:
           - Provide clear, step-by-step guidance to solve the issue, or
           - If it requires professional help, advise the user to create a service request in the app and explain why.
        3. If the input is unclear or outside a problem description:
           - Respond politely and generally.
           - Suggest they provide more details or explore the app's features, such as creating a request to find a provider.
        4. Always be polite and ensure your responses are clear and suitable for a general audience.
    """
            .trimIndent()

    return prompt
  }

  /**
   * generate AI Solver Response
   *
   * @param input : User Input
   * @param onSuccess : Callback called with response successfully generated
   */
  fun generateMessage(input: UserInput, onSuccess: (String) -> Unit) {
    val prompt = buildPrompt(input)
    viewModelScope.launch {
      try {
        val response =
            if (input.bitmap != null) {
              val inputContent =
                  Content.Builder()
                      .apply {
                        image(input.bitmap)
                        text(prompt)
                      }
                      .build()
              model.generateContent(inputContent)
            } else {
              model.generateContent(prompt)
            }

        response.text?.let { onSuccess(it) }
      } catch (e: Exception) {
        Log.d("Ai solver", "Failed to generate a response : $e")
      }
    }
  }
}
