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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
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
    authViewModel: AuthViewModel,
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
      navigationActions.navigateTo(Screen.CHAT)
      chatViewModel.resetIsReadyToNavigate()
    }
  }

  val user by authViewModel.user.collectAsState()

  val request by requestViewModel.selectedRequest.collectAsState()
  if (request == null) {
    navigationActions.goBack()
    return
  }
  val providerId = request!!.providerId
  val provider =
      if (providerId.isNullOrEmpty()) null
      else
          providerViewModel.providersList.collectAsState().value.firstOrNull {
            it.uid == request!!.providerId
          }
  val packageId = request!!.packageId
  val packageProposal =
      if (packageId.isNullOrEmpty()) null
      else
          packageViewModel.proposal.collectAsState().value.firstOrNull {
            it.uid == request!!.packageId
          }

  val acceptedOrScheduled =
      request!!.status == ServiceRequestStatus.ACCEPTED ||
          request!!.status == ServiceRequestStatus.SCHEDULED
  // Scaffold provides the basic structure for the screen with a top bar and content
  Scaffold(
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
              // Centered title within the AppBar
              Box(
                  modifier = Modifier.fillMaxWidth().testTag("booking_title"),
              ) {
                Text("Your booking", color = colorScheme.onBackground, fontWeight = FontWeight.Bold)
              }
            },
            navigationIcon = {
              // Navigation icon to go back to the previous screen (currently unhandled)
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
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
              // Problem description section
              Text(
                  text = "Problem Description", // Title for the problem description
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 8.dp).testTag("problem_description_label"))

              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                  border = BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.12f)),
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

              if (packageProposal != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your chosen Package",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp).testTag("package_label"))
                PackageCard(packageProposal)
              }

              // A row containing two parallel sections: Profile/Rating and Price/Appointment
              Row(
                  modifier =
                      Modifier.fillMaxWidth() // Occupies full width of the screen
                          .height(220.dp) // Fixed height for uniformity
                          .padding(
                              vertical = 16.dp), // Padding between the row and other components
                  horizontalArrangement = Arrangement.SpaceBetween // Space between the boxes
                  ) {
                    // Left box: Profile and rating section
                    if (provider != null) {
                      ProviderCard(provider, providerViewModel, navigationActions)
                    } else {
                      Box(
                          modifier =
                              Modifier.weight(1f)
                                  .fillMaxHeight()
                                  .background(colorScheme.secondary, RoundedCornerShape(16.dp))
                                  .padding(16.dp)
                                  .testTag("profile_box")
                                  .clickable(
                                      onClick = {
                                        providerViewModel.selectService(request!!.type)
                                        navigationActions.navigateTo(Route.PROVIDERS)
                                      })) {
                            Column(
                                horizontalAlignment =
                                    Alignment
                                        .CenterHorizontally, // Center the text inside the column
                                modifier =
                                    Modifier
                                        .fillMaxWidth() // Ensure the column takes up the full width
                                ) {
                                  // Placeholder text for the missing provider information
                                  Text(
                                      text =
                                          "No provider is assigned to this request. \n " +
                                              "CLICK to select a provider or wait for one to contact you.",
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
                              horizontalAlignment =
                                  Alignment.CenterHorizontally, // Center the text inside the column
                              modifier =
                                  Modifier
                                      .fillMaxWidth() // Ensure the column takes up the full width
                              ) {
                                // Price agreed upon
                                Text(
                                    text = "Price agreed on:",
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.secondaryContainer)
                                Spacer(modifier = Modifier.height(8.dp))
                                val price = request!!.agreedPrice
                                Text(
                                    text = if (price != null) "$price €" else "Not set",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = colorScheme.onPrimary // White text for contrast
                                    )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            16.dp)) // Space between price and appointment text

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
                                            fontSize = 20.sp,
                                            color = colorScheme.onPrimary)
                                        Text(
                                            text = time,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colorScheme.onPrimary)
                                      }
                                  if (acceptedOrScheduled) {
                                    DateAndTimePickers(request!!, requestViewModel)
                                  }
                                }
                              }
                        }
                  }

              // Address and map section
              Text(
                  text = "Address", // Address label
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).testTag("address_label"))

              val address = request!!.location
              // Google Map showing the service location
              val mapPosition = rememberCameraPositionState {
                position =
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

              if (request!!.status == ServiceRequestStatus.PENDING) {
                if (provider != null) {
                  EditAndChatButton(
                      currentUserId = user?.uid ?: "",
                      navigationActions = navigationActions,
                      chatViewModel = chatViewModel,
                      provider = provider)
                } else {
                  EditButton(navigationActions)
                }
              }

              if (request!!.status == ServiceRequestStatus.ACCEPTED) {
                // TODO
              }
              if (request!!.status == ServiceRequestStatus.COMPLETED) {
                ReviewButton(navigationActions)
              }
            }
      }
}

@Composable
fun ProviderCard(
    provider: Provider,
    providerViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  Card(
      modifier =
          Modifier.width(141.dp).height(172.dp).testTag("provider_card").clickable {
            providerViewModel.selectProvider(provider)
            navigationActions.navigateTo(Route.PROVIDER_PROFILE)
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

@Composable
fun EditButton(
    navigationActions: NavigationActions,
) {
  Box(
      modifier = Modifier.padding(horizontal = 8.dp).testTag("edit_button"),
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

@Composable
fun EditAndChatButton(
    navigationActions: NavigationActions,
    currentUserId: String,
    chatViewModel: ChatViewModel,
    provider: Provider
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("edit_discuss_button"),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    EditButton(navigationActions)
    Box(
        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).testTag("chat_button"),
        contentAlignment = Alignment.Center) {
          Button(
              onClick = { chatViewModel.prepareForChat(currentUserId, provider.uid, provider) },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
              shape = RoundedCornerShape(8.dp)) {
                Text(text = "Discuss", style = typography.labelLarge)
              }
        }
  }
}

@Composable
fun ReviewButton(navigationActions: NavigationActions) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("review_button"),
      contentAlignment = Alignment.Center) {
        Button(
            onClick = { navigationActions.navigateTo(Screen.REVIEW_SCREEN) },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary),
            shape = RoundedCornerShape(8.dp)) {
              Text(text = "Leave a review", style = typography.labelLarge)
            }
      }
}

@Composable
fun PackageCard(packageProposal: PackageProposal) {
  Card(
      modifier = Modifier.fillMaxSize(),
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
          selectableDates = DatePickerDefaults.AllDates // To be modified with provider availability
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
