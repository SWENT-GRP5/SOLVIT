package com.android.solvit.shared.model.chat

import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class AiSolverViewModelTest {
  private lateinit var aiSolverViewModel: AiSolverViewModel

  var messagesContext =
      listOf(ChatMessage.TextMessage("Help Me"), ChatMessage.ImageMessage("imageUrl"))

  @Before
  fun SetUp() {
    aiSolverViewModel = AiSolverViewModel()
    aiSolverViewModel.setMessageContext(messagesContext)
  }

  @Test
  fun SetMessageContextCorrectly() {
    assertEquals(messagesContext, aiSolverViewModel.messageContext.value)
  }

  @Test
  fun UpdateMessageContextCorrectly() {
    val newMessage = ChatMessage.TextMessage("How Are U ?")
    aiSolverViewModel.updateMessageContext(newMessage)
    assertEquals(aiSolverViewModel.messageContext.value, messagesContext + newMessage)
  }

  @Test
  fun `buildPrompt generates the correct prompt without image`() {
    val userInput = AiSolverViewModel.UserInput("Describe the problem.", null)
    aiSolverViewModel.setMessageContext(messagesContext)

    val prompt = aiSolverViewModel.buildPrompt(userInput)

    assert(prompt.contains("Help Me"))
    assert(prompt.contains("imageUrl"))
    assert(prompt.contains("Describe the problem."))
    assert(!prompt.contains("If provided, here is an image describing the problem"))
  }

  @Test
  fun `buildPrompt generates the correct prompt with image`() {
    val userInput = AiSolverViewModel.UserInput("Describe the problem.", "testImagePath")
    aiSolverViewModel.setMessageContext(messagesContext)

    val prompt = aiSolverViewModel.buildPrompt(userInput)

    assert(prompt.contains("Help Me"))
    assert(prompt.contains("imageUrl"))
    assert(prompt.contains("Describe the problem."))
    assert(prompt.contains("testImagePath"))
  }
}
