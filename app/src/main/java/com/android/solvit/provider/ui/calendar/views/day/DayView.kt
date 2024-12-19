package com.android.solvit.provider.ui.calendar.views.day

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.ui.calendar.components.grid.TimeGrid
import com.android.solvit.provider.ui.calendar.components.timeslot.ServiceRequestTimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.ui.theme.Typography
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A composable function that displays a detailed day view in a calendar, showing scheduled service
 * requests as time slots. It highlights the current date and allows viewing and interacting with
 * scheduled service requests.
 *
 * @param date The date to be displayed in the day view.
 * @param onHeaderClick Callback invoked when the date header is clicked.
 * @param timeSlots A map of service requests grouped by date.
 * @param onServiceRequestClick Callback invoked when a service request is clicked.
 * @param modifier Modifier for customizing the appearance and layout of the day view.
 *
 * **Features:**
 * - **Header:** Displays the selected date in a bold, formatted style.
 * - **Time Grid:** Shows a vertical timeline of 24 hours.
 * - **Service Requests:** Displays service requests as time slots based on their scheduled time.
 * - **Current Time Line:** Highlights the current time if the selected date is today.
 *
 * The component supports clicking the header to trigger additional actions like opening a date
 * picker or navigating to another view.
 */
@Composable
fun DayView(
    date: LocalDate,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    onServiceRequestClick: (ServiceRequest) -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
  val today = LocalDate.now()

  Column(modifier = modifier.fillMaxSize()) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(8.dp)
                .clickable { onHeaderClick() }
                .testTag("dayViewHeader"),
        horizontalArrangement = Arrangement.Center) {
          Text(
              text =
                  date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault())),
              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = if (date == today) colorScheme.primary else colorScheme.onBackground,
              modifier = Modifier.testTag("dayViewHeaderText"))
        }

    TimeGrid(
        modifier = Modifier.weight(1f).testTag("dayViewTimeGrid"),
        hourHeight = 60.dp,
        currentTime = if (date == today) LocalTime.now() else null,
        showCurrentTimeLine = date == today,
        numberOfColumns = 1) { hour, _, contentModifier ->
          Box(modifier = contentModifier) {
            timeSlots[date]?.forEach { request ->
              val startTime =
                  request.meetingDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

              if (startTime.hour == hour) {
                ServiceRequestTimeSlot(
                    request = request,
                    hourHeight = 60.dp,
                    onClick = onServiceRequestClick,
                    calendarView = CalendarView.DAY,
                    modifier = Modifier.testTag("dayViewServiceRequest_${request.uid}"))
              }
            }
          }
        }
  }
}
