package com.android.solvit.provider.ui.request

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.seeker.ui.provider.PackageCard
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function that displays the list requests feed screen.
 *
 * @param serviceRequestViewModel The service request view model
 * @param packageProposalViewModel The package proposal view model
 * @param navigationActions The navigation actions
 * @param notificationViewModel The notification view model
 * @param authViewModel The authentication view model
 * @param chatViewModel The chat view model
 * @param seekerProfileViewModel The seeker profile view model
 */
@Composable
fun RequestsFeedScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    packageProposalViewModel: PackageProposalViewModel =
        viewModel(factory = PackageProposalViewModel.Factory),
    navigationActions: NavigationActions,
    notificationViewModel: NotificationsViewModel =
        viewModel<NotificationsViewModel>(factory = NotificationsViewModel.Factory),
    authViewModel: AuthViewModel = viewModel<AuthViewModel>(factory = AuthViewModel.Factory),
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    seekerProfileViewModel: SeekerProfileViewModel =
        viewModel(factory = SeekerProfileViewModel.Factory),
) {
  val requests by serviceRequestViewModel.pendingRequests.collectAsState()
  val selectedRequest = remember { mutableStateOf<ServiceRequest?>(null) }
  var selectedService by remember { mutableStateOf("All Services") }
  val searchQuery = remember { mutableStateOf("") }
  val showDialog = remember { mutableStateOf(false) }
  val user by authViewModel.user.collectAsState()
  val providerId = user?.uid ?: "-1"
  val packages = packageProposalViewModel.proposal.collectAsState()
  val seekerState = remember { mutableStateOf<SeekerProfile?>(null) }
  val isReadyToNavigate by chatViewModel.isReadyToNavigate.collectAsState()
  val isSeekerReady = remember { mutableStateOf(false) }
  val repliedClicked = remember { mutableStateOf(false) }
  val filteredRequests = filterRequests(requests, selectedService, searchQuery.value)

  LaunchedEffect(selectedRequest.value) {
    val seeker = selectedRequest.value?.userId?.let { seekerProfileViewModel.fetchUserById(it) }
    seekerState.value = seeker
    isSeekerReady.value = seeker != null // Mark as ready when seeker is fetched
  }

  LaunchedEffect(isSeekerReady.value) {
    if (repliedClicked.value && isSeekerReady.value) {
      selectedRequest.value?.let { request ->
        chatViewModel.prepareForChat(
            false, providerId, request.userId, seekerState.value, request.uid)
      }
    }
  }

  LaunchedEffect(isReadyToNavigate) {
    if (repliedClicked.value && isReadyToNavigate) {
      navigationActions.navigateTo(Screen.CHAT)
      chatViewModel.resetIsReadyToNavigate()
      isSeekerReady.value = false
      repliedClicked.value = false
    }
  }

  Scaffold(
      topBar = {
        RequestsTopBar(
            navigationActions,
            notificationViewModel,
            providerId,
            selectedService,
            searchQuery
        ) { selectedService = it }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(colorScheme.background)
                    .testTag("ScreenContent"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              ListRequests(filteredRequests, showDialog, selectedRequest, repliedClicked)

              selectedRequest.value?.let {
                ProposePackageDialog(
                    providerId = providerId,
                    request = it,
                    packages = packages.value.filter { pckg -> pckg.providerId == providerId },
                    showDialog = showDialog,
                    requestViewModel = serviceRequestViewModel)
              }
            }
      }
}

/**
 * Composable function that displays the top bar of the requests feed screen.
 *
 * @param navigationActions The navigation actions
 * @param notificationsViewModel The notification view model
 * @param providerId The provider's ID
 * @param selectedService The selected service
 * @param searchQuery The search query
 * @param onServiceSelected The onServiceSelected action
 */
@Composable
fun RequestsTopBar(
    navigationActions: NavigationActions,
    notificationsViewModel: NotificationsViewModel,
    providerId: String,
    selectedService: String,
    searchQuery: MutableState<String>,
    onServiceSelected: (String) -> Unit
) {
  // Fetch notifications and check for unread ones
  LaunchedEffect(providerId) { notificationsViewModel.init(providerId) }
  val notifications by notificationsViewModel.notifications.collectAsState()
  val hasUnreadNotifications = notifications.any { !it.isRead }

  Box(modifier = Modifier.fillMaxWidth().testTag("servicesScreenTopSection")) {
    // Background Image
    Image(
        painter = painterResource(id = R.drawable.top_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize())
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      Row(
          modifier = Modifier.fillMaxWidth().testTag("RequestsTopBar").padding(start = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {

            Row(
                modifier = Modifier.testTag("SloganIcon"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                  text = "Solv",
                  style =
                      TextStyle(
                          fontSize = 20.sp,
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.onBackground))
              Text(
                  text = "It",
                  style =
                      TextStyle(
                          fontSize = 20.sp,
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.secondary))
            }

            IconButton(onClick = { navigationActions.navigateTo(Route.NOTIFICATIONS) }) {
              Icon(
                  imageVector = Icons.Default.Notifications,
                  tint = if (hasUnreadNotifications) Color.Red else colorScheme.onBackground,
                  contentDescription = "Notifications")
            }
          }

      // Search Bar and Service Type Filter
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            SearchBar(searchQuery, Modifier.weight(1f))
            ServiceTypeFilter(
                selectedService = selectedService,
                onServiceSelected = onServiceSelected,
            )
          }
    }
  }
}

/**
 * Composable function that displays the search bar of the requests feed screen.
 *
 * @param searchQuery The search query state
 * @param modifier The modifier
 */
@Composable
fun SearchBar(searchQuery: MutableState<String>, modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center) {
        // Floating Search Bar with rounded corners
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)) // Fully rounded
                    .background(colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically) {
              // Leading Icon
              Icons.Default.Search.let {
                Icon(
                    imageVector = it,
                    contentDescription = "Search Icon",
                    tint = colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp))
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Search Input
              BasicTextField(
                  value = searchQuery.value,
                  onValueChange = { searchQuery.value = it },
                  singleLine = true,
                  textStyle =
                      TextStyle(
                          color = colorScheme.onSurface,
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Medium),
                  decorationBox = { innerTextField ->
                    if (searchQuery.value.isEmpty()) {
                      Text(
                          "Search requests",
                          style =
                              TextStyle(
                                  color = colorScheme.onSurfaceVariant,
                                  fontSize = 16.sp,
                                  fontWeight = FontWeight.Medium))
                    }
                    innerTextField()
                  },
                  modifier = Modifier.fillMaxWidth().padding(end = 16.dp).testTag("SearchBar"))
            }
      }
}

/**
 * Composable function that displays the list of service requests.
 *
 * @param requests The list of service requests
 * @param showDialog The showDialog state
 * @param selectedRequest The selected request
 * @param repliedClicked The repliedClicked state
 */
@Composable
fun ListRequests(
    requests: List<ServiceRequest>,
    showDialog: MutableState<Boolean>,
    selectedRequest: MutableState<ServiceRequest?>,
    repliedClicked: MutableState<Boolean>
) {
  LazyColumn(
      modifier =
          Modifier.fillMaxSize().padding(horizontal = 16.dp).background(colorScheme.background),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(modifier = Modifier.size(20.dp)) }
        items(requests) { request ->
          ServiceRequestItem(request, showDialog, selectedRequest, repliedClicked)
        }
      }
}

/**
 * Composable function that displays a service request item.
 *
 * @param request The service request
 * @param showDialog The showDialog state
 * @param selectedRequest The selected request
 * @param repliedClicked The repliedClicked state
 */
@Composable
fun ServiceRequestItem(
    request: ServiceRequest,
    showDialog: MutableState<Boolean>,
    selectedRequest: MutableState<ServiceRequest?>,
    repliedClicked: MutableState<Boolean>,
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("ServiceRequest"),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      border = BorderStroke(4.dp, color = Services.getColor(request.type)),
      colors = CardDefaults.cardColors(containerColor = colorScheme.background)) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Header Image
          Box {
            val imageUrl = request.imageUrl
            if (!imageUrl.isNullOrEmpty()) {
              AsyncImage(
                  model = imageUrl,
                  placeholder = painterResource(id = R.drawable.loading),
                  error = painterResource(id = R.drawable.error),
                  contentDescription = "Service Image",
                  modifier = Modifier.fillMaxWidth().height(140.dp),
                  contentScale = ContentScale.Crop)
            } else {
              Box(
                  modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.LightGray),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = "No Image Provided",
                        style =
                            TextStyle(
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold))
                  }
            }
            Icon(
                painter = painterResource(id = Services.getIcon(request.type)),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier =
                    Modifier.padding(16.dp)
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                        .shadow(
                            ambientColor = Services.getColor(request.type),
                            spotColor = Services.getColor(request.type),
                            elevation = 8.dp,
                            shape = RoundedCornerShape(8.dp)))
          }

          // Content Section
          Column(modifier = Modifier.padding(16.dp)) {
            // Title and Type
            Text(
                text = request.title,
                style =
                    TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Services.format(request.type),
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorScheme.onSurfaceVariant))

            Spacer(modifier = Modifier.height(8.dp))

            // Deadline
            Row {
              Text(
                  text = "Deadline: ",
                  style =
                      TextStyle(
                          fontSize = 14.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = colorScheme.onSurface))
              Text(
                  text =
                      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                          .format(request.dueDate.toDate()),
                  style =
                      TextStyle(
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Normal,
                          color = colorScheme.error))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progressive Disclosure for Description
            var isExpanded by remember { mutableStateOf(false) }
            var textOverflow by remember { mutableStateOf(false) }
            Text(
                text = request.description,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = colorScheme.onSurfaceVariant),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                  if (!isExpanded) {
                    // Check overflow only when text is not expanded
                    textOverflow = textLayoutResult.hasVisualOverflow
                  }
                })
            if (textOverflow) {
              Text(
                  text = if (isExpanded) "Read Less" else "Read More",
                  modifier = Modifier.clickable { isExpanded = !isExpanded }.padding(top = 4.dp),
                  style =
                      TextStyle(
                          fontSize = 12.sp,
                          color = colorScheme.primary,
                          fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                  InteractionBar("Propose", R.drawable.share_icon) {
                    selectedRequest.value = request
                    showDialog.value = true
                  }
                  InteractionBar("Reply", R.drawable.reply_icon) {
                    selectedRequest.value = request
                    repliedClicked.value = true
                  }
                }
          }
        }
      }
}

/**
 * Composable function that displays an interaction bar.
 *
 * @param text The text to display
 * @param icon The icon to display
 * @param onClick The onClick action
 */
@Composable
fun InteractionBar(text: String, icon: Int, onClick: () -> Unit = {}) {
  Row(
      modifier =
          Modifier.clip(RoundedCornerShape(8.dp))
              .clickable { onClick() }
              .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center) {
        // Icon
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        // Text
        Text(
            text = text,
            style =
                TextStyle(
                    color = colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium))
      }
}

/**
 * Composable function that displays a dialog to propose a provider's package to a seeker's service
 * request.
 *
 * @param providerId The provider's ID
 * @param request The service request
 * @param packages The list of package proposals
 * @param showDialog The showDialog state
 * @param requestViewModel The service request view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposePackageDialog(
    providerId: String,
    request: ServiceRequest,
    packages: List<PackageProposal>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel
) {
  val selectedPackage = remember { mutableStateOf<PackageProposal?>(null) }
  var selectedIndex by remember { mutableIntStateOf(-1) }

  if (showDialog.value) {
    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      Column(
          modifier =
              Modifier.fillMaxSize().padding(16.dp), // Add padding to avoid content touching edges
          horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally
          verticalArrangement = Arrangement.Center // Center items vertically in the Column
          ) {
            if (packages.isNotEmpty()) {
              // Horizontal scrollable list
              LazyRow(
                  modifier = Modifier.fillMaxWidth().testTag("packagesScrollableList"),
                  horizontalArrangement = Arrangement.spacedBy(20.dp), // Adjusted for spacing
                  contentPadding =
                      PaddingValues(top = 40.dp, start = 12.dp, end = 12.dp), // Increased padding
              ) {
                items(packages.size) { index ->
                  // If package is selected, we display it bigger
                  val size by
                      animateDpAsState(
                          targetValue = if (selectedIndex == index) 350.dp else 320.dp,
                          label = "PackageCardSize")
                  PackageCard(
                      packageProposal = packages[index],
                      selectedIndex = selectedIndex == index,
                      modifier =
                          Modifier.width(260.dp) // Slightly wider for better touch targets
                              .height(size)
                              .testTag("PackageCard"),
                      selectedPackage = selectedPackage,
                      onIsSelectedChange = {
                        selectedIndex = if (selectedIndex == index) -1 else index
                      })
                }
              }
              Spacer(modifier = Modifier.height(16.dp)) // Add space between LazyRow and Button
              Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.testTag("CancelButton"),
                    onClick = { showDialog.value = false }) {
                      Text(text = "Cancel")
                    }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                      selectedPackage.value?.let {
                        requestViewModel.saveServiceRequest(
                            request.copy(
                                providerId = providerId,
                                packageId = it.uid,
                                agreedPrice = it.price,
                                status = ServiceRequestStatus.ACCEPTED))
                        showDialog.value = false
                      }
                    },
                    enabled = selectedPackage.value != null) {
                      Text(text = "Propose Package")
                    }
              }
            } else {
              Card(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  elevation = CardDefaults.cardElevation(8.dp),
                  colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer)) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                          Text(
                              text =
                                  "No packages available. Please enter a price for this service:",
                              fontSize = 18.sp,
                              fontWeight = FontWeight.Bold,
                              color = colorScheme.onPrimaryContainer)
                          Spacer(modifier = Modifier.height(16.dp))
                          var priceInput by remember { mutableStateOf("") }
                          var containsInvalidChars by remember { mutableStateOf(false) }
                          val isPriceValid = priceInput.matches(Regex("^[0-9]+(\\.[0-9]{0,2})?$"))
                          TextField(
                              value = priceInput,
                              onValueChange = { input ->
                                // Check for invalid characters
                                containsInvalidChars = !input.matches(Regex("^[0-9.]*$"))
                                val sanitizedInput =
                                    when {
                                      input.startsWith("0.") -> input // Allow numbers like "0.99"
                                      input == "0" -> input // Allow single "0"
                                      else ->
                                          input.trimStart('0').ifEmpty {
                                            "0"
                                          } // Remove leading zeros
                                    }
                                // Validate the format and update priceInput if valid
                                if (sanitizedInput.matches(
                                    Regex("^[0-9]{0,10}(\\.[0-9]{0,2})?$"))) {
                                  priceInput = sanitizedInput
                                }
                              },
                              label = { Text("Price") },
                              modifier = Modifier.fillMaxWidth().testTag("PriceInput"),
                              keyboardOptions =
                                  KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                              singleLine = true,
                              isError =
                                  (!isPriceValid && priceInput.isNotEmpty()) ||
                                      containsInvalidChars)
                          // Error message for invalid format
                          if (!isPriceValid && priceInput.isNotEmpty()) {
                            Text(
                                text = "Please enter a valid number (e.g., 99 or 99.99)",
                                color = colorScheme.error,
                                fontSize = 12.sp)
                          }
                          // Error message for invalid characters
                          if (containsInvalidChars) {
                            Text(
                                text =
                                    "Invalid characters entered. Please use numbers and a decimal point only.",
                                color = colorScheme.error,
                                fontSize = 12.sp)
                          }
                          Spacer(modifier = Modifier.width(16.dp))
                          Row(
                              horizontalArrangement = Arrangement.Center,
                              modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    modifier = Modifier.testTag("CancelButton"),
                                    onClick = { showDialog.value = false }) {
                                      Text(text = "Cancel")
                                    }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    modifier = Modifier.testTag("ConfirmButton"),
                                    onClick = {
                                      requestViewModel.saveServiceRequest(
                                          request.copy(
                                              providerId = providerId,
                                              agreedPrice = priceInput.toDouble(),
                                              status = ServiceRequestStatus.ACCEPTED))
                                      showDialog.value = false
                                    },
                                    enabled = isPriceValid && priceInput.isNotEmpty()) {
                                      Text(text = "Confirm")
                                    }
                              }
                        }
                  }
            }
          }
    }
  }
}

/**
 * Composable function that displays a dropdown menu to filter service types.
 *
 * @param selectedService The selected service
 * @param onServiceSelected The onServiceSelected action
 * @param modifier The modifier
 */
@Composable
fun ServiceTypeFilter(
    selectedService: String,
    onServiceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier.wrapContentWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
    Button(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
        modifier = Modifier.height(56.dp).testTag("ServiceFilter")) {
          Text(text = selectedService, color = colorScheme.onPrimary)
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier =
            Modifier.background(
                color = colorScheme.surface,
                shape = RoundedCornerShape(12.dp) // Add rounded corners
                )) {
          DropdownMenuItem(
              text = { Text("All Services", style = TextStyle(fontWeight = FontWeight.Medium)) },
              modifier = Modifier.testTag("all_services"),
              onClick = {
                onServiceSelected("All Services")
                expanded = false
              })
          // Add services dynamically here
          Services.entries.forEach { service ->
            DropdownMenuItem(
                text = {
                  Text(Services.format(service), style = TextStyle(fontWeight = FontWeight.Medium))
                },
                modifier = Modifier.testTag(Services.format(service)),
                onClick = {
                  onServiceSelected(Services.format(service))
                  expanded = false
                })
          }
        }
  }
}

/**
 * Function that filters the list of service requests based on the selected service and search
 * query.
 *
 * @param requests The list of service requests
 * @param selectedService The selected service
 * @param searchQuery The search query
 * @return The filtered list of service requests
 */
private fun filterRequests(
    requests: List<ServiceRequest>,
    selectedService: String,
    searchQuery: String
): List<ServiceRequest> {
  return requests.filter {
    val matchesService =
        selectedService == "All Services" ||
            it.type.name == selectedService.replace(" ", "_").uppercase()
    val matchesQuery =
        searchQuery.isBlank() ||
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    matchesService && matchesQuery
  }
}
