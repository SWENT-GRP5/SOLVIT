package com.android.solvit.provider.ui.calendar.views.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.android.solvit.shared.ui.theme.Typography
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A composable function that displays a calendar day item in a month view. It visually highlights
 * the current day, indicates whether the date belongs to the current month, and shows service
 * request statuses using status indicators.
 *
 * @param date The date to be displayed in the calendar item.
 * @param isCurrentDay Indicates if the displayed date is the current day.
 * @param isCurrentMonth Indicates if the displayed date belongs to the currently viewed month.
 * @param onDateSelected Callback invoked when the calendar day item is clicked.
 * @param timeSlots A list of service requests scheduled for the day.
 * @param modifier Modifier for customizing the appearance and layout of the calendar item.
 *
 * **Features:**
 * - **Visual Highlighting:**
 *     - Current day: Highlighted with a primary background and bold text.
 *     - Days outside the current month: Dimmed text to indicate inactive dates.
 * - **Service Request Indicators:**
 *     - Displays status indicators for service requests scheduled throughout the day.
 *     - Separates morning and afternoon service requests into two rows for better visibility.
 * - **Clickable:** The entire calendar day item is clickable, triggering the date selection
 *   callback.
 */
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
              .testTag("monthDayItem_${date}"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = date.dayOfMonth.toString(),
            style =
                Typography.bodyMedium.copy(
                    fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal),
            color = textColor,
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

/**
 * A utility function that calculates the status of service requests for a specific day. It sorts
 * service requests by meeting time and maps each request to its corresponding time and status.
 *
 * @param requests A list of scheduled service requests.
 * @return A list of pairs where each pair contains:
 * - `LocalTime`: The time of the service request.
 * - `ServiceRequestStatus`: The service request's status.
 *
 * The function supports calendar display components by providing a ready-to-use list of scheduled
 * times and statuses for status indicators.
 */
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
