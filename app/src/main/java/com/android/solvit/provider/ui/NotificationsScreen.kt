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
import androidx.compose.ui.platform.testTag
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
  LaunchedEffect(providerId) { viewModel.init(providerId) }

  val notifications by viewModel.notifications.collectAsState()

  if (notifications.isEmpty()) {
    Text(
        text = "No notifications available",
        modifier = Modifier.fillMaxSize().testTag("noNotificationsText"), // Added test tag
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall)
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("notificationsList"), // Added test tag
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(notifications) { notification ->
            NotificationItem(
                notification = notification, onClick = {}, modifier = Modifier // Added test tag
                )
          }
        }
  }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Add modifier to the function
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(onClick = onClick)
              .testTag("notificationItem_${notification.uid}"),
      colors =
          CardDefaults.cardColors(
              containerColor = if (notification.isRead) Color.LightGray else Color.White),
      elevation = CardDefaults.cardElevation(4.dp),
  ) {
    Column {
      Text(
          text = notification.title,
          modifier = Modifier.testTag("notificationTitle_${notification.uid}"),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary)
      Text(
          text = notification.message,
          modifier = Modifier.testTag("notificationMessage_${notification.uid}"),
          style = MaterialTheme.typography.bodyMedium)
      Text(
          text = "Received at: ${notification.timestamp.toDate()}",
          modifier = Modifier.testTag("notificationTimestamp_${notification.uid}"),
          style = MaterialTheme.typography.titleSmall,
          color = Color.Gray)
    }
  }
}
