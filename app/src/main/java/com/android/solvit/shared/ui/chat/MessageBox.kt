package com.android.solvit.shared.ui.chat

import android.annotation.SuppressLint
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import com.android.solvit.shared.ui.utils.formatTimestamp
import com.android.solvit.shared.ui.utils.getReceiverImageUrl
import com.android.solvit.shared.ui.utils.getReceiverName

/**
 * Composable function displaying the message inbox screen.
 *
 * @param chatViewModel ViewModel managing chat interactions.
 * @param navigationActions Actions for navigation between screens.
 * @param authViewModel ViewModel managing authentication data.
 * @param listProviderViewModel ViewModel managing provider data.
 * @param seekerProfileViewModel ViewModel managing seeker profile data.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MessageBox(
    chatViewModel: ChatViewModel,
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel
) {
  val allMessages by chatViewModel.allMessages.collectAsStateWithLifecycle()
  val isReadyToNavigate by chatViewModel.isReadyToNavigate.collectAsStateWithLifecycle()
  val user by authViewModel.user.collectAsStateWithLifecycle()
  val isLoadingMessageBox by chatViewModel.isLoadingMessageBox.collectAsStateWithLifecycle()

  val currentUserUid = user?.uid
  if (currentUserUid != null) {
    chatViewModel.getAllLastMessages(currentUserUid)
  }

  LaunchedEffect(isReadyToNavigate) {
    if (isReadyToNavigate) {
      navigationActions.navigateTo(Screen.CHAT)
      chatViewModel.resetIsReadyToNavigate()
    }
  }

  if (isLoadingMessageBox) {
    // Display loading indicator
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
  } else {
    Scaffold(
        topBar = {
          TopAppBarInbox(title = "Inbox", showAppIcon = true, testTagGeneral = "InboxTopAppBar")
        },
        bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { navigationActions.navigateTo(it) },
              tabList =
                  if (authViewModel.user.value?.role == "seeker") {
                    LIST_TOP_LEVEL_DESTINATION_SEEKER
                  } else {
                    LIST_TOP_LEVEL_DESTINATION_PROVIDER
                  },
              selectedItem = Route.INBOX)
        }) { paddingValues ->
          if (allMessages.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                  items(allMessages.entries.toList()) { message ->
                    ChatListItem(
                        message = message.value,
                        currentUserId = user?.uid ?: "",
                        receiverId = message.key ?: "",
                        role = authViewModel.user.value?.role ?: "",
                        listProviderViewModel = listProviderViewModel,
                        seekerProfileViewModel = seekerProfileViewModel,
                        navigationActions = navigationActions,
                        chatViewModel = chatViewModel)
                  }
                }
          } else {
            NoMessagesSent(modifier = Modifier.padding(paddingValues))
          }
        }
  }
}

/**
 * Composable function displaying a chat list item with the latest message.
 *
 * @param message Latest chat message exchanged.
 * @param currentUserId Current user's unique identifier.
 * @param receiverId Receiver's unique identifier.
 * @param role Current user's role ("seeker" or "provider").
 * @param listProviderViewModel ViewModel managing provider data.
 * @param seekerProfileViewModel ViewModel managing seeker profile data.
 * @param navigationActions Actions for navigation between screens.
 * @param chatViewModel ViewModel managing chat interactions.
 */
@Composable
fun ChatListItem(
    message: ChatMessage,
    currentUserId: String,
    receiverId: String,
    role: String,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel
) {

  // Local state for receiver data
  val receiverState = remember { mutableStateOf<Any?>(null) }

  // Fetch receiver data without affecting shared ViewModel state
  LaunchedEffect(receiverId) {
    if (role == "seeker") {
      val provider = listProviderViewModel.fetchProviderById(receiverId)
      Log.e("provider", "$provider")
      receiverState.value = provider
    } else {
      val seeker = seekerProfileViewModel.fetchUserById(receiverId)
      Log.e("seeker", "$seeker")
      receiverState.value = seeker
    }
  }

  // Extract receiver data
  val receiver = receiverState.value
  val receiverName = receiver?.let { getReceiverName(it) }
  val receiverPicture = receiver?.let { getReceiverImageUrl(it) }
  Log.e("Message Box", "receiver : $receiver ")

  Row(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("ChatListItem").clickable {
            if (receiver != null) {
              chatViewModel.prepareForChat(false, currentUserId, receiverId, receiver)
            }
          },
      verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            modifier = Modifier.size(40.dp).clip(CircleShape),
            model = receiverPicture?.ifEmpty { R.drawable.default_pdp },
            placeholder = painterResource(id = R.drawable.loading),
            error = painterResource(id = R.drawable.error),
            contentDescription = "provider image",
            contentScale = ContentScale.Crop)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = receiverName ?: "",
              style = Typography.titleMedium,
              fontWeight = FontWeight.Bold)
          Text(
              text =
                  when (message) {
                    is ChatMessage.TextMessage -> message.message
                    is ChatMessage.ImageMessage -> "image"
                    is ChatMessage.TextImageMessage -> message.text
                  },
              style = Typography.bodyLarge,
              color = Color.Gray,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatTimestamp(message.timestamp),
            style = Typography.bodyLarge,
            color = Color.Gray)
      }
}

/**
 * Composable function displaying a placeholder screen when no messages are present.
 *
 * @param modifier Modifier applied to the entire screen.
 */
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

        Text(text = "No messages yet", style = Typography.bodyLarge, color = Color.Black)

        Text(text = "Send your first message", style = Typography.bodySmall, color = Color.Gray)
      }
}
