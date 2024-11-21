package com.android.solvit.shared.ui.chat

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

@Composable
fun MessageBox(
    chatViewModel: ChatViewModel,
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel
) {

  val allMessages by chatViewModel.allMessages.collectAsState()

  chatViewModel.getAllLastMessages()

  // picture is hardCoded since we didn't implement yet a logic to all informations of a user
  // starting from its id
  val picture =
      "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F98a09ae2-fddf-4ab8-96a5-3b10210230c7.jpg?alt=media&token=ce9376d6-de0f-42eb-ad97-5e4af0a74b16"

  Scaffold(
      topBar = { ChatListTopBar(navigationActions, chatViewModel, authViewModel) },
      bottomBar = {}) { paddingValues ->
        if (allMessages.isNotEmpty()) {
          LazyColumn(
              modifier = Modifier.padding(paddingValues).fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(allMessages) { chat ->
                  ChatListItem(chat, picture, navigationActions, chatViewModel)
                }
              }
        } else {
          NoMessagesSent(modifier = Modifier.padding(paddingValues))
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel
) {
  val context = LocalContext.current
  chatViewModel.setReceiverUid("12345")
  chatViewModel.initChat()
  TopAppBar(
      title = {
        Box(
            modifier = Modifier.fillMaxWidth(),
            // Allow the title to center
            contentAlignment = Alignment.Center) {
              Text(text = "Inbox", style = MaterialTheme.typography.headlineLarge)
            }
      },
      navigationIcon = {
        IconButton(onClick = { navigationActions.goBack() }) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
      },
      actions = {
        IconButton(
            onClick = {
              // Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()

              val senderUid = authViewModel.user.value?.uid
              Log.e("SendMessage", "$senderUid")
              val message =
                  senderUid?.let {
                    ChatMessage.TextMessage(
                        "Hey",
                        "Hassan",
                        it,
                        System.currentTimeMillis(),
                    )
                  }
              if (message != null) {
                Log.e("SendMessage", "Soy Aqui")
                chatViewModel.sendMessage(message)
              }
            }) {
              Image(
                  painter = painterResource(id = R.drawable.new_message),
                  contentDescription = "Action",
                  modifier = Modifier.size(24.dp))
            }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = Color.White,
              titleContentColor = Color.Black,
              navigationIconContentColor = Color.Black,
              actionIconContentColor = Color.Black),
  )
}

@Composable
fun ChatListItem(
    message: ChatMessage.TextMessage,
    picture: String,
    navigationActions: NavigationActions,
    listChatViewModel: ChatViewModel
) {

  Row(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
            // Actions to retrieve the conversation with correct user
            listChatViewModel.setReceiverUid(message.senderId)
            listChatViewModel.setReceiverName(message.senderName)
            navigationActions.navigateTo(Screen.CHAT)
          },
      verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            modifier = Modifier.size(40.dp).clip(CircleShape),
            model = picture.ifEmpty { R.drawable.empty_profile_img },
            placeholder = painterResource(id = R.drawable.loading),
            error = painterResource(id = R.drawable.error),
            contentDescription = "provider image",
            contentScale = ContentScale.Crop)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = message.senderName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)
          Text(
              text = message.message,
              style = MaterialTheme.typography.bodyLarge,
              color = Color.Gray,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Friday", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
      }
}

@Composable
fun NoMessagesSent(modifier: Modifier) {
  // Screen for user that has never sent messages
  Column(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter =
                painterResource(
                    id = R.drawable.no_messages_image), // Replace with your image resource ID
            contentDescription = "No messages illustration",
            modifier = Modifier.size(200.dp) // Adjust size as needed
            )

        Text(
            text = "No messages yet",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black)

        Text(
            text = "Send your first message",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray)
      }
}
