package com.android.solvit.shared.model.chat

import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Test

class ChatAssistantViewModelTest {

  private lateinit var chatAssistantViewModel: ChatAssistantViewModel
  private lateinit var messageContext: List<ChatMessage.TextMessage>
  private lateinit var senderName: String
  private lateinit var receiverName: String
  private lateinit var selectedTones: List<String>
  private lateinit var requestContext: ServiceRequest

  @Before
  fun setup() {
    chatAssistantViewModel = ChatAssistantViewModel()
    messageContext =
        listOf(ChatMessage.TextMessage("testMessage1"), ChatMessage.TextMessage("testMessage2"))
    senderName = "sender"
    receiverName = "receiver"
    selectedTones = listOf("tone1", "tone2")
    requestContext =
        ServiceRequest(
            uid = "1",
            title = "test",
            type = Services.PLUMBER,
            description = "description",
            userId = "0",
            dueDate = Timestamp.now(),
            location = null)
  }

  @Test
  fun setContextSetsContext() {
    chatAssistantViewModel.setContext(messageContext, senderName, receiverName, requestContext)
    assert(chatAssistantViewModel.messageContext.value == messageContext)
    assert(chatAssistantViewModel.senderName.value == senderName)
    assert(chatAssistantViewModel.receiverName.value == receiverName)
    assert(chatAssistantViewModel.requestContext.value == requestContext)
    chatAssistantViewModel.clear()
  }

  @Test
  fun updateMessageAddsMessageToContext() {
    val newMessage = ChatMessage.TextMessage("newMessage")
    chatAssistantViewModel.setContext(messageContext, senderName, receiverName, requestContext)
    chatAssistantViewModel.updateMessageContext(newMessage)
    assert(chatAssistantViewModel.messageContext.value == messageContext + newMessage)
    chatAssistantViewModel.clear()
  }

  @Test
  fun updateSelectedTonesSetsSelectedTones() {
    chatAssistantViewModel.updateSelectedTones(selectedTones)
    assert(chatAssistantViewModel.selectedTones.value == selectedTones)
    chatAssistantViewModel.clear()
  }

  @Test
  fun clearClearsAllValues() {
    chatAssistantViewModel.setContext(messageContext, senderName, receiverName, requestContext)
    chatAssistantViewModel.updateSelectedTones(selectedTones)
    chatAssistantViewModel.clear()
    assert(chatAssistantViewModel.messageContext.value.isEmpty())
    assert(chatAssistantViewModel.senderName.value.isEmpty())
    assert(chatAssistantViewModel.receiverName.value.isEmpty())
    assert(chatAssistantViewModel.requestContext.value == null)
    assert(chatAssistantViewModel.selectedTones.value.isEmpty())
  }

  @Test
  fun buildMessagePromptCreatesCorrectPrompt() {
    chatAssistantViewModel.setContext(messageContext, senderName, receiverName, requestContext)
    chatAssistantViewModel.updateSelectedTones(selectedTones)
    val input = "input"
    val prompt = chatAssistantViewModel.buildMessagePrompt(input)
    val expectedPrompt =
        "Write a single message response for sender to receiver, based on the following conversation:\n" +
            messageContext.joinToString("\n") { it.senderName + ": " + it.message } +
            ", based on the following service request:\n" +
            requestContext.title +
            ": " +
            requestContext.description +
            ", with the following tones:\n" +
            selectedTones.joinToString(", ") +
            ", with the following infos provided by the sender:\n" +
            input +
            ". If it would not make sense to respond in the conversation, please don't respond: The message you provide should be usable right away."
    assert(prompt == expectedPrompt)
    chatAssistantViewModel.clear()
  }

  @Test
  fun buildSuggestionsPromptCreatesCorrectPrompt() {
    chatAssistantViewModel.setContext(messageContext, senderName, receiverName, requestContext)
    val prompt = chatAssistantViewModel.buildSuggestionsPrompt()
    val expectedPrompt =
        "Provide a list without repetitions of suggestions themes for a response, based on the following conversation:\n" +
            messageContext.joinToString("\n") { it.senderName + ": " + it.message } +
            ", based on the following service request:\n" +
            requestContext.title +
            ": " +
            requestContext.description +
            ". Please make sure the suggestions are relevant to the conversation, otherwise don't provide any."
    assert(prompt == expectedPrompt)
    chatAssistantViewModel.clear()
  }
}
