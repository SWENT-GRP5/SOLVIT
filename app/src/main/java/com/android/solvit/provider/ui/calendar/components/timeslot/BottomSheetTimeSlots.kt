package com.android.solvit.provider.ui.calendar.components.timeslot

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.utils.StatusIndicator
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.ui.theme.Typography
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A composable function that displays a list of service requests as time slots in a bottom sheet
 * layout. Each service request includes its scheduled time, title, and optional description. The
 * list is sorted by meeting date in ascending order.
 *
 * @param timeSlots A list of `ServiceRequest` objects representing scheduled service requests.
 * @param onServiceRequestClick Callback invoked when a service request item is clicked.
 * @param modifier Modifier for customizing the appearance and layout of the bottom sheet.
 *
 * **Item Structure:**
 * - **Time Range:** Displays the start and end times of the service request.
 * - **Title:** The title of the service request, shown prominently.
 * - **Description:** An optional description, truncated if too long.
 * - **Status Indicator:** A visual indicator showing the service request's status.
 *
 * The function applies background and border colors based on the service request's status using
 * `ServiceRequestStatus.getStatusColor()`. Items are clickable and provide visual feedback through
 * custom colors and shape styles.
 */
@Composable
fun BottomSheetTimeSlots(
    timeSlots: List<ServiceRequest>,
    onServiceRequestClick: (ServiceRequest) -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier =
          modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("bottomSheetTimeSlotsList"),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(timeSlots.sortedBy { it.meetingDate!!.toInstant() }) { request ->
          val startTime =
              request.meetingDate!!.toInstant().atZone(ZoneId.systemDefault())?.toLocalTime()
                  ?: return@items
          val endTime = startTime.plusHours(1)

          val backgroundColor =
              ServiceRequestStatus.getStatusColor(request.status).copy(alpha = 0.4f)
          val statusColor = ServiceRequestStatus.getStatusColor(request.status)

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .background(backgroundColor, RoundedCornerShape(8.dp))
                      .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                      .padding(8.dp)
                      .clickable { onServiceRequestClick(request) }
                      .testTag("bottomSheetServiceRequest_${request.uid}")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      StatusIndicator(request.status)
                      Text(
                          text =
                              "${startTime.format(DateTimeFormatter.ofPattern("H:mm", Locale.ROOT))} - ${
                            endTime.format(DateTimeFormatter.ofPattern("H:mm", Locale.ROOT))
                        }",
                          style = Typography.bodyMedium,
                          color = colorScheme.onSurface,
                          modifier =
                              Modifier.testTag("bottomSheetServiceRequestTime_${request.uid}"))
                      Text(
                          text = request.title,
                          style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                          color = colorScheme.onSurface,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier =
                              Modifier.weight(1f)
                                  .testTag("bottomSheetServiceRequestTitle_${request.uid}"))
                    }
                if (request.description.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                      text = request.description,
                      style = Typography.bodySmall,
                      color = colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier =
                          Modifier.testTag("bottomSheetServiceRequestDescription_${request.uid}"))
                }
              }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
      }
}
