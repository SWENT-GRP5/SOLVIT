package com.android.solvit.seeker.ui.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RequestsOverviewScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel = viewModel(factory = ServiceRequestViewModel.Factory)
) {
  Scaffold(
      modifier = Modifier.padding(16.dp).testTag("overviewScreen"),
      content = { paddingValues ->
        val requests = requestViewModel.requests.collectAsState()
        if (requests.value.isEmpty()) {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .background(color = Color.White, shape = RoundedCornerShape(18.dp))
                      .testTag("noServiceRequestsScreen"),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
                text = "You have no active service request. Create one.",
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF4E616D),
                        letterSpacing = 0.5.sp,
                    ))
          }
        } else {
          LazyColumn(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .background(color = Color.White, shape = RoundedCornerShape(18.dp)),
          ) {
            // Loop through each request and displays it in the overview layout
            items(requests.value) { request ->
              RequestItemRow(
                  request = request,
                  onClick = {
                    requestViewModel.selectRequest(request)
                    navigationActions.navigateTo(Route.EDIT_REQUEST)
                  })
            }
          }
        }
      },
      bottomBar = {
        SeekerBottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      })
}

@Composable
fun RequestItemRow(request: ServiceRequest, onClick: () -> Unit) {
  // Use a Box to wrap the entire row and the bottom bar
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .background(color = Color.Transparent)
              .padding(start = 16.dp, end = 16.dp)
              .testTag("requestListItem")) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(80.dp).background(color = Color.Transparent),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        ) {
          Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
              verticalAlignment = Alignment.Top,
              modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)) {
                      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      val date = dateFormat.format(request.dueDate.toDate())
                      Text(
                          text = "Deadline: $date",
                          style =
                              TextStyle(
                                  fontSize = 12.sp,
                                  lineHeight = 16.sp,
                                  fontFamily = FontFamily.Default,
                                  fontWeight = FontWeight(500),
                                  color = Color(0xFF49454F),
                                  letterSpacing = 0.5.sp,
                              ))
                      Text(
                          text = request.title,
                          style =
                              TextStyle(
                                  fontSize = 16.sp,
                                  lineHeight = 24.sp,
                                  fontFamily = FontFamily.Default,
                                  fontWeight = FontWeight(400),
                                  color = Color(0xFF191C1E),
                                  letterSpacing = 0.5.sp,
                              ))
                      Text(
                          text = request.type.name.lowercase().replaceFirstChar { it.uppercase() },
                          style =
                              TextStyle(
                                  fontSize = 14.sp,
                                  lineHeight = 20.sp,
                                  fontFamily = FontFamily.Default,
                                  fontWeight = FontWeight(400),
                                  color = Color(0xFF49454F),
                                  letterSpacing = 0.25.sp,
                              ))
                    }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                    verticalAlignment = Alignment.Top,
                ) {
                  Text(
                      text =
                          request.status.name.replace("_", " ").lowercase().replaceFirstChar {
                            it.uppercase()
                          },
                      style =
                          TextStyle(
                              fontSize = 11.sp,
                              lineHeight = 16.sp,
                              fontFamily = FontFamily.Default,
                              fontWeight = FontWeight(500),
                              color = getStatusColor(request.status),
                              textAlign = TextAlign.Right,
                              letterSpacing = 0.5.sp,
                          ))
                  Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "Navigate",
                      tint = Color.Gray)
                }
              }
        }

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.LightGray))
      }
}

fun getStatusColor(status: ServiceRequestStatus): Color {
  return when (status) {
    ServiceRequestStatus.PENDING -> Color(0xFFE5A800)
    ServiceRequestStatus.ACCEPTED -> Color(0xFF00A3FF)
    ServiceRequestStatus.STARTED -> Color(0xFF00BFA5)
    ServiceRequestStatus.ENDED -> Color(0xFF02F135)
    ServiceRequestStatus.ARCHIVED -> Color(0xFF000000)
  }
}
