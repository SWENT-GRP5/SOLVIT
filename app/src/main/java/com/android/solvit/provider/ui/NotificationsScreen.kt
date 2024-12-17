package com.android.solvit.provider.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.Notification
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
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

  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  var showDialog by remember { mutableStateOf(false) }
  val selectedServiceRequest = remember { mutableStateOf<ServiceRequest?>(null) }
  Scaffold(
      topBar = {
        TopAppBarInbox(
            title = "Notifications",
            testTagGeneral = "notifications_title",
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack,
            leftButtonAction = { navigationActions.goBack() },
            testTagLeft = "goBackButton")
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
          if (notifications.isEmpty()) {
            // Display the image and the text when no notifications are available
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                  // matching Figma's dimensions and typography
                  Image(
                      painter = painterResource(id = R.drawable.no_notifications_image),
                      contentDescription = "No Notifications Image",
                      modifier = Modifier.size(180.dp).testTag("noNotificationsImage"))
                  Spacer(modifier = Modifier.height(16.dp))

                  Text(
                      text = "No Notifications Yet",
                      style =
                          MaterialTheme.typography.bodyLarge.copy(
                              fontWeight = FontWeight.Bold,
                              fontSize = 20.sp,
                              lineHeight = 30.44.sp,
                              textAlign = TextAlign.Center),
                      color = Color.Black,
                      modifier =
                          Modifier.testTag("noNotificationsText").width(209.dp).height(30.dp))
                }
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("notificationsList"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  val groupedNotifications = groupNotificationsByDate(notifications).toList()

                  items(groupedNotifications) { (date, notificationsForDate) ->
                    Text(
                        text = date,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        color = colorScheme.primary,
                        modifier =
                            Modifier.padding(vertical = 8.dp).testTag("notificationTimestamp"))

                    Spacer(modifier = Modifier.height(8.dp)) // Adds space between date and cards

                    notificationsForDate.forEach { notification ->
                      NotificationItem(
                          notification = notification,
                          onClick = {
                            showDialog = true
                            selectedServiceRequest.value = notification.serviceRequest
                            viewModel.markNotificationAsRead(notification)
                          },
                          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    }
                  }
                }

            if (showDialog) {
              Notifications_Dialog(
                  onDismiss = { showDialog = false },
                  serviceRequest = selectedServiceRequest.value!!)
            }
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
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(8.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
          text = notification.title,
          modifier = Modifier.testTag("notificationTitle_${notification.uid}"),
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          color = colorScheme.primary)
      Text(
          text = notification.message,
          modifier = Modifier.testTag("notificationMessage_${notification.uid}"),
          style = MaterialTheme.typography.bodyMedium)
    }
  }
}

fun groupNotificationsByDate(notifications: List<Notification>): Map<String, List<Notification>> {
  val dateFormat = SimpleDateFormat("MMMM dd", Locale.ENGLISH)
  return notifications.groupBy { notification ->
    dateFormat.format(notification.timestamp.toDate())
  }
}

@Composable
fun Notifications_Dialog(onDismiss: () -> Unit, serviceRequest: ServiceRequest) {
  Dialog(onDismissRequest = onDismiss) {
    Box(
        modifier = Modifier.fillMaxSize().clickable { onDismiss() },
        contentAlignment = Alignment.Center) {
          Box(contentAlignment = Alignment.TopCenter) {
            // Dialog Surface
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier =
                    Modifier.width(350.dp)
                        .padding(top = 32.dp) // Ensures there's space for the icon at the top
                ) {
                  Column(
                      modifier = Modifier.padding(16.dp).testTag("serviceRequestDialog"),
                      verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Title with Icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                              imageVector = Icons.Filled.Edit,
                              contentDescription = "Title Icon",
                              tint = Color.Black,
                              modifier = Modifier.size(24.dp))
                          Spacer(modifier = Modifier.width(8.dp))
                          Text(
                              text = serviceRequest.title,
                              style = MaterialTheme.typography.bodyMedium,
                              modifier = Modifier.testTag("dialogTitle"))
                        }

                        // Description with Expand Option and Icon
                        Row(verticalAlignment = Alignment.Top) {
                          Icon(
                              imageVector = Icons.Filled.Build,
                              contentDescription = "Description Icon",
                              tint = Color.Black,
                              modifier = Modifier.size(24.dp))
                          Spacer(modifier = Modifier.width(8.dp))
                          Column {
                            var isExpanded by remember { mutableStateOf(false) }
                            val truncatedDescription =
                                serviceRequest.description.split(".").take(3).joinToString(". ")
                            Text(
                                text =
                                    if (isExpanded) serviceRequest.description
                                    else truncatedDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("dialogDescription"))
                            if (!isExpanded && serviceRequest.description.split(".").size > 3) {
                              TextButton(onClick = { isExpanded = true }) {
                                Text(text = "Read more")
                              }
                            }
                          }
                        }

                        // Image Section with Two Side-by-Side Images
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                              // Image 1
                              val imageUrl1 = serviceRequest!!.imageUrl
                              if (!imageUrl1.isNullOrEmpty()) {
                                AsyncImage(
                                    model = imageUrl1,
                                    placeholder = painterResource(id = R.drawable.loading),
                                    error = painterResource(id = R.drawable.error),
                                    contentDescription = "Service Image 1",
                                    modifier =
                                        Modifier.weight(1f)
                                            .height(100.dp)
                                            .testTag("request_image")
                                            .border(
                                                1.dp,
                                                colorScheme.onPrimary,
                                                RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop)
                              } else {
                                Box(
                                    modifier =
                                        Modifier.weight(1f)
                                            .height(100.dp)
                                            .testTag("request_image")
                                            .border(
                                                1.dp,
                                                colorScheme.onPrimary,
                                                RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.LightGray),
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

                              Spacer(modifier = Modifier.width(8.dp))

                              // Image 2 (Map)
                              Box(
                                  modifier =
                                      Modifier.weight(1f)
                                          .testTag("map_image")
                                          .height(100.dp)
                                          .border(
                                              1.dp,
                                              colorScheme.onPrimary,
                                              RoundedCornerShape(12.dp))
                                          .clip(RoundedCornerShape(12.dp)),
                                  contentAlignment = Alignment.Center) {
                                    serviceRequest.location?.let { location ->
                                      val mapPosition = rememberCameraPositionState()
                                      LaunchedEffect(location) {
                                        mapPosition.position =
                                            com.google.android.gms.maps.model.CameraPosition
                                                .fromLatLngZoom(
                                                    LatLng(location.latitude, location.longitude),
                                                    15f)
                                      }
                                      GoogleMap(
                                          cameraPositionState = mapPosition,
                                          modifier =
                                              Modifier.fillMaxSize()
                                                  .clip(RoundedCornerShape(12.dp)))
                                    }
                                  }
                            }

                        // Deadline with Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center) {
                              Icon(
                                  imageVector = Icons.Filled.DateRange,
                                  contentDescription = "Calendar Icon",
                                  tint = Color.Black,
                                  modifier = Modifier.size(24.dp))
                              Spacer(modifier = Modifier.width(8.dp))
                              serviceRequest.dueDate?.toDate()?.let { date ->
                                val formattedDate =
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                                Text(
                                    text = "Deadline: ",
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold),
                                    color = colorScheme.error)
                                Text(
                                    text = formattedDate,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold),
                                    color = Color.Black,
                                    modifier = Modifier.testTag("dialogDueDate"))
                              }
                            }
                      }
                }

            // Notification Icon - Positioned directly above the dialog
            Box(
                modifier =
                    Modifier.size(64.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-20).dp), // Negative offset to overlap the icon on top of the
                // dialog
                contentAlignment = Alignment.Center) {
                  Icon(
                      imageVector = Icons.Filled.Notifications,
                      contentDescription = "Notification Icon",
                      tint = Color.White,
                      modifier =
                          Modifier.size(48.dp).clip(CircleShape).background(colorScheme.primary))
                }
          }
        }
  }
}
