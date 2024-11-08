package com.android.solvit.provider.ui.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.shared.model.jobs.Job
import com.android.solvit.shared.model.jobs.JobDashboardViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.maps.model.LatLng

@Composable
fun JobDashboardScreen(
    navigationActions: NavigationActions,
    jobDashboardViewModel: JobDashboardViewModel =
        viewModel(factory = JobDashboardViewModel.Factory)
) {
  JobDashboardContent(viewModel = jobDashboardViewModel)
}

@Composable
fun JobDashboardContent(
    viewModel: JobDashboardViewModel,
    currentLocation: LatLng = LatLng(40.748817, -73.985428)
) {
  val context = LocalContext.current
  val todaySortedJobs by viewModel.currentJobs.collectAsState()

  Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
    Text(
        "Current Jobs",
        style = MaterialTheme.typography.h5,
        modifier = Modifier.padding(bottom = 8.dp))

    // Navigate to All Jobs Button
    Button(
        onClick = { navigateToAllSortedJobs(context, currentLocation, todaySortedJobs) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF42A5F5))) {
          Text("Optimize and Navigate All Jobs for Today", color = Color.White)
        }

    Spacer(modifier = Modifier.height(16.dp))

    if (todaySortedJobs.isEmpty()) {
      Text("No current jobs available", style = MaterialTheme.typography.body2, color = Color.Gray)
    } else {
      todaySortedJobs.forEach { job ->
        CurrentJobItem(
            job = job,
            onNavigateToJob = {
              navigateToSingleJob(context, job.location!!.latitude, job.location.longitude)
            },
            onContactCustomer = {
              // Contact customer logic here
            },
            onMarkAsCompleted = {
              // Mark as completed logic here
            })
      }
    }
  }
}

@Composable
fun CurrentJobItem(
    job: Job,
    onNavigateToJob: () -> Unit,
    onContactCustomer: () -> Unit,
    onMarkAsCompleted: () -> Unit
) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      elevation = 4.dp,
      backgroundColor = Color(0xFFE3F2FD) // Light blue background for active jobs
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Job Title and Description
          Text(job.title, style = MaterialTheme.typography.subtitle1, color = Color.Black)
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

          // Location and Navigate Button
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Place, contentDescription = "Location", tint = Color.Gray)
            Text(job.locationName, style = MaterialTheme.typography.caption, color = Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onNavigateToJob) { Text("Navigate") }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Contact and Complete Buttons
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onContactCustomer) {
                  Icon(
                      Icons.Outlined.Phone,
                      contentDescription = "Contact Customer",
                      tint = Color(0xFF42A5F5))
                }
                Button(
                    onClick = onMarkAsCompleted,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF66BB6A))) {
                      Text("Mark As Complete", color = Color.White)
                    }
              }
        }
      }
}
