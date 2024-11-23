package com.android.solvit.provider.ui.request

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestStatus.Companion.format
import com.android.solvit.shared.model.request.ServiceRequestStatus.Companion.getStatusColor
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.utils.isInternetAvailable
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RequestsDashboardScreen(
    navigationActions: NavigationActions,
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory)
) {
  var selectedTab by remember { mutableIntStateOf(2) }
  val statusTabs = ServiceRequestStatus.entries.toTypedArray()

  Scaffold(
      topBar = {
        RequestsTopBar(title = "Job Dashboard", onBackClicked = { navigationActions.goBack() })
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
                  selectedTab = selectedTab, serviceRequestViewModel = serviceRequestViewModel)
            }
      })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsTopBar(title: String, onBackClicked: () -> Unit) {
  TopAppBar(
      title = {
        Text(
            text = title,
            style = typography.titleLarge,
            color = colorScheme.onBackground,
            modifier = Modifier.testTag("JobDashboardTitle"))
      },
      navigationIcon = {
        IconButton(onClick = onBackClicked, modifier = Modifier.testTag("JobDashboardBackButton")) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
        }
      },
      colors =
          TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = colorScheme.background,
              navigationIconContentColor = colorScheme.onBackground,
              titleContentColor = colorScheme.onBackground))
}

@Composable
fun StatusTabs(selectedTab: Int, tabs: Array<ServiceRequestStatus>, onTabSelected: (Int) -> Unit) {
  ScrollableTabRow(
      selectedTabIndex = selectedTab,
      modifier = Modifier.fillMaxWidth().testTag("statusTabRow"),
      containerColor = colorScheme.background,
      contentColor = colorScheme.primary) {
        tabs.forEachIndexed { index, status ->
          Tab(
              selected = selectedTab == index,
              onClick = { onTabSelected(index) },
              text = {
                Text(
                    text = ServiceRequestStatus.format(status),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(status))
              })
        }
      }
}

@Composable
fun JobSectionContent(selectedTab: Int, serviceRequestViewModel: ServiceRequestViewModel) {
  when (selectedTab) {
    0 -> PendingJobsSection(serviceRequestViewModel)
    1 -> AcceptedJobSection(serviceRequestViewModel)
    2 -> ScheduledJobsSection(serviceRequestViewModel)
    3 -> CompletedJobsSection(serviceRequestViewModel)
    4 -> CanceledJobsSection(serviceRequestViewModel)
    5 -> ArchivedJobsSection(serviceRequestViewModel)
  }
}

@Composable
fun JobListSection(
    title: String,
    requests: List<ServiceRequest>,
    emptyMessage: String,
    onNavigateToJob: ((ServiceRequest) -> Unit)? = null,
    onContactCustomer: ((ServiceRequest) -> Unit)? = null,
    onMarkAsCompleted: ((ServiceRequest) -> Unit)? = null,
    onConfirmRequest: ((ServiceRequest) -> Unit)? = null,
    onCancelRequest: ((ServiceRequest) -> Unit)? = null,
    onArchiveRequest: ((ServiceRequest) -> Unit)? = null,
    onChat: ((ServiceRequest) -> Unit)? = null
) {
  val filteredRequests = requests.filter { it.providerId == Firebase.auth.currentUser?.uid }
  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("${title}Section"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement =
          if (filteredRequests.isEmpty()) Arrangement.Center else Arrangement.Top) {
        item {
          if (filteredRequests.isEmpty()) {
            Text(
                text = emptyMessage,
                style = typography.titleLarge,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("${title}EmptyText"))
          } else {
            filteredRequests.forEach { request ->
              JobItem(
                  request = request,
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

@Composable
fun PendingJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val pendingRequests by viewModel.pendingRequests.collectAsState()

  JobListSection(
      title = "Pending",
      requests = pendingRequests,
      emptyMessage = "No pending jobs",
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
      onConfirmRequest = { request -> viewModel.confirmRequest(request) },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() })
}

@Composable
fun AcceptedJobSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val acceptedRequests by viewModel.acceptedRequests.collectAsState()

  JobListSection(
      title = "Accepted",
      requests = acceptedRequests,
      emptyMessage = "No accepted jobs",
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() })
}

@Composable
fun ScheduledJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val currentLocation = LatLng(40.748817, -73.985428)
  val scheduledRequests by viewModel.scheduledRequests.collectAsState()

  Column(Modifier.fillMaxSize().padding(16.dp).testTag("ScheduledJobsSection")) {
    // "Navigate to All Jobs of the Day" button
    Button(
        onClick = {
          navigateToAllSortedJobs(context, currentLocation, viewModel.getTodayScheduledRequests())
        },
        modifier =
            Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("NavigateAllJobsButton"),
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = "NavigateIcon",
                tint = colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Navigate to All Jobs of the Day", color = colorScheme.onPrimary)
          }
        }

    Spacer(modifier = Modifier.height(8.dp))

    JobListSection(
        title = "Scheduled",
        requests = scheduledRequests,
        emptyMessage = "No scheduled jobs",
        onNavigateToJob = { request ->
          request.location?.let { navigateToSingleJob(context, it.latitude, it.longitude) }
        },
        onContactCustomer = {
          Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
        },
        onMarkAsCompleted = { request -> viewModel.completeRequest(request) },
        onCancelRequest = { request -> viewModel.cancelRequest(request) },
        onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() })
  }
}

@Composable
fun CompletedJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val completedRequests by viewModel.completedRequests.collectAsState()

  JobListSection(
      title = "Completed",
      requests = completedRequests,
      emptyMessage = "No completed jobs",
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() },
      onArchiveRequest = { request -> viewModel.archiveRequest(request) })
}

@Composable
fun CanceledJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val canceledRequests by viewModel.cancelledRequests.collectAsState()

  JobListSection(
      title = "Canceled",
      requests = canceledRequests,
      emptyMessage = "No canceled jobs",
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
  )
}

@Composable
fun ArchivedJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val archivedRequests by viewModel.archivedRequests.collectAsState()

  JobListSection(
      title = "Archived",
      requests = archivedRequests,
      emptyMessage = "No archived jobs",
      onChat = { Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show() },
      onContactCustomer = {
        Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
      },
  )
}

/**
 * JobItem represents a job item in a list. The display changes based on job status: Pending,
 * Current, or History.
 *
 * @param request ServiceRequest object containing job details.
 * @param onNavigateToJob Optional callback to navigate to the job's location.
 * @param onContactCustomer Optional callback to contact the customer.
 * @param onMarkAsCompleted Optional callback to mark the job as completed.
 * @param onConfirmRequest Optional callback to confirm a job request (for pending jobs).
 * @param onCancelRequest Optional callback to cancel the job.
 * @param onChat Optional callback to initiate a chat with the customer.
 */
@Composable
fun JobItem(
    request: ServiceRequest,
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
          // Title and Navigate Button
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Job Title
                Text(
                    request.title, style = typography.titleMedium, color = colorScheme.onBackground)

                // Navigate Button for Scheduled Jobs
                if (status == ServiceRequestStatus.SCHEDULED) {
                  onNavigateToJob?.let {
                    Button(
                        onClick = it,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        modifier = Modifier.testTag("NavigateButton_${request.uid}")) {
                          Text("Navigate", color = colorScheme.onPrimary)
                        }
                  }
                }
              }
          // Job  Description
          Text(
              request.description,
              style = typography.bodyMedium,
              color = colorScheme.onSurface,
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
                "Scheduled: $date at $time",
                style = typography.bodySmall,
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
              Text(it.name, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
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
                            Text("Cancel", color = colorScheme.error, fontSize = 10.sp)
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
                        style = typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.CANCELED -> {
                    Text(
                        text = format(ServiceRequestStatus.CANCELED),
                        color = getStatusColor(ServiceRequestStatus.CANCELED),
                        style = typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.ACCEPTED -> {
                    // Status Indicator (Accepted)
                    Text(
                        text = format(ServiceRequestStatus.ACCEPTED),
                        color = getStatusColor(ServiceRequestStatus.ACCEPTED),
                        style = typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.COMPLETED -> {
                    // Status Indicator (Completed)
                    Text(
                        text = format(ServiceRequestStatus.COMPLETED),
                        color = getStatusColor(ServiceRequestStatus.COMPLETED),
                        style = typography.bodySmall,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                    // Archive Button
                    onArchiveRequest?.let {
                      Button(
                          onClick = it,
                          colors =
                              ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                          modifier = Modifier.testTag("ArchiveButton_${request.uid}")) {
                            Text("Archive", color = colorScheme.onSecondary)
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
