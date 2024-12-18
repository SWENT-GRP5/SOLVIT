import android.net.Uri
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
    val testReceiver = "Test Receiver"
    chatViewModel.setReceiver(testReceiver)

    val receiver = chatViewModel.receiver.value
    assert(receiver == testReceiver)
  }

  @Test
  fun `deleteConversation call repository`() = runTest {
    chatViewModel.setReceiverUid("receiver123")

    whenever(chatRepository.initChat(eq(false), any(), any(), any(), any())).doAnswer {
      val onSuccess = it.arguments[2] as (String) -> Unit
      onSuccess("testChatId")
      null
    }
    chatViewModel.initChat(false, "currentUserUid")
    chatViewModel.clearConversation(false)
    verify(chatRepository).clearConversation(any(), any(), any(), any())
  }

  @Test
  fun `upload image to storage call repository`() = runTest {
    val uri = mock(Uri::class.java)

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(String?) -> Unit>(1)
          onSuccess("https://example.com/image.jpg")
          null
        }
        .whenever(chatRepository)
        .uploadChatImagesToStorage(any(), any(), any())

    val chatViewModel = ChatViewModel(chatRepository)

    val result = chatViewModel.uploadImagesToStorage(uri)

    verify(chatRepository).uploadChatImagesToStorage(eq(uri), any(), any())

    assertEquals("https://example.com/image.jpg", result)
  }

  @Test
  fun `setShouldCreateRequest call repository`() = runTest {
    chatViewModel.setReceiverUid("receiver123")

    whenever(chatRepository.initChat(eq(false), any(), any(), any(), any())).doAnswer {
      val onSuccess = it.arguments[2] as (String) -> Unit
      onSuccess("testChatId")
      null
    }
    chatViewModel.initChat(false, "currentUserUid")
    chatViewModel.setShouldCreateRequest(true)
    verify(chatRepository).seekerShouldCreateRequest(any(), eq(true), any(), any())
  }

  @Test
  fun `getShouldCreateRequest call repository`() = runTest {
    chatViewModel.setReceiverUid("receiver123")

    whenever(chatRepository.initChat(eq(false), any(), any(), any(), any())).doAnswer {
      val onSuccess = it.arguments[2] as (String) -> Unit
      onSuccess("testChatId")
      null
    }
    chatViewModel.initChat(false, "currentUserUid")
    chatViewModel.getShouldCreateRequest()
    verify(chatRepository).getShouldCreateRequest(any(), any(), any())
  }
}
