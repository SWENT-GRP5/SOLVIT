package com.android.solvit.provider.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.solvit.shared.model.Notification
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationsViewModel,
    providerId: String,
    navigationActions: NavigationActions
) {
  LaunchedEffect(providerId) { viewModel.init(providerId) }

  val notifications by viewModel.notifications.collectAsState()
  var showDialog by remember { mutableStateOf(false) }
  val selectedServiceRequest = remember { mutableStateOf<ServiceRequest?>(null) }
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth().testTag("notifications_title"),
                  horizontalArrangement = Arrangement.Start) {
                    Text(
                        "Notifications",
                        textAlign = TextAlign.Center,
                        style = Typography.titleLarge)
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            })
      }) { paddingValues ->
        // Content inside the Scaffold
        Column(modifier = Modifier.padding(paddingValues)) {
          if (notifications.isEmpty()) {
            Text(
                text = "No notifications available",
                modifier = Modifier.fillMaxSize().testTag("noNotificationsText"), // Added test tag
                style = Typography.bodySmall.copy(textAlign = TextAlign.Center))
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("notificationsList"), // Added test tag
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                          showDialog = true
                          selectedServiceRequest.value = notification.serviceRequest
                          viewModel.markNotificationAsRead(notification)
                        },
                        modifier = Modifier)
                  }
                }
          }

          // Show dialog when necessary
          if (showDialog) {
            Dialog(
                onDismiss = { showDialog = false }, serviceRequest = selectedServiceRequest.value!!)
          }
        }
      }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(onClick = onClick)
              .testTag("notificationItem_${notification.uid}"),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (notification.isRead) colorScheme.surfaceVariant else colorScheme.background),
      elevation = CardDefaults.cardElevation(4.dp),
  ) {
    Column {
      Text(
          text = notification.title,
          modifier = Modifier.testTag("notificationTitle_${notification.uid}"),
          style =
              Typography.titleMedium.copy(
                  color = colorScheme.primary, textAlign = TextAlign.Center))
      Text(
          text = notification.message,
          modifier = Modifier.testTag("notificationMessage_${notification.uid}"),
          style = Typography.bodyMedium)
      Text(
          text = "Received on: ${notification.timestamp.toDate().formatAsDate()}",
          modifier = Modifier.testTag("notificationTimestamp_${notification.uid}"),
          style = Typography.titleSmall.copy(color = colorScheme.onBackground))
    }
  }
}

fun Date.formatAsDate(): String {
  val sdf = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
  return sdf.format(this)
}

@Composable
fun Dialog(onDismiss: () -> Unit, serviceRequest: ServiceRequest) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(32.dp), shadowElevation = 8.dp) {
      Column(
          modifier = Modifier.padding(16.dp).testTag("serviceRequestDialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {

            // Title
            Text(
                text = "Title : ${serviceRequest.title}",
                style = Typography.titleLarge.copy(color = colorScheme.primary),
                modifier = Modifier.testTag("dialogTitle"))
            // Description
            Text(
                text = "Description: ${serviceRequest.description}",
                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("dialogDescription"))
            // Location
            serviceRequest.location?.name?.let { locationName ->
              Row {
                Text(
                    text = "Location: ",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = locationName,
                    style = Typography.bodyMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.testTag("dialogLocation") // Set color for location name
                    )
              }
            }
            // Due Date
            serviceRequest.dueDate.toDate().let { date ->
              val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
              Row {
                Text(
                    text = "Due Date: ",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = " $formattedDate",
                    style = Typography.bodyMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.testTag("dialogDueDate"))
              }
            }
            // Status
            Row {
              Text(
                  text = "Status: ",
                  style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
              Text(
                  text = ServiceRequestStatus.format(serviceRequest.status),
                  style = Typography.bodyMedium,
                  color =
                      ServiceRequestStatus.getStatusColor(serviceRequest.status), // Colored status
                  modifier = Modifier.testTag("dialogStatus"))
            }
          }
    }
  }
}
