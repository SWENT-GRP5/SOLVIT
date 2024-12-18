package com.android.solvit.shared.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.ui.review.MapCard
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.AiSolverViewModel
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import com.android.solvit.shared.model.chat.ChatMessage
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.utils.isInternetAvailable
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.Black
import com.android.solvit.shared.ui.theme.Carpenter
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.getReceiverImageUrl
import com.android.solvit.shared.ui.utils.getReceiverName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Composable for the chat screen, where users can send and receive messages in a chat conversation.
 *
 * @param navigationActions Actions to navigate to different screens
 * @param chatViewModel ViewModel for chat messages
 * @param authViewModel ViewModel for user authentication
 * @param chatAssistantViewModel ViewModel for AI chat assistant
 * @param serviceRequestViewModel ViewModel for service requests
 */
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun ChatScreen(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    chatAssistantViewModel: ChatAssistantViewModel,
    serviceRequestViewModel: ServiceRequestViewModel
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  chatAssistantViewModel.clear()

  val messages by chatViewModel.coMessage.collectAsStateWithLifecycle()
  val receiver by chatViewModel.receiver.collectAsStateWithLifecycle()
  val receiverName = getReceiverName(receiver)
  val receiverPicture = getReceiverImageUrl(receiver)
  val user by authViewModel.user.collectAsStateWithLifecycle()
  val isSeeker = user?.role == "seeker"
  chatViewModel.getChatRequest { id ->
    serviceRequestViewModel.getServiceRequestById(id) { chatViewModel.setChatRequest(it) }
  }
  val request by chatViewModel.chatRequest.collectAsStateWithLifecycle()
  chatAssistantViewModel.setContext(messages, user?.userName ?: "", receiverName, request)

  // To send Image Messages
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  val localContext = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("ChatScreen"),
      topBar = {
        ChatHeader(
            name = receiverName, picture = receiverPicture, navigationActions = navigationActions)
      },
      bottomBar = {
        MessageInputBar(
            chatAssistantViewModel = chatAssistantViewModel,
            isAiSolverScreen = false,
            isSeeker = isSeeker,
            onImageSelected = { uri: Uri? ->
              imageUri = uri
              uri?.let { imageBitmap = loadBitmapFromUri(localContext, it) }
            },
            onSendClickButton = { textMessage, onMessageChange ->
              chatViewModel.viewModelScope.launch {
                // Don't Forget To Update Context
                // upload Image User want to send to storage
                val imageUrl =
                    if (imageUri != null) chatViewModel.uploadImagesToStorage(imageUri!!) else null
                val message = user?.let { buildMessage(it.uid, textMessage, imageUrl, it.userName) }
                if (message != null) {
                  chatViewModel.sendMessage(false, message)

                  onMessageChange("")
                  imageUri = null
                  imageBitmap = null
                }
              }
            },
            context = context)
      }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
        ) {
          if (request != null) {
            // Display the request details
            RequestDetails(request!!, serviceRequestViewModel, navigationActions)
          }

          val listState = rememberLazyListState()
          LaunchedEffect(messages, request) { listState.animateScrollToItem(messages.size) }
          LazyColumn(state = listState, modifier = Modifier.imePadding().testTag("conversation")) {
            items(messages) { message ->
              if (message.senderId == user?.uid) {
                // Item for messages authenticated user send
                SentMessage(message, true)
              } else {
                // Item for messages authenticated user receive
                SentMessage(
                    message, isSentByUser = false, showProfilePicture = true, receiverPicture)
              }
            }
          }
        }
      }
}

/**
 * Composable for displaying the Welcome screen for the AI Problem Solver.
 *
 * @param navigationActions Actions to navigate to different screens
 * @param chatViewModel ViewModel for chat messages
 * @param authViewModel ViewModel for user authentication
 */
@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSolverWelcomeScreen(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  val user by authViewModel.user.collectAsStateWithLifecycle()
  val iaBotUserId = "JL36T8yHjWDYkuq4u6S4" // The default Id of the IA Bot created
  // Fetch conversation
  val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
  val conversation by chatViewModel.coMessage.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) { chatViewModel.prepareForIaChat(true, user?.uid, iaBotUserId, "IaBot") }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background))
      },
  ) { innerPadding ->
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxSize()
                .background(color = colorScheme.background)
                .padding(innerPadding)
                .testTag("AiGetStartedScreen"),
        contentAlignment = Alignment.TopCenter) {
          if (!isInternetAvailable(context)) {
            Text(
                text = "AI solver requires Internet Connection",
                color = colorScheme.error,
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("noInternetConnection").align(Alignment.Center))
          } else if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
          } else {
            val screenHeight = maxHeight

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier.fillMaxSize()
                        .padding(
                            top = screenHeight.times(0.05f), bottom = screenHeight.times(0.05f))) {
                  Text(
                      text =
                          buildAnnotatedString {
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = Black,
                                        fontSize = screenHeight.times(0.04f).value.sp)) {
                                  append("Your Personal ")
                                }
                            withStyle(
                                style =
                                    SpanStyle(
                                        brush =
                                            Brush.linearGradient(
                                                colors = listOf(colorScheme.primary, Carpenter),
                                                start = Offset.Zero,
                                                end = Offset.Infinite),
                                        fontSize = screenHeight.times(0.04f).value.sp)) {
                                  append("\n\nAI Problem Solver")
                                }
                          },
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(horizontal = 16.dp).testTag("title"))

                  Image(
                      modifier = Modifier.testTag("image"),
                      painter = painterResource(id = R.drawable.ai_image),
                      contentDescription = "ai logo",
                      contentScale = ContentScale.FillBounds)

                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "I'm pleased that I meet you! How can\nI help you right now?",
                            fontSize = screenHeight.times(0.02f).value.sp,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier.padding(horizontal = 16.dp)
                                    .padding(top = screenHeight.times(0.01f)))
                        Spacer(modifier = Modifier.height(screenHeight.times(0.05f)))
                        if (conversation.isEmpty()) {
                          AiSolverButton(
                              screenHeight = screenHeight,
                              title = "Let solve a new problem",
                              onClick = {
                                chatViewModel.setShouldCreateRequest(false)
                                navigationActions.navigateTo(Screen.AI_SOLVER_CHAT_SCREEN)
                              })
                        } else {
                          AiSolverButton(
                              screenHeight = screenHeight,
                              title = "Resume Chat",
                              onClick = {
                                navigationActions.navigateTo(Screen.AI_SOLVER_CHAT_SCREEN)
                              })
                          Spacer(modifier = Modifier.height(8.dp))
                          AiSolverButton(
                              screenHeight = screenHeight,
                              title = "Let solve a new problem",
                              onClick = {
                                chatViewModel.clearConversation(true)
                                chatViewModel.setShouldCreateRequest(false)
                                navigationActions.navigateTo(Screen.AI_SOLVER_CHAT_SCREEN)
                              })
                        }
                      }
                }
          }
        }
  }
}

/**
 * Represent either a solve new Problem Button or continue solving current Problem
 *
 * @param screenHeight height of the screen
 * @param title title of the button
 * @param onClick action to perform when clicking on the button
 */
@Composable
fun AiSolverButton(screenHeight: Dp = 700.dp, title: String, onClick: () -> Unit) {
  Button(
      onClick = { onClick() },
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      contentPadding = PaddingValues(),
      modifier =
          Modifier.fillMaxWidth(0.8f)
              .height(screenHeight.times(0.07f))
              .testTag("getStartedButton")
              .shadow(
                  elevation = 8.dp,
                  shape = RoundedCornerShape(16.dp),
                  ambientColor = colorScheme.primary,
                  spotColor = Carpenter)) {
        // Gradient background
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors = listOf(colorScheme.primary, Carpenter)),
                        shape = RoundedCornerShape(50)),
            contentAlignment = Alignment.Center) {
              // Button text
              Text(
                  text = title,
                  fontSize = screenHeight.times(0.025f).value.sp,
                  color = colorScheme.surface,
                  maxLines = 1, // Ensure text stays on one line
                  overflow = TextOverflow.Ellipsis)
            }
      }
}

/**
 * Chat with Ai problem solver chatbot
 *
 * @param navigationActions to navigate back to the previous screen
 * @param authViewModel ViewModel for user authentication
 * @param chatViewModel ViewModel for chat messages
 * @param aiSolverViewModel ViewModel for AI problem solver
 */
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun AiSolverScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    aiSolverViewModel: AiSolverViewModel
) {

  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  // To send Image Messages
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var reply by remember { mutableStateOf("") }
  val localContext = LocalContext.current
  val conversation by chatViewModel.coMessage.collectAsStateWithLifecycle()
  val user by authViewModel.user.collectAsStateWithLifecycle()
  var isTyping by remember { mutableStateOf(false) }
  // Boolean indicating if given the problem of the user he should create a request to solve it
  val shouldCreateRequest by chatViewModel.shouldCreateRequest.collectAsStateWithLifecycle()
  aiSolverViewModel.setMessageContext(conversation)

  val lazyListState = rememberLazyListState()

  // Scroll to the last item when entering the screen
  LaunchedEffect(conversation) {
    if (conversation.isNotEmpty()) {
      lazyListState.animateScrollToItem(conversation.lastIndex)
    }
  }
  Scaffold(
      modifier = Modifier.testTag("AiSolverScreen"),
      topBar = { AiSolverHeader(navigationActions) },
      bottomBar = {
        if (!shouldCreateRequest) {
          MessageInputBar(
              isAiSolverScreen = true,
              isSeeker = true,
              onImageSelected = { uri: Uri? ->
                imageUri = uri
                uri?.let { imageBitmap = loadBitmapFromUri(localContext, it) }
              },
              onSendClickButton = { textMessage, onMessageChange ->
                chatViewModel.viewModelScope.launch {
                  // upload Image User want to send to storage
                  val imageUrl =
                      if (imageUri != null) chatViewModel.uploadImagesToStorage(imageUri!!)
                      else null
                  val message =
                      user?.let { buildMessage(it.uid, textMessage, imageUrl, it.userName) }
                  if (message != null) {
                    chatViewModel.sendMessage(true, message)
                    onMessageChange("")
                    val userInput = AiSolverViewModel.UserInput(textMessage, imageBitmap)
                    aiSolverViewModel.updateMessageContext(message)
                    aiSolverViewModel.generateMessage(
                        userInput,
                        onSuccess = {
                          chatViewModel.setShouldCreateRequest(it.shouldCreateRequest)
                          reply = it.response
                          val aiReply =
                              ChatMessage.TextMessage(
                                  message = reply,
                                  senderName = "AiBot",
                                  senderId = "JL36T8yHjWDYkuq4u6S4",
                                  timestamp = System.currentTimeMillis())

                          chatViewModel.sendMessage(true, aiReply)
                          isTyping = false
                        })
                    isTyping = true
                    imageUri = null
                    imageBitmap = null
                  }
                }
              },
              context = context)
        } else {
          // In case the user should create a request we display a button so that he directly
          // navigate to the create Request Screen
          SuggestionToCreateRequest(
              navigationActions = navigationActions,
              onClick = { chatViewModel.setShouldCreateRequest(false) })
        }
      }) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(paddingValues).imePadding().testTag("chat")) {
              items(conversation) { message ->
                if (message.senderId == user?.uid) {
                  SentMessage(message, isSentByUser = true)
                } else {
                  SentMessage(message, isSentByUser = false, showProfilePicture = true, "")
                }
              }
              if (isTyping) {
                item { TypingIndicator() }
              }
            }
      }
}

/**
 * Header of Chat with Ai problem solver
 *
 * @param navigationActions to navigate back to the previous screen
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AiSolverHeader(navigationActions: NavigationActions) {
  TopAppBar(
      modifier = Modifier.testTag("AiChatHeader"),
      title = { Text(text = "AI Solver", style = Typography.titleLarge) },
      navigationIcon = {
        IconButton(onClick = { navigationActions.goBack() }) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = colorScheme.background,
              titleContentColor = colorScheme.onSurface,
              navigationIconContentColor = colorScheme.onSurface,
              actionIconContentColor = colorScheme.onSurface),
  )
}

/** Typing indicator while the IA is generating a response */
@Composable
fun TypingIndicator() {
  var dots by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    while (true) {
      dots =
          when (dots) {
            "" -> "."
            "." -> ".."
            ".." -> "..."
            else -> ""
          }
      delay(500) // Update every 500ms
    }
  }

  Text(
      modifier = Modifier.padding(5.dp).testTag("TypingIndicator"),
      text = "AiBot is typing$dots",
      style = Typography.bodyLarge,
      color = colorScheme.onSurface)
}

/**
 * Composable for displaying the header of the chat screen with the name and profile picture of the
 * chat partner.
 *
 * @param name The name of the chat partner
 * @param picture The profile picture of the chat partner
 * @param navigationActions Actions to navigate to different screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(name: String?, picture: String, navigationActions: NavigationActions) {
  TopAppBar(
      modifier = Modifier.testTag("ChatHeader"),
      colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
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

          Text(text = name ?: "Empty Name", style = Typography.titleSmall)
        }
      },
      navigationIcon = {
        IconButton(onClick = { navigationActions.goBack() }) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      })
}

/**
 * Composable for displaying a chat message in the chat screen.
 *
 * @param message The chat message to display
 * @param isSentByUser Boolean indicating if the message was sent by the user
 * @param showProfilePicture Boolean indicating if the profile picture should be displayed
 * @param receiverPicture The profile picture of the chat partner
 */
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
            modifier = Modifier.padding(horizontal = 8.dp) // Add padding around the bubble
            ) {
              val maxBubbleWidth =
                  maxWidth * 0.8f // Dynamically calculate 80% of the available width
              Box(
                  modifier =
                      Modifier.widthIn(max = maxBubbleWidth) // Constrain bubble width
                          .wrapContentWidth() // Wrap content width to ensure text wraps
                          .clip(
                              RoundedCornerShape(
                                  topStart = if (isSentByUser) 16.dp else 0.dp,
                                  topEnd = if (isSentByUser) 0.dp else 16.dp,
                                  bottomStart = 16.dp,
                                  bottomEnd = 16.dp))
                          .background(
                              if (isSentByUser) colorScheme.primary else colorScheme.surfaceVariant)
                          .padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Message Item take different forms given the format
                    when (message) {
                      is ChatMessage.TextMessage ->
                          Text(
                              text =
                                  buildAnnotatedString {
                                    // To convert in bold some part of IA generated messages
                                    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
                                    var lastIndex = 0

                                    boldRegex.findAll(message.message).forEach { matchResult ->
                                      val start = matchResult.range.first
                                      val end = matchResult.range.last
                                      val boldText = matchResult.groupValues[1]

                                      append(message.message.substring(lastIndex, start))

                                      withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(boldText)
                                      }

                                      lastIndex = end + 1
                                    }

                                    append(message.message.substring(lastIndex))
                                  },
                              color =
                                  if (isSentByUser) colorScheme.background
                                  else colorScheme.onSurface,
                              style = Typography.bodySmall)
                      is ChatMessage.ImageMessage -> {
                        Log.e("display Image", message.imageUrl)
                        AsyncImage(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp)),
                            model = message.imageUrl,
                            placeholder = painterResource(id = R.drawable.loading),
                            error = painterResource(id = R.drawable.error),
                            contentDescription = "Image message",
                            contentScale = ContentScale.Crop,
                        )
                      }
                      is ChatMessage.TextImageMessage -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                              Text(
                                  text = message.text,
                                  color =
                                      if (isSentByUser) colorScheme.onPrimary
                                      else colorScheme.onSurface,
                                  style = Typography.bodySmall)

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
}

/**
 * Composable displayed in the bottom bar of the AI chat screen with 2 possibilities (either
 * creating a request or continue chatting)
 *
 * @param navigationActions to navigate create request screen on click
 * @param onClick action to perform when clicking on "Continue chatting"
 */
@Composable
fun SuggestionToCreateRequest(navigationActions: NavigationActions, onClick: () -> Unit) {
  Box(modifier = Modifier.imePadding(), contentAlignment = Alignment.BottomCenter) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("finishChatOptions"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          AiSolverButton(title = "Contact a professional") {
            navigationActions.navigateTo(Route.CREATE_REQUEST)
          }

          Spacer(modifier = Modifier.height(8.dp)) // Adds spacing between button and text

          Text(
              text = "Continue chatting",
              fontSize = 18.sp,
              textDecoration = TextDecoration.Underline,
              color = Carpenter,
              modifier = Modifier.clickable { onClick() }.testTag("continueChattingButton"))
        }
  }
}

/**
 * Composable for displaying the message input bar at the bottom of the chat screen.
 *
 * @param chatAssistantViewModel ViewModel for AI chat assistant
 * @param onImageSelected Callback when an image is selected
 * @param isSeeker Boolean indicating if the user is a seeker
 * @param isAiSolverScreen Boolean indicating if the screen is the AI Problem Solver screen
 * @param onSendClickButton Callback when the send button is clicked
 */
@Composable
fun MessageInputBar(
    chatAssistantViewModel: ChatAssistantViewModel =
        viewModel(factory = ChatAssistantViewModel.Factory),
    onImageSelected: (Uri?) -> Unit,
    isSeeker: Boolean,
    isAiSolverScreen: Boolean,
    onSendClickButton: (String, (String) -> Unit) -> Unit,
    context: Context
) {
  var message by remember { mutableStateOf("") }
  var imageUri by remember { mutableStateOf<Uri?>(null) }

  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? ->
            imageUri = uri
            onImageSelected(uri)
          })

  // Control for showing the AI Assistant dialog
  var showDialog by remember { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.background(color = colorScheme.surface, shape = RoundedCornerShape(size = 28.dp))
              .padding(horizontal = 8.dp, vertical = 8.dp)
              .imePadding()
              .testTag("SendMessageBar")) {
        if (!isAiSolverScreen) {
          AssistantSuggestions(chatAssistantViewModel, isSeeker, context) {
            chatAssistantViewModel.updateSelectedTones(emptyList())
            chatAssistantViewModel.generateMessage(it, isSeeker) { msg -> message = msg }
          }
        }

        // If an image is selected, display it above the text field
        imageUri?.let { uri ->
          Box(
              modifier =
                  Modifier.clip(RoundedCornerShape(12.dp))
                      .background(colorScheme.surface)
                      .padding(bottom = 8.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Uploaded Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(70.dp).clip(RoundedCornerShape(12.dp)))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Image",
                    tint = colorScheme.error,
                    modifier =
                        Modifier.align(Alignment.TopEnd).padding(4.dp).clickable {
                          imageUri = null
                          onImageSelected(null)
                        })
              }
        }

        // Row containing the TextField and action buttons
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .background(
                        color = colorScheme.surface, shape = RoundedCornerShape(size = 28.dp))
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              // TextField for entering the message
              TextField(
                  value = message,
                  onValueChange = { message = it },
                  modifier =
                      Modifier.weight(1f)
                          .heightIn(min = 56.dp) // Matches the height of buttons for alignment
                          .padding(end = 8.dp)
                          .testTag("enterText"),
                  placeholder = {
                    Text(
                        text = "Send Message",
                        color = colorScheme.onSurfaceVariant,
                        style = Typography.bodyLarge)
                  },
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedBorderColor = colorScheme.background,
                          focusedBorderColor = colorScheme.background,
                          focusedTextColor = colorScheme.onSurface),
                  maxLines = 5)

              // Optional AI Chat Assistant Button
              if (!isAiSolverScreen) {
                IconButton(
                    onClick = {
                      if (isInternetAvailable(context)) {
                        showDialog = true
                      } else {
                        Toast.makeText(
                                context,
                                "Chat Assistant requires Internet Connection",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    modifier = Modifier.size(48.dp).testTag("aiButton")) {
                      Icon(
                          painter = painterResource(R.drawable.ai_message),
                          contentDescription = "Chat Assistant",
                          tint = colorScheme.onSurfaceVariant)
                    }
              }

              // Button to upload an image
              IconButton(
                  onClick = { imagePickerLauncher.launch("image/*") },
                  modifier = Modifier.size(48.dp).testTag("uploadImageButton")) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Upload Image",
                        tint = colorScheme.onSurfaceVariant)
                  }

              // Button to send the message
              IconButton(
                  onClick = {
                    imageUri = null
                    if (!isInternetAvailable(context)) {
                      Toast.makeText(
                              context,
                              "Your messages will be sent when you are back online",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                    onSendClickButton(message) { message = it }
                  },
                  modifier = Modifier.size(48.dp).testTag("sendMessageButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(-45f))
                  }
            }
      }

  // AI Assistant Dialog if triggered
  if (showDialog) {
    ChatAssistantDialog(
        chatAssistantViewModel,
        isSeeker,
        onDismiss = { showDialog = false },
        onResponse = { message = it })
  }
}

/**
 * Handle different messages possible format a user send
 *
 * @param userId id of the user
 * @param messageText text of the message
 * @param imageUrl url of the image
 * @param senderName name of the sender
 */
fun buildMessage(
    userId: String,
    messageText: String?,
    imageUrl: String?,
    senderName: String
): ChatMessage? {
  return when {
    // User send both a text and an Image
    !messageText.isNullOrEmpty() && !imageUrl.isNullOrEmpty() -> {
      ChatMessage.TextImageMessage(
          text = messageText,
          imageUrl = imageUrl,
          senderId = userId,
          senderName = senderName,
          timestamp = System.currentTimeMillis())
    }
    // User send only a text
    !messageText.isNullOrEmpty() -> {
      ChatMessage.TextMessage(
          message = messageText,
          senderId = userId,
          senderName = senderName,
          timestamp = System.currentTimeMillis())
    }
    // User send only a message
    !imageUrl.isNullOrEmpty() -> {
      ChatMessage.ImageMessage(
          imageUrl = imageUrl,
          senderId = userId,
          senderName = senderName,
          timestamp = System.currentTimeMillis())
    }
    else -> null
  }
}

/**
 * Composable for displaying the AI Chat Assistant dialog.
 *
 * @param chatAssistantViewModel ViewModel for AI chat assistant
 * @param isSeeker Boolean indicating if the user is a seeker
 * @param onSuggestionSelect Callback when a suggestion is selected
 */
@Composable
fun AssistantSuggestions(
    chatAssistantViewModel: ChatAssistantViewModel,
    isSeeker: Boolean,
    context: Context,
    onSuggestionSelect: (String) -> Unit
) {
  val suggestions by chatAssistantViewModel.suggestions.collectAsStateWithLifecycle()
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .horizontalScroll(rememberScrollState())
              .testTag("aiSuggestions"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ai_stars),
            contentDescription = "AI suggestions",
            Modifier.padding(horizontal = 8.dp),
            tint = colorScheme.onSurfaceVariant)
        if (!isInternetAvailable(context)) {
          Text("No Internet Connection", color = colorScheme.error, style = Typography.bodyLarge)
        } else if (suggestions.isEmpty()) {
          Text(
              "Load Suggestions",
              color = colorScheme.onSurfaceVariant,
              style = Typography.bodyLarge)
        } else {
          suggestions.forEach { suggestion ->
            Text(
                text = suggestion,
                modifier =
                    Modifier.border(1.dp, colorScheme.onSurfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { onSuggestionSelect(suggestion) }
                        .padding(8.dp)
                        .testTag("suggestionItem$suggestion"),
                color = colorScheme.onSurfaceVariant,
                style = Typography.bodyLarge)
          }
        }
        IconButton(
            onClick = {
              if (isInternetAvailable(context)) {
                chatAssistantViewModel.generateSuggestions(isSeeker) {
                  Log.e("ChatScreen", "Suggestions generated")
                }
              } else {
                Toast.makeText(
                        context, "Chat Assistant requires Internet Connection", Toast.LENGTH_SHORT)
                    .show()
              }
            }) {
              Icon(
                  Icons.Default.Refresh,
                  contentDescription = "Generate suggestions",
                  tint = colorScheme.onSurfaceVariant)
            }
      }
}

/**
 * Composable for displaying a request's details.
 *
 * @param serviceRequest The service request to display
 * @param serviceRequestViewModel ViewModel for service requests
 * @param navigationActions Actions to navigate to different screens
 */
@Composable
fun RequestDetails(
    serviceRequest: ServiceRequest,
    serviceRequestViewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  OutlinedCard(
      modifier =
          Modifier.fillMaxWidth().padding(16.dp).clickable {
            serviceRequestViewModel.selectRequest(serviceRequest)
            navigationActions.navigateTo(Route.BOOKING_DETAILS)
          },
      colors =
          CardColors(
              colorScheme.background,
              colorScheme.onBackground,
              colorScheme.background,
              colorScheme.onBackground),
  ) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).testTag("requestCard")) {
      Column(modifier = Modifier.fillMaxWidth(.4f).padding(8.dp)) {
        serviceRequest.location?.let { MapCard(it) }
      }
      Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = serviceRequest.title,
            modifier = Modifier.testTag("requestTitle"),
            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis)
        Text(
            text = serviceRequest.description,
            modifier = Modifier.testTag("requestDescription"),
            style = Typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis)
      }
    }
  }
}
