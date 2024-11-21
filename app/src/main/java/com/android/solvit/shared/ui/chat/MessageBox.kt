package com.android.solvit.shared.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.chat.ChatViewModel

@Composable
fun MessageBox(listChatViewModel: ChatViewModel, picture: String) {

  val chats = listOf("Hello", "How Are You")
  Scaffold(topBar = { ChatListTopBar() }, bottomBar = {}) { paddingValues ->

    /*LazyColumn(
        modifier = Modifier.padding(paddingValues).fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(chats){
            chat ->
            ChatListItem(chat, picture)

        }
    }*/

    NoMessagesSent(modifier = Modifier.padding(paddingValues))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar() {
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
        IconButton(onClick = { /* Handle back action */}) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
      },
      actions = {
        IconButton(onClick = { /* Handle new chat action */}) {
          Image(
              painter =
                  painterResource(id = R.drawable.new_message), // Replace with your image resource
              contentDescription = "Action",
              modifier = Modifier.size(24.dp) // Adjust the size as needed
              )
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
fun ChatListItem(chat: String, picture: String) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
              text = "Hassan",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold)
          Text(
              text = chat,
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
