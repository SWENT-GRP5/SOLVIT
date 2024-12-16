package com.android.solvit.provider.ui.request

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestStatus.Companion.format
import com.android.solvit.shared.model.request.ServiceRequestStatus.Companion.getStatusColor
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.utils.isInternetAvailable
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Composable function that displays the Requests Dashboard screen.
 *
 * @param navigationActions Actions for navigation.
 * @param serviceRequestViewModel ViewModel for managing service requests.
 * @param authViewModel ViewModel for managing authentication.
 * @param listProviderViewModel View model for list of providers
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun RequestsDashboardScreen(
    navigationActions: NavigationActions,
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory)
) {
  val user by authViewModel.user.collectAsState()
  // Selected tab index
  var selectedTab by remember { mutableIntStateOf(2) }
  val statusTabs = ServiceRequestStatus.entries.toTypedArray()

  Scaffold(
      topBar = {
        TopAppBarInbox(
            title = "Job Dashboard",
            testTagTitle = "JobDashboardTitle",
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack,
            leftButtonAction = { navigationActions.goBack() },
            testTagLeft = "JobDashboardBackButton")
      },
      bottomBar = {
        val currentRoute = navigationActions.currentRoute()
        Log.e(
            "ProviderCalendarScreen", "Current route passed to BottomNavigationMenu: $currentRoute")
      },
      content = { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().background(colorScheme.background).padding(innerPadding)) {
              StatusTabs(
                  selectedTab = selectedTab,
                  tabs = statusTabs,
                  onTabSelected = { selectedTab = it })
              JobSectionContent(
                  selectedTab = selectedTab,
                  providerId = user?.uid ?: "-1",
                  serviceRequestViewModel = serviceRequestViewModel,
                  navigationActions = navigationActions,
                  listProviderViewModel = listProviderViewModel,
              )
            }
      })
}

/**
 * Composable function that displays the status tabs for the Requests Dashboard screen.
 *
 * @param selectedTab Index of the selected tab.
 * @param tabs Array of ServiceRequestStatus values.
 * @param onTabSelected Callback to handle tab selection.
 */
@Composable
fun StatusTabs(selectedTab: Int, tabs: Array<ServiceRequestStatus>, onTabSelected: (Int) -> Unit) {
  ScrollableTabRow(
      selectedTabIndex = selectedTab,
      modifier = Modifier.fillMaxWidth().testTag("statusTabRow"),
      containerColor = colorScheme.background,
      contentColor = colorScheme.primary) {
        // Create a tab for each status
        tabs.forEachIndexed { index, status ->
          Tab(
              modifier = Modifier.testTag("statusTab_$index"),
              selected = selectedTab == index,
              onClick = { onTabSelected(index) },
              text = {
                Text(
                    text = ServiceRequestStatus.format(status),
                    color = getStatusColor(status),
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
              })
        }
      }
}

/**
 * Composable function that displays the content of the selected tab in the Requests Dashboard
 * screen.
 *
 * @param providerId ID of the current provider.
 * @param listProviderViewModel view model to fetch infos of the provider
 * @param selectedTab Index of the selected tab.
 * @param serviceRequestViewModel ViewModel for managing service requests.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun JobSectionContent(
    selectedTab: Int,
    listProviderViewModel: ListProviderViewModel,
    providerId: String,
    serviceRequestViewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  when (selectedTab) {
    0 -> PendingJobsSection(providerId, serviceRequestViewModel, navigationActions)
    1 -> AcceptedJobSection(providerId, serviceRequestViewModel, navigationActions)
    2 ->
        ScheduledJobsSection(
            providerId, serviceRequestViewModel, navigationActions, listProviderViewModel)
    3 -> CompletedJobsSection(providerId, serviceRequestViewModel, navigationActions)
    4 -> CanceledJobsSection(providerId, serviceRequestViewModel, navigationActions)
    5 -> ArchivedJobsSection(providerId, serviceRequestViewModel, navigationActions)
  }
}

/**
 * Composable function that displays a list of jobs based on the status.
 *
 * @param title Title of the section.
 * @param providerId ID of the current provider.
 * @param requests List of ServiceRequest objects.
 * @param emptyMessage Message to display when the list is empty.
 * @param onLearnMore Optional callback to learn more about a job.
 * @param onNavigateToJob Optional callback to navigate to the job's location.
 * @param onContactCustomer Optional callback to contact the customer.
 * @param onMarkAsCompleted Optional callback to mark the job as completed.
 * @param onConfirmRequest Optional callback to confirm a job request (for pending jobs).
 * @param onCancelRequest Optional callback to cancel the job.
 * @param onArchiveRequest Optional callback to archive the job.
 * @param onChat Optional callback to initiate a chat with the customer.
 */
@Composable
fun JobListSection(
    title: String,
    providerId: String,
    requests: List<ServiceRequest>,
    emptyMessage: String,
    onLearnMore: ((ServiceRequest) -> Unit)? = null,
    onNavigateToJob: ((ServiceRequest) -> Unit)? = null,
    onContactCustomer: ((ServiceRequest) -> Unit)? = null,
    onMarkAsCompleted: ((ServiceRequest) -> Unit)? = null,
    onConfirmRequest: ((ServiceRequest) -> Unit)? = null,
    onCancelRequest: ((ServiceRequest) -> Unit)? = null,
    onArchiveRequest: ((ServiceRequest) -> Unit)? = null,
    onChat: ((ServiceRequest) -> Unit)? = null
) {
  // Filter requests based on the current user's ID
  val filteredRequests = requests.filter { it.providerId == providerId }
  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("${title}Section"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement =
          // Center the empty message if there are no requests, otherwise align to the top
          if (filteredRequests.isEmpty()) Arrangement.Center else Arrangement.Top) {
        item {
          if (filteredRequests.isEmpty()) {
            Text(
                text = emptyMessage,
                style = Typography.titleLarge.copy(color = colorScheme.onSurfaceVariant),
                modifier = Modifier.testTag("${title}EmptyText"))
          } else {
            // Display a list of job items
            filteredRequests.forEach { request ->
              JobItem(
                  request = request,
                  onLearnMore = { onLearnMore?.invoke(request) },
                  onNavigateToJob = { onNavigateToJob?.invoke(request) },
                  onContactCustomer = { onContactCustomer?.invoke(request) },
                  onMarkAsCompleted = { onMarkAsCompleted?.invoke(request) },
                  onConfirmRequest = { onConfirmRequest?.invoke(request) },
                  onCancelRequest = { onCancelRequest?.invoke(request) },
                  onArchiveRequest = { onArchiveRequest?.invoke(request) },
                  onChat = { onChat?.invoke(request) })
            }
          }
        }
      }
}

/**
 * Composable functions for displaying the pending requests on the Requests Dashboard screen.
 *
 * @param providerId ID of the current provider.
 * @param viewModel ViewModel for managing service requests.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun PendingJobsSection(
    providerId: String,
    viewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val pendingRequests by viewModel.pendingRequests.collectAsState()

  JobListSection(
      title = "Pending",
      providerId = providerId,
      requests = pendingRequests,
      emptyMessage = "No pending jobs",
      onLearnMore = {
        viewModel.selectRequest(it)
        navigationActions.navigateTo(Route.BOOKING_DETAILS)
      },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
      onConfirmRequest = { request ->
        viewModel.viewModelScope.launch { viewModel.confirmRequest(request, "test provider name") }
      },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() })
}

/**
 * Composable functions for displaying the accepted requests on the Requests Dashboard screen.
 *
 * @param providerId ID of the current provider.
 * @param viewModel ViewModel for managing service requests.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun AcceptedJobSection(
    providerId: String,
    viewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val acceptedRequests by viewModel.acceptedRequests.collectAsState()

  JobListSection(
      title = "Accepted",
      providerId = providerId,
      requests = acceptedRequests,
      emptyMessage = "No accepted jobs",
      onLearnMore = {
        viewModel.selectRequest(it)
        navigationActions.navigateTo(Route.BOOKING_DETAILS)
      },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() })
}

/**
 * Composable functions for displaying the scheduled requests on the Requests Dashboard screen.
 *
 * @param providerId ID of the current provider.
 * @param viewModel ViewModel for managing service requests.
 * @param listProviderViewModel ViewModel
 * @param navigationActions Actions for navigation.
 */
@Composable
fun ScheduledJobsSection(
    providerId: String,
    viewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions,
    listProviderViewModel: ListProviderViewModel
) {
  val context = LocalContext.current
  val scheduledRequests by viewModel.scheduledRequests.collectAsState()

  Column(Modifier.fillMaxSize().padding(16.dp)) {
    // "Navigate to All Jobs of the Day" button
    Button(
        onClick = { navigateToAllSortedJobs(context, viewModel.getTodayScheduledRequests()) },
        modifier =
            Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("NavigateAllJobsButton"),
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Place,
                contentDescription = "NavigateIcon",
                tint = colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Navigate to All Jobs of the Day",
                color = colorScheme.onPrimary,
                style = Typography.bodyLarge)
          }
        }

    Spacer(modifier = Modifier.height(8.dp))

    JobListSection(
        title = "Scheduled",
        providerId = providerId,
        requests = scheduledRequests,
        emptyMessage = "No scheduled jobs",
        onLearnMore = {
          viewModel.selectRequest(it)
          navigationActions.navigateTo(Route.BOOKING_DETAILS)
        },
        onNavigateToJob = { request ->
          request.location?.let { navigateToSingleJob(context, it.latitude, it.longitude) }
        },
        onContactCustomer = {
          Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
        },
        onMarkAsCompleted = { request ->
          CoroutineScope(Dispatchers.Main).launch {
            viewModel.completeRequest(request)
            val provider = listProviderViewModel.fetchProviderById(providerId)
            // Update nbr of jobs completed of provider
            if (provider != null) {
              listProviderViewModel.updateProvider(
                  provider.copy(nbrOfJobs = provider.nbrOfJobs + 1))
            } else {
              Log.e("JobsSection", "Failed to fetch provider")
            }
          }
        },
        onCancelRequest = { request -> viewModel.cancelRequest(request) },
        onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() })
  }
}

/**
 * Composable functions for displaying the completed requests on the Requests Dashboard screen.
 *
 * @param providerId ID of the current provider.
 * @param viewModel ViewModel for managing service requests.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun CompletedJobsSection(
    providerId: String,
    viewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val completedRequests by viewModel.completedRequests.collectAsState()

  JobListSection(
      title = "Completed",
      providerId = providerId,
      requests = completedRequests,
      emptyMessage = "No completed jobs",
      onLearnMore = {
        viewModel.selectRequest(it)
        navigationActions.navigateTo(Route.BOOKING_DETAILS)
      },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() },
      onArchiveRequest = { request -> viewModel.archiveRequest(request) })
}

/**
 * Composable functions for displaying the canceled requests on the Requests Dashboard screen.
 *
 * @param providerId ID of the current provider.
 * @param viewModel ViewModel for managing service requests.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun CanceledJobsSection(
    providerId: String,
    viewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val canceledRequests by viewModel.cancelledRequests.collectAsState()

  JobListSection(
      title = "Canceled",
      providerId = providerId,
      requests = canceledRequests,
      emptyMessage = "No canceled jobs",
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() },
      onLearnMore = {
        viewModel.selectRequest(it)
        navigationActions.navigateTo(Route.BOOKING_DETAILS)
      },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
  )
}

/**
 * Composable functions for displaying the archived requests on the Requests Dashboard screen.
 *
 * @param providerId ID of the current provider.
 * @param viewModel ViewModel for managing service requests.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun ArchivedJobsSection(
    providerId: String,
    viewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val archivedRequests by viewModel.archivedRequests.collectAsState()

  JobListSection(
      title = "Archived",
      providerId = providerId,
      requests = archivedRequests,
      emptyMessage = "No archived jobs",
      onLearnMore = {
        viewModel.selectRequest(it)
        navigationActions.navigateTo(Route.BOOKING_DETAILS)
      },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
  )
}

@Composable
fun OnLearnMoreButton(onLearnMore: (() -> Unit)?, request: ServiceRequest) {
  onLearnMore?.let {
    Button(
        onClick = it,
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
        modifier = Modifier.wrapContentWidth().testTag("LearnMoreButton_${request.uid}")) {
          Text("Learn More", color = colorScheme.onPrimary)
        }
  }
}

/**
 * JobItem represents a job item in a list. The display changes based on job status: Pending,
 * Accepted, Scheduled, Completed, Canceled, or Archived.
 *
 * @param request ServiceRequest object containing job details.
 * @param onLearnMore Optional callback to learn more about a job.
 * @param onNavigateToJob Optional callback to navigate to the job's location.
 * @param onContactCustomer Optional callback to contact the customer.
 * @param onMarkAsCompleted Optional callback to mark the job as completed.
 * @param onConfirmRequest Optional callback to confirm a job request (for pending jobs).
 * @param onCancelRequest Optional callback to cancel the job.
 * @param onArchiveRequest Optional callback to archive the job.
 * @param onChat Optional callback to initiate a chat with the customer.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun JobItem(
    request: ServiceRequest,
    onLearnMore: (() -> Unit)? = null,
    onNavigateToJob: (() -> Unit)? = null,
    onContactCustomer: (() -> Unit)? = null,
    onMarkAsCompleted: (() -> Unit)? = null,
    onConfirmRequest: (() -> Unit)? = null,
    onCancelRequest: (() -> Unit)? = null,
    onArchiveRequest: (() -> Unit)? = null,
    onChat: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val status = request.status
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag("JobItem_${status.name}_${request.uid}"),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
      shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Learn More and Navigate Button
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween, // Distribute space between children
              verticalAlignment = Alignment.CenterVertically) {
                // See More Button
                if (status == ServiceRequestStatus.SCHEDULED) {
                  OnLearnMoreButton(onLearnMore, request)
                  Spacer(modifier = Modifier.weight(1f))
                  onNavigateToJob?.let {
                    Button(
                        onClick = it,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        modifier = Modifier.testTag("NavigateButton_${request.uid}")) {
                          Text(
                              "Navigate",
                              color = colorScheme.onPrimary,
                              style = Typography.bodyLarge)
                        }
                  }
                } else {
                  Text(
                      text = request.title,
                      style = Typography.titleMedium,
                      color = colorScheme.onBackground,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f).padding(end = 8.dp))
                  OnLearnMoreButton(onLearnMore, request)
                }
              }
          if (status == ServiceRequestStatus.SCHEDULED) {
            // Job Title
            Text(
                text = request.title,
                style = Typography.titleMedium,
                color = colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
          }

          // Job  Description
          Text(
              request.description,
              style = Typography.bodyMedium,
              color = colorScheme.onSurface,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.Start)
          Spacer(modifier = Modifier.height(8.dp))

          val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
          val date = dateFormat.format(request.meetingDate?.toDate() ?: request.dueDate.toDate())
          val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
          val time = timeFormat.format(request.meetingDate?.toDate() ?: request.dueDate.toDate())
          // Scheduled Date and Time
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Scheduled Time",
                tint = colorScheme.onSurfaceVariant)
            Text(
                if (request.meetingDate == null) "Deadline: $date" else "Scheduled: $date at $time",
                style = Typography.bodySmall,
                color = colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Location
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Place,
                contentDescription = "Location",
                tint = colorScheme.onSurfaceVariant)
            request.location?.let {
              Text(
                  it.name,
                  style = Typography.bodySmall,
                  color = colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            }
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Buttons Row
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Chat Button
                onChat?.let {
                  IconButton(
                      onClick = it, modifier = Modifier.testTag("ChatButton_${request.uid}")) {
                        Icon(
                            Icons.Outlined.MailOutline,
                            contentDescription = "Chat with Customer",
                            tint = colorScheme.primary)
                      }
                }

                // Conditional Buttons based on the status
                when (status) {
                  ServiceRequestStatus.PENDING -> {
                    // Confirm Request Button
                    onConfirmRequest?.let {
                      Button(
                          onClick = {
                            if (isInternetAvailable(context)) {
                              it()
                            } else {
                              Toast.makeText(
                                      context,
                                      "Confirming requests requires internet connectivity",
                                      Toast.LENGTH_SHORT)
                                  .show()
                            }
                          },
                          colors =
                              ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                          modifier = Modifier.testTag("ConfirmButton_${request.uid}")) {
                            Text("Confirm Request", color = colorScheme.onSecondary)
                          }
                    }
                  }
                  ServiceRequestStatus.SCHEDULED -> {
                    // Cancel and Complete Buttons for Current Jobs
                    onCancelRequest?.let {
                      Button(
                          onClick = {
                            if (isInternetAvailable(context)) {
                              it()
                            } else {
                              Toast.makeText(
                                      context,
                                      "Cancelling requests requires internet connectivity",
                                      Toast.LENGTH_SHORT)
                                  .show()
                            }
                          },
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = colorScheme.errorContainer),
                          modifier = Modifier.testTag("CancelButton_${request.uid}")) {
                            Text(
                                "Cancel",
                                color = colorScheme.error,
                                style = Typography.bodySmall.copy(fontSize = 10.sp))
                          }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    // Mark as Complete Button
                    onMarkAsCompleted?.let {
                      Button(
                          onClick = {
                            if (isInternetAvailable(context)) {
                              it()
                            } else {
                              Toast.makeText(
                                      context,
                                      "Completing requests requires internet connectivity",
                                      Toast.LENGTH_SHORT)
                                  .show()
                            }
                          },
                          colors =
                              ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                          modifier = Modifier.testTag("CompleteButton_${request.uid}")) {
                            Text(
                                "Mark As Complete",
                                color = colorScheme.onSecondary,
                                fontSize = 10.sp)
                          }
                    }
                  }
                  ServiceRequestStatus.ARCHIVED -> {
                    // Status Indicator (Completed or Cancelled)
                    Text(
                        text = format(ServiceRequestStatus.ARCHIVED),
                        color = getStatusColor(ServiceRequestStatus.ARCHIVED),
                        style = Typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.CANCELED -> {
                    Text(
                        text = format(ServiceRequestStatus.CANCELED),
                        color = getStatusColor(ServiceRequestStatus.CANCELED),
                        style = Typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.ACCEPTED -> {
                    // Status Indicator (Accepted)
                    Text(
                        text = format(ServiceRequestStatus.ACCEPTED),
                        color = getStatusColor(ServiceRequestStatus.ACCEPTED),
                        style = Typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.COMPLETED -> {
                    // Status Indicator (Completed)
                    Text(
                        text = format(ServiceRequestStatus.COMPLETED),
                        color = getStatusColor(ServiceRequestStatus.COMPLETED),
                        style = Typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                    // Archive Button
                    onArchiveRequest?.let {
                      Button(
                          onClick = it,
                          colors =
                              ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                          modifier = Modifier.testTag("ArchiveButton_${request.uid}")) {
                            Text(
                                "Archive",
                                color = colorScheme.onSecondary,
                                style = Typography.bodyLarge)
                          }
                    }
                  }
                }

                // Call Button
                onContactCustomer?.let {
                  IconButton(
                      onClick = it, modifier = Modifier.testTag("CallButton_${request.uid}")) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = "Contact Customer",
                            tint = colorScheme.primary)
                      }
                }
              }
        }
      }
}
