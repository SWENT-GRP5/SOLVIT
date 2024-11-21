package com.android.solvit.shared.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults.shape
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.solvit.R

@Composable
fun ChatScreen(receiverName: String?, receiverImage: String, receiverId: String?) {

  val messages = listOf("1", "2", "1", "2")
  var message by remember { mutableStateOf("") }
  Scaffold(
      topBar = { ChatHeader(name = receiverName, picture = receiverImage) },
      bottomBar = { MessageInputBar(message = message, onMessageChange = { message = it }) }) {
          paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
          items(messages) { message ->
            if (message == "1") {
              // Item for messages authentified user send
              SentMessage(message, true)
            } else {
              // Item for messages authentified user receive
              SentMessage(message, false, true, receiverImage)
            }
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(name: String?, picture: String) {
  TopAppBar(
      title = {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          AsyncImage(
              modifier = Modifier.size(40.dp).clip(CircleShape),
              model = picture.ifEmpty { R.drawable.empty_profile_img },
              placeholder = painterResource(id = R.drawable.loading),
              error = painterResource(id = R.drawable.error),
              contentDescription = "provider image",
              contentScale = ContentScale.Crop)

          Spacer(Modifier.width(8.dp))

          Text(text = name ?: "Empty Name", style = MaterialTheme.typography.titleSmall)
        }
      },
      navigationIcon = {
        IconButton(onClick = { /*navigationActions.goBack()*/}) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
      })
}

@Composable
fun SentMessage(
    message: String,
    isSentByUser: Boolean,
    showProfilePicture: Boolean = false,
    receiverPicture: String = ""
) {

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start) {
        if (!isSentByUser && showProfilePicture) {
          AsyncImage(
              modifier = Modifier.size(40.dp).clip(CircleShape),
              model = receiverPicture.ifEmpty { R.drawable.empty_profile_img },
              placeholder = painterResource(id = R.drawable.loading),
              error = painterResource(id = R.drawable.error),
              contentDescription = "provider image",
              contentScale = ContentScale.Crop)

          Spacer(modifier = Modifier.width(8.dp))
        }

        BoxWithConstraints(
            modifier =
                Modifier.fillMaxWidth(fraction = 0.8f) // Limit bubble width to 80% of the screen
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isSentByUser) 16.dp else 0.dp,
                            topEnd = if (isSentByUser) 0.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp))
                    .background(
                        if (isSentByUser) MaterialTheme.colorScheme.primary else Color.LightGray)
                    .padding(horizontal = 16.dp, vertical = 12.dp)) {
              constraints

              Text(
                  text = message,
                  color = if (isSentByUser) MaterialTheme.colorScheme.background else Color.Black,
                  style = MaterialTheme.typography.bodySmall)
            }
      }
}

@Composable
fun MessageInputBar(message: String, onMessageChange: (String) -> Unit) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  color = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(size = 28.dp),
              ),
  ) {

    // Input to enter message you want to send
    TextField(
        value = message,
        onValueChange = { onMessageChange(message) },
        modifier = Modifier.weight(1f).padding(end = 8.dp),
        placeholder = {
          Text(text = "Send Message", color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.background,
                focusedTextColor = MaterialTheme.colorScheme.background),
        singleLine = true // Ensures the TextField stays compact
        )

    // Button to send your message
    IconButton(onClick = { /*chatvm.sendMessag()*/}, modifier = Modifier.size(48.dp)) {
      Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "send",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.rotate(-45f))
    }
  }
}
