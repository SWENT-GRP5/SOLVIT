package com.android.solvit.provider.ui.jobs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.android.solvit.shared.model.jobs.Job
import com.android.solvit.shared.model.jobs.JobDashboardViewModel
import com.android.solvit.shared.model.jobs.JobStatus
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.LatLng

/**
 * JobDashboardScreen displays a dashboard with three tabs: Pending, Current, and History. Each tab
 * contains a list of jobs in its respective state. The initial tab is set to "Current."
 *
 * @param navigationActions Actions for navigating back to the previous screen.
 * @param jobDashboardViewModel ViewModel instance containing job data.
 */
@Composable
fun JobDashboardScreen(
    navigationActions: NavigationActions,
    jobDashboardViewModel: JobDashboardViewModel =
        viewModel(factory = JobDashboardViewModel.Factory)
) {
  // Set initial tab to 1 (Current) so it’s the first page displayed
  var selectedTab by remember { mutableStateOf(1) }

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
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
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
            0 -> PendingJobsSection(viewModel = jobDashboardViewModel)
            1 -> CurrentJobsSection(viewModel = jobDashboardViewModel)
            2 -> HistoryJobsSection(viewModel = jobDashboardViewModel)
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
fun CurrentJobsSection(viewModel: JobDashboardViewModel) {
  val context = LocalContext.current
  val currentLocation = LatLng(40.748817, -73.985428)
  val currentJobs by viewModel.currentJobs.collectAsState()

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("CurrentJobsSection")) {
        // "Navigate to All Jobs of the Day" button
        Button(
            onClick = {
              navigateToAllSortedJobs(context, currentLocation, viewModel.getTodaySortedJobs())
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
        if (currentJobs.isEmpty()) {
          Text(
              "No current jobs available",
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              modifier = Modifier.testTag("CurrentJobsEmptyText"))
        } else {
          currentJobs.forEach { job ->
            JobItem(
                job = job,
                status = JobStatus.CURRENT,
                onNavigateToJob = {
                  job.location?.let { navigateToSingleJob(context, it.latitude, it.longitude) }
                },
                onContactCustomer = {
                  Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
                },
                onMarkAsCompleted = { viewModel.completeJob(job) }, // Move to history
                onCancelRequest = {
                  viewModel.completeJob(job, isCanceled = true)
                }, // Move to history
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
fun PendingJobsSection(viewModel: JobDashboardViewModel) {
  val context = LocalContext.current
  val pendingJobs by viewModel.pendingJobs.collectAsState()

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("PendingJobsSection")) {
        if (pendingJobs.isEmpty()) {
          Text(
              "No pending jobs",
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              modifier = Modifier.testTag("PendingJobsEmptyText"))
        } else {
          pendingJobs.forEach { job ->
            JobItem(
                job = job,
                status = JobStatus.PENDING,
                onContactCustomer = {
                  Toast.makeText(context, "Contact Not yet Implemented", Toast.LENGTH_SHORT).show()
                },
                onConfirmRequest = { viewModel.confirmJob(job) },
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
fun HistoryJobsSection(viewModel: JobDashboardViewModel) {
  val context = LocalContext.current
  val historyJobs by viewModel.historyJobs.collectAsState()

  Column(
      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("HistoryJobsSection")) {
        if (historyJobs.isEmpty()) {
          Text(
              "No job history",
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              modifier = Modifier.testTag("HistoryJobsEmptyText"))
        } else {
          historyJobs.forEach { job ->
            JobItem(
                job = job,
                status = JobStatus.HISTORY,
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
 * @param job Job data to display.
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
    job: Job,
    status: JobStatus,
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
              .testTag("JobItem_${status.name}_${job.id}"),
      elevation = 4.dp,
      backgroundColor =
          when (status) {
            JobStatus.PENDING -> Color(0xFFF3E5F5) // Light purple for pending jobs
            JobStatus.CURRENT -> Color(0xFFE3F2FD) // Light blue for current jobs
            JobStatus.HISTORY -> Color(0xFFECEFF1) // Light gray for history
          },
      shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Title and Navigate Button
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Job Title
                Text(job.title, style = MaterialTheme.typography.subtitle1, color = Color.Black)

                // Navigate Button for Current Jobs
                if (status == JobStatus.CURRENT) {
                  onNavigateToJob?.let {
                    Button(
                        onClick = it,
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color.Black,
                            ),
                        modifier = Modifier.testTag("NavigateButton_${job.id}")) {
                          Text("Navigate", color = Color.White)
                        }
                  }
                }
              }
          // Job  Description
          Text(
              job.description,
              style = MaterialTheme.typography.body2,
              color = Color.Gray,
              textAlign = TextAlign.Start)
          Spacer(modifier = Modifier.height(8.dp))

          // Scheduled Date and Time
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = "Scheduled Time", tint = Color.Gray)
            Text(
                "Scheduled: ${job.date} at ${job.time}",
                style = MaterialTheme.typography.caption,
                color = Color.Gray)
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Location
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Place, contentDescription = "Location", tint = Color.Gray)
            Text(job.locationName, style = MaterialTheme.typography.caption, color = Color.Gray)
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Buttons Row
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Chat Button
                onChat?.let {
                  IconButton(onClick = it, modifier = Modifier.testTag("ChatButton_${job.id}")) {
                    Icon(
                        Icons.Outlined.MailOutline,
                        contentDescription = "Chat with Customer",
                        tint = Color(0xFF42A5F5))
                  }
                }

                // Conditional Buttons based on the status
                when (status) {
                  JobStatus.PENDING -> {
                    // Confirm Request Button
                    onConfirmRequest?.let {
                      Button(
                          onClick = it,
                          colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF66BB6A)),
                          modifier = Modifier.testTag("ConfirmButton_${job.id}")) {
                            Text("Confirm Request", color = Color.White)
                          }
                    }
                  }
                  JobStatus.CURRENT -> {
                    // Cancel and Complete Buttons for Current Jobs
                    onCancelRequest?.let {
                      Button(
                          onClick = it,
                          colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                          modifier = Modifier.testTag("CancelButton_${job.id}")) {
                            Text("Cancel", color = Color.White, fontSize = 10.sp)
                          }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    // Mark as Complete Button
                    onMarkAsCompleted?.let {
                      Button(
                          onClick = it,
                          colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF66BB6A)),
                          modifier = Modifier.testTag("CompleteButton_${job.id}")) {
                            Text("Mark As Complete", color = Color.White, fontSize = 10.sp)
                          }
                    }
                  }
                  JobStatus.HISTORY -> {
                    // Status Indicator (Completed or Cancelled)
                    Text(
                        text = if (job.status == "COMPLETED") "Completed" else "Cancelled",
                        color = if (job.status == "COMPLETED") Color.Green else Color.Red,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.testTag("StatusText_${job.id}"))
                  }
                }

                // Call Button
                onContactCustomer?.let {
                  IconButton(onClick = it, modifier = Modifier.testTag("CallButton_${job.id}")) {
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
