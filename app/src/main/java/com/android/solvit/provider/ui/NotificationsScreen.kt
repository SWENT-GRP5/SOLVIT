package com.android.solvit.provider.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.model.Notification
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun NotificationScreen(
    viewModel: NotificationsViewModel,
    providerId: String,
    navigationActions: NavigationActions
) {

  // Fetch notifications for the provider when the screen is first loaded
  LaunchedEffect(providerId) {
    viewModel.init(providerId) // Pass the providerId to the ViewModel for initialization
  }

  // Collect notifications from the ViewModel
  val notifications by viewModel.notifications.collectAsState()

  // Display UI for notifications
  if (notifications.isEmpty()) {
    Text(
        text = "No notifications available",
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall)
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(notifications) { notification ->
            NotificationItem(notification = notification, onClick = {})
          }
        }
  }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit // onClick callback for marking as read
) {
  Card(
      modifier =
          Modifier.fillMaxWidth().clickable(onClick = onClick), // Handle click to mark as read
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (notification.isRead) Color.LightGray
                  else Color.White // Set the background color based on the read status
              ),
      elevation = CardDefaults.cardElevation(4.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
          text = notification.title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary)
      Text(text = notification.message, style = MaterialTheme.typography.bodyMedium)
      Text(
          text = "Received at: ${notification.timestamp.toDate()}",
          style = MaterialTheme.typography.titleSmall,
          color = Color.Gray)
    }
  }
}
