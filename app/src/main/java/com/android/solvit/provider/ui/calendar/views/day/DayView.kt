package com.android.solvit.provider.ui.calendar.views.day

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayView(
    date: LocalDate,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    onServiceRequestClick: (ServiceRequest) -> Unit = {},
    modifier: Modifier = Modifier
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
              style = MaterialTheme.typography.titleMedium,
              color =
                  if (date == today) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onBackground,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("dayViewHeaderText"))
        }

    TimeGrid(
        modifier = Modifier.weight(1f),
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
