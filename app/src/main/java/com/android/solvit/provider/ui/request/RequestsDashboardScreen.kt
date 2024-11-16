package com.android.solvit.provider.ui.request

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * JobDashboardScreen displays a dashboard with three tabs: Pending, Current, and History. Each tab
 * contains a list of jobs in its respective state. The initial tab is set to "Current."
 *
 * @param navigationActions Actions for navigating back to the previous screen.
 * @param serviceRequestViewModel ViewModel instance containing the list of jobs.
 */
@Composable
fun RequestsDashboardScreen(
    navigationActions: NavigationActions,
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory)
) {
  // Set initial tab to 1 (Current) so it’s the first page displayed
  var selectedTab by remember { mutableIntStateOf(1) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Job Dashboard",
                  style = MaterialTheme.typography.h6,
                  color = Color.Black,
                  modifier = Modifier.testTag("JobDashboardTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("JobDashboardBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back")
                  }
            },
            backgroundColor = Color.White, // Set background color to white or any preferred color
            elevation = 0.dp // Remove shadow for a flat look, adjust as needed
            )
      },
      content = { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          // Tabs for Pending, Current, and History
          TabRow(selectedTabIndex = selectedTab, backgroundColor = Color.Transparent) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                  Text("Pending", color = if (selectedTab == 0) Color.Black else Color.Gray)
                },
                modifier = Modifier.testTag("Tab_Pending"))
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                  Text("Current", color = if (selectedTab == 1) Color.Black else Color.Gray)
                },
                modifier = Modifier.testTag("Tab_Current"))
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                  Text("History", color = if (selectedTab == 2) Color.Black else Color.Gray)
                },
                modifier = Modifier.testTag("Tab_History"))
          }

          // Show content based on selected tab
          when (selectedTab) {
            0 -> PendingJobsSection(viewModel = serviceRequestViewModel)
            1 -> CurrentJobsSection(viewModel = serviceRequestViewModel)
            2 -> HistoryJobsSection(viewModel = serviceRequestViewModel)
          }
        }
      })
}

/**
 * CurrentJobsSection displays the list of "Current" jobs, allowing navigation and state
 * transitions.
 *
 * @param viewModel ViewModel instance containing the list of current jobs.
 */
@Composable
fun CurrentJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val currentLocation = LatLng(40.748817, -73.985428)
  val scheduledRequests by viewModel.scheduledRequests.collectAsState()

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("CurrentJobsSection")) {
        // "Navigate to All Jobs of the Day" button
        Button(
            onClick = {
              navigateToAllSortedJobs(
                  context, currentLocation, viewModel.getTodayScheduledRequests())
            },
            modifier =
                Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("NavigateAllJobsButton"),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF42A5F5))) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigate to All Jobs of the Day", color = Color.Black)
              }
            }

        Spacer(modifier = Modifier.height(8.dp))

        // Job list or message if no current jobs
        if (scheduledRequests.isEmpty()) {
          Text(
              "No current jobs available",
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              modifier = Modifier.testTag("CurrentJobsEmptyText"))
        } else {
          scheduledRequests.forEach { request ->
            JobItem(
                request = request,
                status = ServiceRequestStatus.SCHEDULED,
                onNavigateToJob = {
                  request.location?.let { navigateToSingleJob(context, it.latitude, it.longitude) }
                },
                onContactCustomer = {
                  Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
                },
                onMarkAsCompleted = { viewModel.completeRequest(request) }, // Move to history
                onCancelRequest = { viewModel.completeRequest(request) }, // Move to history
                onChat = {
                  Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show()
                })
          }
        }
      }
}

/**
 * PendingJobsSection displays the list of "Pending" jobs. Allows confirming job requests to move
 * them to the "Current" tab.
 *
 * @param viewModel ViewModel instance containing the list of pending jobs.
 */
@Composable
fun PendingJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val pendingRequests by viewModel.pendingRequests.collectAsState()

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("PendingJobsSection")) {
        if (pendingRequests.isEmpty()) {
          Text(
              "No pending jobs",
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              modifier = Modifier.testTag("PendingJobsEmptyText"))
        } else {
          pendingRequests.forEach { request ->
            JobItem(
                request = request,
                status = ServiceRequestStatus.PENDING,
                onContactCustomer = {
                  Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
                },
                onConfirmRequest = { viewModel.confirmRequest(request) },
                onChat = {
                  Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show()
                })
          }
        }
      }
}

/**
 * HistoryJobsSection displays the list of completed or canceled jobs.
 *
 * @param viewModel ViewModel instance containing the list of historical jobs.
 */
@Composable
fun HistoryJobsSection(viewModel: ServiceRequestViewModel) {
  val context = LocalContext.current
  val archivedRequests by viewModel.archivedRequests.collectAsState()

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("HistoryJobsSection")) {
        if (archivedRequests.isEmpty()) {
          Text(
              "No job history",
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              modifier = Modifier.testTag("HistoryJobsEmptyText"))
        } else {
          archivedRequests.forEach { request ->
            JobItem(
                request = request,
                status = ServiceRequestStatus.ARCHIVED,
                onContactCustomer = {
                  Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
                },
                onChat = {
                  Toast.makeText(context, "Chat Not yet Implemented", Toast.LENGTH_SHORT).show()
                })
          }
        }
      }
}

/**
 * JobItem represents a job item in a list. The display changes based on job status: Pending,
 * Current, or History.
 *
 * @param request ServiceRequest object containing job details.
 * @param status JobStatus indicating the current state of the job.
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
    status: ServiceRequestStatus,
    onNavigateToJob: (() -> Unit)? = null,
    onContactCustomer: (() -> Unit)? = null,
    onMarkAsCompleted: (() -> Unit)? = null,
    onConfirmRequest: (() -> Unit)? = null,
    onCancelRequest: (() -> Unit)? = null,
    onChat: (() -> Unit)? = null
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag("JobItem_${status.name}_${request.uid}"),
      elevation = 4.dp,
      backgroundColor =
          when (status) {
            ServiceRequestStatus.PENDING -> Color(0xFFF3E5F5) // Light purple for pending jobs
            ServiceRequestStatus.ACCEPTED -> Color(0xFFE8F5E9) // Light green for accepted jobs
            ServiceRequestStatus.SCHEDULED -> Color(0xFFE3F2FD) // Light blue for current jobs
            ServiceRequestStatus.COMPLETED -> Color(0xFFE0F2F1) // Light teal for completed jobs
            ServiceRequestStatus.ARCHIVED -> Color(0xFFECEFF1) // Light gray for history
          },
      shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Title and Navigate Button
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Job Title
                Text(request.title, style = MaterialTheme.typography.subtitle1, color = Color.Black)

                // Navigate Button for Current Jobs
                if (status == ServiceRequestStatus.SCHEDULED) {
                  onNavigateToJob?.let {
                    Button(
                        onClick = it,
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color.Black,
                            ),
                        modifier = Modifier.testTag("NavigateButton_${request.uid}")) {
                          Text("Navigate", color = Color.White)
                        }
                  }
                }
              }
          // Job  Description
          Text(
              request.description,
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              textAlign = TextAlign.Start)
          Spacer(modifier = Modifier.height(8.dp))

          val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
          val date = dateFormat.format(request.meetingDate?.toDate() ?: request.dueDate.toDate())
          val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
          val time = timeFormat.format(request.meetingDate?.toDate() ?: request.dueDate.toDate())
          // Scheduled Date and Time
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = "Scheduled Time", tint = Color.Gray)
            Text(
                "Scheduled: $date at $time",
                style = MaterialTheme.typography.caption,
                color = Color.Gray)
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Location
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Place, contentDescription = "Location", tint = Color.Gray)
            request.location?.let {
              Text(it.name, style = MaterialTheme.typography.caption, color = Color.Gray)
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
                            tint = Color(0xFF42A5F5))
                      }
                }

                // Conditional Buttons based on the status
                when (status) {
                  ServiceRequestStatus.PENDING -> {
                    // Confirm Request Button
                    onConfirmRequest?.let {
                      Button(
                          onClick = it,
                          colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF66BB6A)),
                          modifier = Modifier.testTag("ConfirmButton_${request.uid}")) {
                            Text("Confirm Request", color = Color.White)
                          }
                    }
                  }
                  ServiceRequestStatus.SCHEDULED -> {
                    // Cancel and Complete Buttons for Current Jobs
                    onCancelRequest?.let {
                      Button(
                          onClick = it,
                          colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                          modifier = Modifier.testTag("CancelButton_${request.uid}")) {
                            Text("Cancel", color = Color.White, fontSize = 10.sp)
                          }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    // Mark as Complete Button
                    onMarkAsCompleted?.let {
                      Button(
                          onClick = it,
                          colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF66BB6A)),
                          modifier = Modifier.testTag("CompleteButton_${request.uid}")) {
                            Text("Mark As Complete", color = Color.White, fontSize = 10.sp)
                          }
                    }
                  }
                  ServiceRequestStatus.ARCHIVED -> {
                    // Status Indicator (Completed or Cancelled)
                    Text(
                        text =
                            if (request.status == ServiceRequestStatus.COMPLETED) "Completed"
                            else "Cancelled",
                        color =
                            if (request.status == ServiceRequestStatus.COMPLETED) Color.Green
                            else Color.Red,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.testTag("StatusText_${request.uid}"))
                  }
                  ServiceRequestStatus.ACCEPTED -> TODO()
                  ServiceRequestStatus.COMPLETED -> TODO()
                }

                // Call Button
                onContactCustomer?.let {
                  IconButton(
                      onClick = it, modifier = Modifier.testTag("CallButton_${request.uid}")) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = "Contact Customer",
                            tint = Color(0xFF42A5F5))
                      }
                }
              }
        }
      }
}
