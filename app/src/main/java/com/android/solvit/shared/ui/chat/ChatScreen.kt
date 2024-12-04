package com.android.solvit.shared.ui.chat

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.AiSolverViewModel
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.Black
import com.android.solvit.shared.ui.theme.LightOrange
import com.android.solvit.shared.ui.utils.getReceiverImageUrl
import com.android.solvit.shared.ui.utils.getReceiverName
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    chatAssistantViewModel: ChatAssistantViewModel,
) {
  chatAssistantViewModel.clear()

  val messages by chatViewModel.coMessage.collectAsState()
  val receiver by chatViewModel.receiver.collectAsState()
  val user by authViewModel.user.collectAsState()
  val receiverName = getReceiverName(receiver)
  val receiverPicture = getReceiverImageUrl(receiver)

  // chatAssistantViewModel.setContext(messages, "Hassan", receiverName)

  // To send Image Messages
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  val localContext = LocalContext.current
  var sendMessageWithImage by remember { mutableStateOf(false) }

  // To Upload Image To Firebase
  if (sendMessageWithImage)
      Scaffold(
          topBar = {
            ChatHeader(
                name = receiverName,
                picture = receiverPicture,
                navigationActions = navigationActions)
          },
          bottomBar = {
            MessageInputBar(
                chatAssistantViewModel = chatAssistantViewModel,
                isAiSolverScreen = false,
                onImageSelected = { uri: Uri? ->
                  imageUri = uri
                  uri?.let { imageBitmap = loadBitmapFromUri(localContext, it) }
                },
                onSendClickButton = { message, onMessageChange ->
                  val chatMessage =
                      authViewModel.user.value?.uid?.let {
                        ChatMessage.TextMessage(
                            message,
                            "Hassan", // Has to be updated once we implement a logic to link the
                            // authenticated user to its profile (generic class for both provider
                            // and seeker that contains common informations,
                            it,
                            timestamp = System.currentTimeMillis(),
                        )
                      }

                  if (chatMessage != null && message.isNotEmpty()) {
                    chatViewModel.sendMessage(false, chatMessage)
                    chatAssistantViewModel.updateMessageContext(chatMessage)
                    onMessageChange("")
                  } else {
                    Toast.makeText(localContext, "Failed to send message", Toast.LENGTH_LONG).show()
                  }
                })
          }) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues).imePadding().testTag("conversation")) {
                  items(messages) { message ->
                    if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                      // Item for messages authentified user send
                      SentMessage(message, true)
                    } else {
                      // Item for messages authentified user receive
                      SentMessage(message, false, true, receiverPicture)
                    }
                  }
                }
          }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSolverWelcomeScreen(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  val user by authViewModel.user.collectAsState()
  val iaBotUserId = "JL36T8yHjWDYkuq4u6S4" // The default Id of the IA Bot created
  // Fetch conversation
  val isLoading by chatViewModel.isLoading.collectAsState()
  val conversation by chatViewModel.coMessage.collectAsState()

  LaunchedEffect(Unit) { chatViewModel.prepareForIaChat(true, user?.uid, iaBotUserId, "IaBot") }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background))
      },
  ) { innerPadding ->
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .testTag("AiGetStartedScreen"),
        contentAlignment = Alignment.TopCenter) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
          } else {
            val screenHeight = maxHeight

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier.fillMaxSize()
                        .padding(
                            top = screenHeight.times(0.05f), bottom = screenHeight.times(0.1f))) {
                  Text(
                      text =
                          buildAnnotatedString {
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = Black,
                                        fontSize = screenHeight.times(0.03f).value.sp)) {
                                  append("Meet Your Personal ")
                                }
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = LightOrange,
                                        fontSize = screenHeight.times(0.03f).value.sp)) {
                                  append("AI\n")
                                }
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = LightOrange,
                                        fontSize = screenHeight.times(0.03f).value.sp)) {
                                  append("Problem Solver")
                                }
                          },
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(horizontal = 16.dp).testTag("title"))

                  Image(
                      modifier = Modifier.testTag("image"),
                      painter = painterResource(id = R.drawable.ai_image),
                      contentDescription = "ai logo",
                      contentScale = ContentScale.FillBounds)

                  Text(
                      text = "I'm pleased that I meet you! How can\nI help you right now?",
                      fontSize = screenHeight.times(0.02f).value.sp,
                      color = Color.Gray,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(horizontal = 16.dp))

                  if (conversation.isEmpty()) {
                    ButtonStartConversationWithAI(
                        navigationActions = navigationActions,
                        screenHeight = screenHeight,
                        title = "Let solve a new problem",
                        clearConversation = false,
                        chatViewModel)
                  } else {
                    ButtonStartConversationWithAI(
                        navigationActions = navigationActions,
                        screenHeight = screenHeight,
                        title = "Continue",
                        clearConversation = false,
                        chatViewModel)
                    ButtonStartConversationWithAI(
                        navigationActions = navigationActions,
                        screenHeight = screenHeight,
                        title = "Let solve a new problem",
                        clearConversation = true,
                        chatViewModel)
                  }
                }
          }
        }
  }
}
/** Represent either a solve new Problem Button or continue solving current Problem* */
@Composable
fun ButtonStartConversationWithAI(
    navigationActions: NavigationActions,
    screenHeight: Dp,
    title: String,
    clearConversation: Boolean,
    chatViewModel: ChatViewModel
) {
  Button(
      onClick = {
        if (clearConversation) chatViewModel.clearConversation(true)
        navigationActions.navigateTo(Screen.AI_SOLVER_CHAT_SCREEN)
      },
      colors = ButtonDefaults.buttonColors(containerColor = LightOrange),
      shape = RoundedCornerShape(50),
      modifier =
          Modifier.fillMaxWidth(0.6f)
              .height(screenHeight.times(0.07f))
              .testTag("getStartedButton")) {
        Text(text = title, fontSize = screenHeight.times(0.025f).value.sp, color = Color.White)
      }
}

/** Chat with Ai problem solver chatbot */
@Composable
fun AiSolverScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    aiSolverViewModel: AiSolverViewModel
) {

  // To send Image Messages
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var reply by remember { mutableStateOf("") }
  val localContext = LocalContext.current
  val conversation by chatViewModel.coMessage.collectAsState()
  val user by authViewModel.user.collectAsState()
  aiSolverViewModel.setMessageContext(conversation)

  Scaffold(
      modifier = Modifier.testTag("AiSolverScreen"),
      topBar = { AiSolverHeader(navigationActions) },
      bottomBar = {
        MessageInputBar(
            isAiSolverScreen = true,
            onImageSelected = { uri: Uri? ->
              imageUri = uri
              uri?.let { imageBitmap = loadBitmapFromUri(localContext, it) }
            },
            onSendClickButton = { textMessage, onMessageChange ->
              chatViewModel.viewModelScope.launch {
                // upload Image User want to send to storage
                val imageUrl =
                    if (imageUri != null) chatViewModel.uploadImagesToStorage(imageUri!!) else null
                val message =
                    user?.let {
                      buildMessage(
                          it.uid,
                          textMessage,
                          imageUrl,
                      )
                    }
                if (message != null) {
                  chatViewModel.sendMessage(true, message)
                  onMessageChange("")
                  val userInput = AiSolverViewModel.UserInput(textMessage, imageBitmap)
                  aiSolverViewModel.updateMessageContext(message)
                  aiSolverViewModel.generateMessage(
                      userInput,
                      onSuccess = {
                        reply = it
                        val aiReply =
                            ChatMessage.TextMessage(
                                message = reply,
                                senderName = "AiBot",
                                senderId = "JL36T8yHjWDYkuq4u6S4",
                                timestamp = System.currentTimeMillis())

                        chatViewModel.sendMessage(true, aiReply)
                      })
                  imageUri = null
                  imageBitmap = null
                }
              }
            })
      }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).imePadding().testTag("chat")) {
          items(conversation) { message ->
            if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
              SentMessage(message, true)
            } else {
              SentMessage(message, false, true, "")
            }
          }
        }
      }
}

/** Header of Chat with Ai problem solver */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AiSolverHeader(navigationActions: NavigationActions) {
  TopAppBar(
      modifier = Modifier.testTag("AiChatHeader"),
      title = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text(text = "Ai Solver", style = MaterialTheme.typography.headlineLarge)
        }
      },
      navigationIcon = {
        IconButton(onClick = { navigationActions.navigateTo(Route.SERVICES) }) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(name: String?, picture: String, navigationActions: NavigationActions) {
  TopAppBar(
      modifier = Modifier.testTag("ChatHeader"),
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
        IconButton(onClick = { navigationActions.goBack() }) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
      })
}

@Composable
fun SentMessage(
    message: ChatMessage,
    isSentByUser: Boolean,
    showProfilePicture: Boolean = false,
    receiverPicture: String = ""
) {

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("MessageItem"),
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

              // Message Item take different forms given the format
              when (message) {
                is ChatMessage.TextMessage ->
                    Text(
                        text = message.message,
                        color =
                            if (isSentByUser) MaterialTheme.colorScheme.background else Color.Black,
                        style = MaterialTheme.typography.bodySmall)
                is ChatMessage.ImageMessage ->
                    AsyncImage(
                        modifier =
                            Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                        model = message.imageUrl.ifEmpty { R.drawable.error },
                        placeholder = painterResource(id = R.drawable.loading),
                        error = painterResource(id = R.drawable.error),
                        contentDescription = "Image message",
                        contentScale = ContentScale.Crop)
                is ChatMessage.TextImageMessage -> {
                  Column(
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = message.text,
                            color =
                                if (isSentByUser) MaterialTheme.colorScheme.onPrimary
                                else Color.Black,
                            style = MaterialTheme.typography.bodySmall)

                        AsyncImage(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .aspectRatio(1f) // Maintain 1:1 aspect ratio
                                    .clip(RoundedCornerShape(8.dp)),
                            model = message.imageUrl.ifEmpty { R.drawable.error },
                            placeholder = painterResource(id = R.drawable.loading),
                            error = painterResource(id = R.drawable.error),
                            contentDescription = "Image message",
                            contentScale = ContentScale.Crop)
                      }
                }
              }
            }
      }
}

@Composable
fun MessageInputBar(
    chatAssistantViewModel: ChatAssistantViewModel =
        viewModel(factory = ChatAssistantViewModel.Factory),
    onImageSelected: (Uri?) -> Unit,
    isAiSolverScreen: Boolean,
    onSendClickButton: (String, (String) -> Unit) -> Unit
) {

  var message by remember { mutableStateOf("") }
  val current = LocalContext.current

  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? -> onImageSelected(uri) })
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

    // State to control the visibility of the Chat Assistant Dialog
    var showDialog by remember { mutableStateOf(false) }

    if (!isAiSolverScreen) {
      // Button to Use the Chat Assistant
      IconButton(onClick = { showDialog = true }, modifier = Modifier.size(48.dp)) {
        Icon(
            painter = painterResource(R.drawable.ai_message),
            contentDescription = "chat assistant",
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      // Show the Chat Assistant Dialog if showDialog is true
      if (showDialog) {
        ChatAssistantDialog(
            chatAssistantViewModel,
            onDismiss = { showDialog = false },
            onResponse = { message = it })
      }
    }

    IconButton(
        onClick = { imagePickerLauncher.launch("image/*") },
        modifier = Modifier.testTag("uploadImageButton")) {
          Icon(
              imageVector = Icons.Default.AddCircle,
              contentDescription = "upload image",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
    // Button to send your message
    IconButton(
        onClick = { onSendClickButton(message) { message = it } },
        modifier = Modifier.size(48.dp)) {
          Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "send",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.rotate(-45f))
        }
  }
}

/** Handle different messages possible format a user send */
fun buildMessage(userId: String, messageText: String?, imageUrl: String?): ChatMessage? {
  return when {
    // User send both a text and an Image
    !messageText.isNullOrEmpty() && !imageUrl.isNullOrEmpty() -> {
      ChatMessage.TextImageMessage(
          text = messageText,
          imageUrl = imageUrl,
          senderId = userId,
          senderName = "Hassan", // To Update
          timestamp = System.currentTimeMillis())
    }
    // User send only a text
    !messageText.isNullOrEmpty() -> {
      ChatMessage.TextMessage(
          message = messageText,
          senderId = userId,
          senderName = "Hassan", // To Update
          timestamp = System.currentTimeMillis())
    }
    // User send only a message
    !imageUrl.isNullOrEmpty() -> {
      ChatMessage.ImageMessage(
          imageUrl = imageUrl,
          senderId = userId,
          senderName = "Hassan", // To Update
          timestamp = System.currentTimeMillis())
    }
    else -> null
  }
}
