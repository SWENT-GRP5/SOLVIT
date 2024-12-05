package com.android.solvit.provider.ui.calendar.views.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.utils.StatusIndicator
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MonthDayItem(
    date: LocalDate,
    isCurrentDay: Boolean,
    isCurrentMonth: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    timeSlots: List<ServiceRequest>,
    modifier: Modifier = Modifier
) {
  val dayStatuses = calculateDayStatus(timeSlots)

  val backgroundColor =
      when {
        isCurrentDay -> colorScheme.primary.copy(alpha = 0.6f)
        else -> Color.Transparent
      }

  val textColor =
      when {
        !isCurrentMonth -> colorScheme.onSurface.copy(alpha = 0.5f)
        isCurrentDay -> colorScheme.onPrimary
        else -> colorScheme.onSurface
      }

  Column(
      modifier =
          modifier
              .aspectRatio(1f)
              .padding(2.dp)
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .clickable { onDateSelected(date) }
              .padding(4.dp)
              .testTag("monthDayColumn_${date}"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.testTag("monthDayNumber_${date}"))

        if (dayStatuses.isNotEmpty()) {
          Column(
              modifier = Modifier.padding(top = 2.dp).testTag("monthDayStatusColumn_${date}"),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Morning row (before noon)
                Row(
                    modifier = Modifier.testTag("monthDayMorningRow_${date}"),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      dayStatuses
                          .filter { (time, _) -> time.hour < 12 }
                          .forEach { (time, status) ->
                            StatusIndicator(
                                status = status,
                                modifier =
                                    Modifier.testTag(
                                        "monthDayMorningStatus_${date}_${
                                        time.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    }"))
                          }
                    }

                // Afternoon row (noon and after)
                Row(
                    modifier = Modifier.testTag("monthDayAfternoonRow_${date}"),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      dayStatuses
                          .filter { (time, _) -> time.hour >= 12 }
                          .forEach { (time, status) ->
                            StatusIndicator(
                                status = status,
                                modifier =
                                    Modifier.testTag(
                                        "monthDayAfternoonStatus_${date}_${
                                        time.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    }"))
                          }
                    }
              }
        }
      }
}

fun calculateDayStatus(
    requests: List<ServiceRequest>
): List<Pair<LocalTime, ServiceRequestStatus>> {
  return requests
      .sortedBy { it.meetingDate!!.toInstant() }
      .map { request ->
        val time = request.meetingDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        time to request.status
      }
}
