package com.android.solvit.shared.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TestChatScreen(
    chatAssistantViewModel: ChatAssistantViewModel,
    navigationActions: NavigationActions
) {
  chatAssistantViewModel.clear()
  val message1 =
      ChatMessage.TextMessage(
          "Hello, I am contacting you because my wc is completely clogged", "Hassan", "1")
  val message2 =
      ChatMessage.TextMessage(
          "Ok! I can take care of it, however today and tomorrow I am fully booked, can you wait a couple of days?",
          "Manu",
          "2")
  val initialMessages = listOf(message1, message2)
  chatAssistantViewModel.updateMessageContext(message1)
  chatAssistantViewModel.updateMessageContext(message2)

  val messagesState = MutableStateFlow<List<ChatMessage.TextMessage>>(initialMessages)
  val messages by messagesState.collectAsState()
  val receiverName by MutableStateFlow("test").collectAsState()

  // picture is hardCoded since we didn't implement yet a logic to all informations of a user
  // starting from its id
  val picture =
      "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F98a09ae2-fddf-4ab8-96a5-3b10210230c7.jpg?alt=media&token=ce9376d6-de0f-42eb-ad97-5e4af0a74b16"
  Scaffold(
      topBar = {
        ChatHeader(name = receiverName, picture = picture, navigationActions = navigationActions)
      },
      bottomBar = {
        TestMessageInputBar(
            onMessageSend = { messagesState.value = messages + it }, chatAssistantViewModel)
      }) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).imePadding().testTag("conversation")) {
              items(messages) { message ->
                if (message.senderId == "1") {
                  // Item for messages authentified user send
                  SentMessage(message.message, true)
                } else {
                  // Item for messages authentified user receive
                  SentMessage(message.message, false, true, picture)
                }
              }
            }
      }
}

@Composable
fun TestMessageInputBar(
    onMessageSend: (ChatMessage.TextMessage) -> Unit,
    chatAssistantViewModel: ChatAssistantViewModel
) {

  var message by remember { mutableStateOf("") }

  val current = LocalContext.current
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  color = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(size = 28.dp),
              )
              .imePadding()
              .testTag(
                  "SendMessageBar"), // To ensure that content of scaffold appears even if keyboard
      // is
      // displayed
  ) {

    // Input to enter message you want to send
    TextField(
        value = message,
        onValueChange = { message = it },
        modifier = Modifier.weight(1f).padding(end = 8.dp),
        placeholder = {
          Text(text = "Send Message", color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = Color.Black,
                focusedTextColor = Color.Black),
        singleLine = true // Ensures the TextField stays compact
        )

    // Button to send your message
    IconButton(
        onClick = {
          val chatMessage =
              ChatMessage.TextMessage(
                  message,
                  "Hassan", // Has to be updated once we implement a logic to link the
                  // authenticated user to its profile (generic class for both provider
                  // and seeker that contains common informations,
                  "1",
                  timestamp = System.currentTimeMillis(),
              )
          onMessageSend(chatMessage)
          chatAssistantViewModel.updateMessageContext(chatMessage)
          message = ""
          // chatViewModel.getConversation()
        },
        modifier = Modifier.size(48.dp)) {
          Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "send",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.rotate(-45f))
        }

    // State to control the visibility of the Chat Assistant Dialog
    var showDialog by remember { mutableStateOf(false) }

    // Button to Use the Chat Assistant
    IconButton(onClick = { showDialog = true }, modifier = Modifier.size(48.dp)) {
      Icon(
          imageVector = Icons.Filled.Person,
          contentDescription = "send",
          tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    // Show the Chat Assistant Dialog if showDialog is true
    if (showDialog) {
      ChatAssistantDialog(
          chatAssistantViewModel, onDismiss = { showDialog = false }, onResponse = { message = it })
    }
  }
}
