package com.android.solvit.provider.ui.calendar.components.timeslot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.ui.calendar.components.utils.StatusIndicator
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.ui.theme.Typography
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ServiceRequestTimeSlot(
    request: ServiceRequest,
    hourHeight: Dp,
    modifier: Modifier = Modifier,
    onClick: (ServiceRequest) -> Unit = {},
    calendarView: CalendarView = CalendarView.DAY
) {
  val startTime =
      request.meetingDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime() ?: return
  val endTime = startTime.plusHours(1)
  val minutesInHour = startTime.minute

  val topOffsetDp = hourHeight * (minutesInHour / 60f)
  val durationMinutes = Duration.between(startTime, endTime).toMinutes().toInt()
  val height = hourHeight * (durationMinutes / 60f)

  val backgroundColor = ServiceRequestStatus.getStatusColor(request.status).copy(alpha = 0.4f)
  val statusColor = ServiceRequestStatus.getStatusColor(request.status)

  Box(
      modifier =
          modifier
              .offset(y = topOffsetDp)
              .height(height)
              .fillMaxWidth()
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .border(1.dp, statusColor, RoundedCornerShape(8.dp))
              .clickable { onClick(request) }
              .padding(4.dp)
              .zIndex(1f)) {
        when (calendarView) {
          CalendarView.WEEK -> {
            Text(
                text = request.title,
                style =
                    Typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold, fontSize = 8.sp, lineHeight = 12.sp),
                color = colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("serviceRequestTitle_${request.uid}"))
          }
          CalendarView.DAY -> {
            Column {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    StatusIndicator(request.status)
                    Text(
                        text =
                            "${startTime.format(DateTimeFormatter.ofPattern("H:mm"))} - ${endTime.format(DateTimeFormatter.ofPattern("H:mm"))}",
                        style = Typography.bodyMedium,
                        color = colorScheme.onSurface,
                        modifier = Modifier.testTag("serviceRequestTime_${request.uid}"))
                    Text(
                        text = request.title,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier.weight(1f).testTag("serviceRequestTitle_${request.uid}"))
                  }
              if (request.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.description,
                    style = Typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 14.sp),
                    color = colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("serviceRequestDescription_${request.uid}"))
              }
            }
          }
          else -> Unit
        }
      }
}
