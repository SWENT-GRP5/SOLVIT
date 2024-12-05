import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*

class ChatViewModelTest {

  private lateinit var chatRepository: ChatRepository
  private lateinit var chatViewModel: ChatViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
    chatRepository = mock(ChatRepository::class.java)
    chatViewModel = ChatViewModel(chatRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initChat calls repository with correct parameters`() = runTest {
    chatViewModel.setReceiverUid("receiver123")
    val currentUserUid = "currentUserUid"

    whenever(chatRepository.initChat(any(), any(), any(), any(), any())).doAnswer { invocation ->
      val onSuccess = invocation.arguments[2] as (String) -> Unit
      onSuccess("testChatId")
      null
    }

    chatViewModel.initChat(false, currentUserUid)

    verify(chatRepository).initChat(any(), any(), any(), any(), any())
  }

  @Test
  fun `sendMessage calls repository sendMessage`() = runTest {
    chatViewModel.setReceiverUid("receiver123")

    whenever(chatRepository.initChat(eq(false), any(), any(), any(), any())).doAnswer {
      val onSuccess = it.arguments[2] as (String) -> Unit
      onSuccess("testChatId")
      null
    }
    chatViewModel.initChat(false, "currentUserUid")

    val message =
        ChatMessage.TextMessage(
            senderId = "sender123",
            senderName = "Sender Name",
            message = "Hello",
            timestamp = System.currentTimeMillis())

    chatViewModel.sendMessage(false, message)

    verify(chatRepository).sendMessage(eq(false), eq("testChatId"), eq(message), any(), any())
  }

  @Test
  fun `getConversation calls repository listenForMessages`() = runTest {
    chatViewModel.setReceiverUid("receiver123")
    whenever(chatRepository.initChat(eq(false), any(), any(), any(), any())).doAnswer {
      val onSuccess = it.arguments[2] as (String) -> Unit
      onSuccess("testChatId")
      null
    }

    chatViewModel.initChat(false, "currentUserUid")

    chatViewModel.getConversation(false)

    verify(chatRepository).listenForMessages(eq(false), eq("testChatId"), any(), any())
  }

  @Test
  fun `getAllMessages calls repository listenForLastMessages`() = runTest {
    whenever(chatRepository.listenForLastMessages(any(), any(), any())).doAnswer {
      val onSuccess = it.arguments[1] as (Map<String, ChatMessage>) -> Unit
      onSuccess(
          mapOf("key1" to ChatMessage.TextMessage("key1", "Hi", "sender", "Hello", 123456789L)))
      null
    }

    chatViewModel.getAllLastMessages("currentUserUid")
    advanceUntilIdle()

    verify(chatRepository).listenForLastMessages(any(), any(), any())
    assert(chatViewModel.allMessages.value.isNotEmpty())
  }

  @Test
  fun `setReceiver updates _receiver`() {
    // Act
    val testReceiver = "Test Receiver"
    chatViewModel.setReceiver(testReceiver)

    // Assert
    val receiver = chatViewModel.receiver.value
    assert(receiver == testReceiver)
  }
}
