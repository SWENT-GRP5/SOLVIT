package com.android.solvit.shared.ui.booking

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.provider.Note
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A composable function that displays a service booking screen for users, including a top bar,
 * service details, price, appointment, and a map showing the service location.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBookingScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    seekerProfileViewModel: SeekerProfileViewModel =
        viewModel(factory = SeekerProfileViewModel.Factory),
    providerViewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    packageViewModel: PackageProposalViewModel =
        viewModel(factory = PackageProposalViewModel.Factory),
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  val isReadyToNavigate by chatViewModel.isReadyToNavigate.collectAsState()
  LaunchedEffect(isReadyToNavigate) {
    if (isReadyToNavigate) {
      navigationActions.navigateAndSetBackStack(Screen.CHAT, listOf(Route.INBOX))
      chatViewModel.resetIsReadyToNavigate()
    }
  }

  val user = authViewModel.user.collectAsState().value
  val role = user?.role ?: "seeker"
  val seekerState = remember { mutableStateOf<Any?>(null) }

  val request by requestViewModel.selectedRequest.collectAsState()
  if (request == null) return
  val providerId = request!!.providerId
  val provider =
      if (providerId.isNullOrEmpty()) null
      else
          providerViewModel.providersList.collectAsState().value.firstOrNull {
            it.uid == providerId
          }

  LaunchedEffect(request) {
    val seeker = request?.userId?.let { seekerProfileViewModel.fetchUserById(it) }
    seekerState.value = seeker
  }

  val packageId = request!!.packageId
  val packageProposal =
      if (packageId.isNullOrEmpty()) null
      else packageViewModel.proposal.collectAsState().value.firstOrNull { it.uid == packageId }

  val isSeeker = role == "seeker"

  val isPending = request!!.status == ServiceRequestStatus.PENDING
  val isAccepted = request!!.status == ServiceRequestStatus.ACCEPTED
  val isScheduled = request!!.status == ServiceRequestStatus.SCHEDULED
  val isCompleted = request!!.status == ServiceRequestStatus.COMPLETED
  val acceptedOrScheduled = isAccepted || isScheduled
  val pendingOrAcceptedOrScheduled = isPending || acceptedOrScheduled
  // Scaffold provides the basic structure for the screen with a top bar and content
  Scaffold(
      modifier = Modifier.testTag("service_booking_screen"),
      topBar = {
        // TopAppBar displays the navigation icon and title of the screen
        TopAppBar(
            colors =
                TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                ),
            title = {
              Text(
                  text = "Your booking",
                  modifier = Modifier.testTag("booking_title"),
                  color = colorScheme.onBackground,
                  textAlign = TextAlign.Center,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              // Navigation icon to go back to the previous screen (currently unhandled)
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground)
                  }
            },
            actions = {
              // Empty action space to balance the top bar layout (could be used for future actions)
              Box(modifier = Modifier.size(48.dp))
            })
      }) { innerPadding ->
        // Main content of the screen inside a Column
        Column(
            modifier =
                Modifier.fillMaxSize() // Fill the entire screen
                    .padding(16.dp)
                    .background(colorScheme.background) // White background for the screen
                    .padding(innerPadding) // Additional padding to respect Scaffold's inner padding
                    .verticalScroll(rememberScrollState()) // Make the content scrollable
            ) {
              Text(
                  text = "Details", // Title for the provider details
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 8.dp).testTag("details_label"))

              // A row containing two parallel sections: Profile/Rating and Price/Appointment
              Row(
                  modifier =
                      Modifier.fillMaxWidth() // Occupies full width of the screen
                          .height(220.dp) // Fixed height for uniformity
                          // Padding between the row and other components
                          .padding(vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween // Space between the boxes
                  ) {
                    // Left box: Provider card
                    Box(
                        modifier =
                            Modifier.weight(1f) // Equal space
                                .testTag("profile_box")
                                .fillMaxHeight()
                                .background(
                                    colorScheme.onSurface.copy(alpha = 0.8f),
                                    RoundedCornerShape(16.dp))
                                .clickable(
                                    onClick = {
                                      if (isSeeker) {
                                        providerViewModel.selectService(request!!.type)
                                        navigationActions.navigateTo(Route.PROVIDERS_LIST)
                                      }
                                    })) {
                          if (provider != null) {
                            // Render provider card when provider is selected
                            ProviderCard(provider, isSeeker, providerViewModel, navigationActions)
                          } else {
                            // Render placeholder when no provider is selected
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                  Icon(
                                      imageVector =
                                          Icons.Default.AccountCircle, // Use an outline person icon
                                      contentDescription = null,
                                      tint = colorScheme.onPrimary,
                                      modifier = Modifier.size(64.dp) // Larger icon for visibility
                                      )
                                  Spacer(modifier = Modifier.height(8.dp))
                                  Text(
                                      text =
                                          if (isSeeker) "Select a Provider"
                                          else "No Provider Assigned",
                                      fontWeight = FontWeight.Bold,
                                      fontSize = 20.sp,
                                      color = colorScheme.onPrimary,
                                      textAlign = TextAlign.Center)
                                }
                          }
                        }
                    Spacer(modifier = Modifier.width(16.dp)) // Space between the two boxes

                    // Right box: Price and appointment information
                    Box(
                        modifier =
                            Modifier.weight(1f)
                                .fillMaxHeight()
                                .background(colorScheme.primary, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                                .testTag("price_appointment_box")) {
                          Column(
                              // Center the text inside the column
                              horizontalAlignment = Alignment.CenterHorizontally,
                              // Ensure the column takes up the full width
                              modifier = Modifier.fillMaxWidth()) {
                                var price = "Not set"
                                request!!.agreedPrice?.let { price = "$it CHF" }
                                // Price agreed upon
                                Text(
                                    text = "Price agreed on:",
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.secondaryContainer)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                  Text(
                                      text = price,
                                      fontWeight = FontWeight.Bold,
                                      fontSize = 20.sp,
                                      textAlign = TextAlign.Center,
                                      color = colorScheme.onPrimary,
                                      modifier =
                                          if (acceptedOrScheduled) Modifier.weight(1f)
                                          else Modifier)
                                  if (acceptedOrScheduled && !isSeeker) {
                                    // Edit button for the price
                                    EditPriceDialog(request!!, requestViewModel)
                                  }
                                }

                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                var date = "Not set"
                                var time = "Not set"
                                request!!.meetingDate.let {
                                  if (it != null) {
                                    date = dateFormat.format(it.toDate())
                                    time = timeFormat.format(it.toDate())
                                  }
                                }
                                // Appointment date and time
                                Text(
                                    modifier = Modifier.testTag("appointment_date"),
                                    text = "Your appointment:",
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.secondaryContainer)
                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)) // Space between the text and date
                                Row {
                                  Column(
                                      modifier =
                                          if (acceptedOrScheduled) Modifier.weight(1f)
                                          else Modifier) {
                                        Text(
                                            text = date,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            color = colorScheme.onPrimary)
                                        Text(
                                            text = time,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center,
                                            color = colorScheme.onPrimary)
                                      }
                                  if (acceptedOrScheduled && isSeeker) {
                                    DateAndTimePickers(request!!, requestViewModel)
                                  }
                                }
                              }
                        }
                  }
              // Problem description section
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = "Problem Description", // Title for the problem description
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 8.dp).testTag("problem_description_label"))

              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                  border = BorderStroke(2.dp, colorScheme.onBackground.copy(alpha = 0.12f)),
                  elevation = CardDefaults.cardElevation(0.dp) // No shadow
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Text(
                          text = request!!.description,
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.onSurface,
                          modifier = Modifier.testTag("problem_description"))
                    }
                  }

              if (!request!!.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Provided Image", // Title for the image display
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp).testTag("problem_image_label"))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                    border = BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(0.dp) // No shadow
                    ) {
                      AsyncImage(
                          modifier = Modifier.fillMaxSize().height(200.dp).testTag("problem_image"),
                          model = request!!.imageUrl,
                          placeholder = painterResource(id = R.drawable.loading),
                          error = painterResource(id = R.drawable.error),
                          contentDescription = "problem_image",
                          contentScale = ContentScale.Crop)
                    }
              }

              if (packageProposal != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your chosen Package",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp).testTag("package_label"))
                PackageCard(packageProposal)
              }

              // Address and map section
              Text(
                  text = "Address", // Address label
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).testTag("address_label"))

              val address = request!!.location
              // Google Map showing the service location
              val mapPosition = rememberCameraPositionState()

              LaunchedEffect(address) {
                mapPosition.position =
                    com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                        LatLng(address?.latitude ?: 0.0, address?.longitude ?: 0.0), 15f)
              }

              // Container for the Google Map
              Box(
                  modifier =
                      Modifier.fillMaxWidth() // Fill the available width
                          .height(200.dp) // Fixed height for the map
                          .background(colorScheme.surface, RoundedCornerShape(16.dp))
                          .testTag("google_map_container") // Test tag for UI testing
                  ) {
                    GoogleMap(
                        cameraPositionState = mapPosition,
                        modifier =
                            Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))) // Display the map
              }

              if (pendingOrAcceptedOrScheduled) {
                // If on Seeker View, Init a chat with as receiver the provider
                if (isSeeker) {
                  if (provider != null && providerId != null) {
                    EditAndChatButton(
                        currentUserId = user?.uid ?: "",
                        navigationActions = navigationActions,
                        chatViewModel = chatViewModel,
                        receiverId = providerId,
                        receiver = provider,
                        requestId = request!!.uid,
                        isPending = isPending,
                        isSeeker = true,
                        modifier = Modifier.weight(1f))
                  } else {
                    EditButton(navigationActions, isPending, true)
                  }
                  // Else Init Chat with as receiver Seeker
                } else {
                  if (seekerState.value != null) {
                    EditAndChatButton(
                        currentUserId = user?.uid ?: "",
                        navigationActions = navigationActions,
                        chatViewModel = chatViewModel,
                        receiverId = request?.userId ?: "",
                        receiver = seekerState.value!!,
                        requestId = request!!.uid,
                        isPending = isPending,
                        isSeeker = false,
                        modifier = Modifier.weight(1f))
                  }
                }
              }
              if (isSeeker && isCompleted) {
                ReviewButton(navigationActions)
              }
            }
      }
}

/**
 * A composable function that displays a provider card with the provider's name and rating.
 *
 * @param provider The provider to display in the card.
 * @param providerViewModel The ViewModel for the provider list.
 * @param navigationActions The navigation actions to navigate to the provider's profile.
 */
@Composable
fun ProviderCard(
    provider: Provider,
    isSeeker: Boolean,
    providerViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  Card(
      modifier =
          Modifier.testTag("provider_card").clickable {
            if (isSeeker) {
              providerViewModel.selectProvider(provider)
              navigationActions.navigateTo(Route.PROVIDER_INFO)
            }
          },
      elevation =
          CardDefaults.cardElevation(
              defaultElevation = 8.dp, pressedElevation = 4.dp, focusedElevation = 10.dp),
      shape = RoundedCornerShape(16.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
          AsyncImage(
              modifier = Modifier.fillMaxSize(),
              model = provider.imageUrl.ifEmpty { R.drawable.empty_profile_img },
              placeholder = painterResource(id = R.drawable.loading),
              error = painterResource(id = R.drawable.error),
              contentDescription = "provider image",
              contentScale = ContentScale.Crop)

          // Add gradient overlay
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(72.dp)
                      .align(Alignment.BottomCenter)
                      .background(
                          brush =
                              Brush.verticalGradient(
                                  colors = listOf(Color.Transparent, colorScheme.background))))

          Row(
              modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = provider.name.uppercase(),
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(400),
                            color = colorScheme.onBackground),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Note(provider.rating.toInt().toString())
              }
        }
      }
}

/**
 * A composable function that displays an edit button to edit the service request details.
 *
 * @param navigationActions The navigation actions to navigate to the edit request screen.
 * @param isPending Indicates if the request is pending.
 * @param isSeeker Indicates if the current user is the service seeker.
 */
@Composable
fun EditButton(
    navigationActions: NavigationActions,
    isPending: Boolean,
    isSeeker: Boolean,
    modifier: Modifier = Modifier
) {
  if (isPending && isSeeker) {
    Box(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp).testTag("edit_button"),
        contentAlignment = Alignment.Center) {
          Button(
              onClick = { navigationActions.navigateTo(Route.EDIT_REQUEST) },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
              shape = RoundedCornerShape(8.dp)) {
                Text(text = "Edit details", style = typography.labelLarge)
              }
        }
  }
}

@Composable
fun ChatButton(
    chatViewModel: ChatViewModel,
    currentUserId: String,
    receiverId: String,
    receiver: Any,
    requestId: String,
    modifier: Modifier = Modifier
) {
  Box(
      modifier = modifier.fillMaxWidth().padding(top = 8.dp).testTag("chat_button"),
      contentAlignment = Alignment.Center) {
        Button(
            onClick = {
              chatViewModel.prepareForChat(false, currentUserId, receiverId, receiver, requestId)
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
            shape = RoundedCornerShape(8.dp)) {
              Text(text = "Discuss", style = typography.labelLarge)
            }
      }
}

/**
 * A composable function that displays an edit button to edit the service request details and a chat
 * button to initiate a chat with the provider or the seeker.
 *
 * @param navigationActions The navigation actions to navigate to the edit request screen.
 * @param currentUserId The ID of the current user.
 * @param chatViewModel The ViewModel for the chat.
 * @param receiverId The ID of the receiver of the chat.
 * @param receiver The receiver of the chat.
 */
@Composable
fun EditAndChatButton(
    navigationActions: NavigationActions,
    currentUserId: String,
    chatViewModel: ChatViewModel,
    receiverId: String,
    receiver: Any,
    requestId: String,
    isPending: Boolean,
    isSeeker: Boolean,
    modifier: Modifier
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("edit_discuss_button"),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    EditButton(navigationActions, isPending, isSeeker, modifier)
    ChatButton(chatViewModel, currentUserId, receiverId, receiver, requestId, modifier)
  }
}

/**
 * A composable function that displays a review button to leave a review for the service.
 *
 * @param navigationActions The navigation actions to navigate to the review screen.
 */
@Composable
fun ReviewButton(navigationActions: NavigationActions) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("review_button"),
      contentAlignment = Alignment.Center) {
        Button(
            onClick = { navigationActions.navigateTo(Route.REVIEW) },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
            shape = RoundedCornerShape(8.dp)) {
              Text(text = "Leave a review", style = typography.labelLarge)
            }
      }
}

/**
 * A composable function that displays a package card with the package details.
 *
 * @param packageProposal The package proposal to display in the card.
 */
@Composable
fun PackageCard(packageProposal: PackageProposal, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier.fillMaxSize(),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      colors = CardDefaults.cardColors(containerColor = colorScheme.primary)) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxHeight().testTag("package_content"),
            horizontalAlignment = Alignment.Start) {
              // Price of the Package
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.testTag("price"),
                    text = "$${packageProposal.price}",
                    style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp)) // Increased space between price and unit
                Text(
                    text = "/hour",
                    style = typography.bodySmall,
                    color = colorScheme.onPrimaryContainer)
              }
              // Title of the Package
              Text(
                  modifier = Modifier.testTag("title"),
                  text = packageProposal.title,
                  style = typography.titleMedium,
                  color = colorScheme.onPrimaryContainer)
              Spacer(
                  modifier =
                      Modifier.height(12.dp)) // Increased space between title and description
              // Description of the Package
              Text(
                  modifier = Modifier.testTag("description"),
                  text = packageProposal.description,
                  style = typography.bodyMedium,
                  color = colorScheme.onSurface)
              Spacer(
                  modifier =
                      Modifier.height(12.dp)) // Increased space between description and features
              // Important infos about the package
              Column(
                  modifier = Modifier.testTag("bullet_points"),
              ) {
                packageProposal.bulletPoints.forEach { feature ->
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier =
                            Modifier.size(18.dp)) // Slightly bigger icon for better visibility
                    Spacer(modifier = Modifier.width(8.dp)) // Increased space between icon and text
                    Text(
                        text = feature,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurface)
                  }
                }
              }
            }
      }
}

/**
 * A composable function that displays a date and time picker dialog to select the appointment date
 * and time for the service.
 *
 * @param request The service request to update with the selected date and time.
 * @param requestViewModel The ViewModel for the service request.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndTimePickers(request: ServiceRequest, requestViewModel: ServiceRequestViewModel) {
  var meetingDate: Timestamp?
  var showModal by remember { mutableStateOf(false) }
  // Track current step (true = date, false = time)
  var isSelectingDate by remember { mutableStateOf(true) }
  val currentTime = Calendar.getInstance()

  // States for DatePicker and TimePicker
  val datePickerState =
      rememberDatePickerState(
          initialSelectedDateMillis = currentTime.timeInMillis,
          initialDisplayedMonthMillis = currentTime.timeInMillis,
          selectableDates =
              DatePickerDefaults.AllDates, // To be modified with provider availability
      )
  val timePickerState =
      rememberTimePickerState(
          initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
          initialMinute = currentTime.get(Calendar.MINUTE),
          is24Hour = true)
  var selectedDate by remember { mutableStateOf<Long?>(null) }
  var selectedTime: TimePickerState? by remember { mutableStateOf(null) }

  IconButton(
      colors = IconButtonDefaults.iconButtonColors(contentColor = colorScheme.onPrimary),
      onClick = {
        showModal = true
        isSelectingDate = true // Start with date selection
      },
      modifier = Modifier.testTag("date_time_picker_button")) {
        Icon(Icons.Default.DateRange, contentDescription = "Select date and time")
      }

  if (showModal) {
    Dialog(onDismissRequest = { showModal = false }) {
      Surface(
          shape = RoundedCornerShape(16.dp),
          color = colorScheme.surface,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(min = 400.dp, max = 600.dp)
                        .verticalScroll(rememberScrollState()) // Enable scrolling if needed
                        .padding(16.dp)) {
                  Column(
                      verticalArrangement = Arrangement.spacedBy(16.dp),
                      modifier = Modifier.fillMaxWidth()) {
                        if (!isSelectingDate) {
                          Text(
                              text = "Select Time",
                              style = typography.titleMedium,
                              color = colorScheme.onBackground,
                              modifier = Modifier.testTag("select_time_text"))
                        }
                        if (isSelectingDate) {
                          DatePicker(
                              state = datePickerState,
                              title = {
                                Text(
                                    "Select Date",
                                    style = typography.titleMedium,
                                    color = colorScheme.onBackground)
                              },
                              modifier = Modifier.testTag("date_picker"))
                        } else {
                          TimePicker(
                              state = timePickerState, modifier = Modifier.testTag("time_picker"))
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End) {
                              TextButton(
                                  onClick = { showModal = false },
                                  modifier = Modifier.testTag("cancel_button")) {
                                    Text("Cancel")
                                  }
                              TextButton(
                                  modifier = Modifier.testTag("action_button"),
                                  onClick = {
                                    if (isSelectingDate) {
                                      // Save selected date and proceed to time selection
                                      selectedDate = datePickerState.selectedDateMillis
                                      isSelectingDate = false
                                    } else {
                                      // Save selected time and close dialog
                                      selectedTime = timePickerState
                                      meetingDate =
                                          selectedDate
                                              ?.let { dateMillis ->
                                                Calendar.getInstance()
                                                    .apply {
                                                      timeInMillis = dateMillis
                                                      set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                                                      set(Calendar.MINUTE, selectedTime!!.minute)
                                                    }
                                                    .time
                                                    .let { Date(it.time) }
                                              }
                                              ?.let { Timestamp(it) }
                                      requestViewModel.saveServiceRequest(
                                          request.copy(
                                              meetingDate = meetingDate,
                                              status = ServiceRequestStatus.SCHEDULED))
                                      showModal = false
                                    }
                                  }) {
                                    Text(if (isSelectingDate) "Next" else "OK")
                                  }
                            }
                      }
                }
          }
    }
  }
}

/**
 * A composable function that displays a dialog to edit the agreed price for the service.
 *
 * @param request The service request to update with the agreed price.
 * @param requestViewModel The ViewModel for the service request.
 */
@Composable
fun EditPriceDialog(
    request: ServiceRequest,
    requestViewModel: ServiceRequestViewModel,
) {
  // Price input field
  var price by remember { mutableStateOf(request.agreedPrice?.toString() ?: "") }
  // Dialog visibility state
  var showDialog by remember { mutableStateOf(false) }

  // Edit button for the price
  IconButton(
      onClick = { showDialog = true },
      colors = IconButtonDefaults.iconButtonColors(contentColor = colorScheme.onPrimary),
      modifier = Modifier.testTag("edit_price_button")) {
        Icon(Icons.Default.Edit, contentDescription = "Edit price")
      }
  // Display the dialog for editing the price
  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag("edit_price_dialog"),
        onDismissRequest = { showDialog = false },
        title = {
          Text(
              modifier = Modifier.testTag("edit_price_title"),
              text = "Edit Price",
              style = typography.titleMedium,
              color = colorScheme.onBackground)
        },
        text = {
          Column {
            Text(
                text = "Enter the agreed price for the service",
                style = typography.bodyMedium,
                color = colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            var containsInvalidChars by remember { mutableStateOf(false) }
            val isPriceValid = price.matches(Regex("^[0-9]+(\\.[0-9]{0,2})?$"))
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                  // Check for invalid characters
                  containsInvalidChars = !input.matches(Regex("^[0-9.]*$"))
                  val sanitizedInput =
                      when {
                        input.startsWith("0.") -> input // Allow numbers like "0.99"
                        input == "0" -> input // Allow single "0"
                        else -> input.trimStart('0').ifEmpty { "0" } // Remove leading zeros
                      }
                  // Validate the format and update priceInput if valid
                  if (sanitizedInput.matches(Regex("^[0-9]{0,10}(\\.[0-9]{0,2})?$"))) {
                    price = sanitizedInput
                  }
                },
                label = { Text("Price") },
                leadingIcon = { Text("CHF") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = (!isPriceValid && price.isNotEmpty()) || containsInvalidChars,
                modifier = Modifier.fillMaxWidth().testTag("edit_price_input"))
            // Error message for invalid format
            if (!isPriceValid && price.isNotEmpty()) {
              Text(
                  modifier = Modifier.testTag("invalid_price_error"),
                  text = "Please enter a valid number (e.g., 99 or 99.99)",
                  color = colorScheme.error,
                  fontSize = 12.sp)
            }
            // Error message for invalid characters
            if (containsInvalidChars) {
              Text(
                  text = "Invalid characters entered. Please use numbers and a decimal point only.",
                  color = colorScheme.error,
                  fontSize = 12.sp)
            }
          }
        },
        confirmButton = {
          TextButton(
              modifier = Modifier.testTag("save_button"),
              onClick = {
                val priceValue = price.toDoubleOrNull()
                priceValue?.let {
                  requestViewModel.saveServiceRequest(
                      request.copy(
                          agreedPrice = priceValue,
                      ))
                }
                showDialog = false
              }) {
                Text("Save")
              }
        },
        dismissButton = {
          TextButton(
              modifier = Modifier.testTag("cancel_button"), onClick = { showDialog = false }) {
                Text("Cancel")
              }
        })
  }
}
