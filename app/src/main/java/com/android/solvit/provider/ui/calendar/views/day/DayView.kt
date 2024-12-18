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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.ui.calendar.components.grid.TimeGrid
import com.android.solvit.provider.ui.calendar.components.timeslot.ServiceRequestTimeSlot
import com.android.solvit.shared.model.provider.Schedule
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.ui.theme.Typography
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayView(
    viewDate: LocalDate,
    timeSlots: List<ServiceRequest>,
    onHeaderClick: () -> Unit = {},
    onServiceRequestClick: (ServiceRequest) -> Unit = {},
    schedule: Schedule? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
  val today = LocalDate.now()
  val listState = rememberLazyListState()

  // Calculate initial scroll position
  LaunchedEffect(viewDate, timeSlots) {
    val scrollHour =
        when {
          timeSlots.isNotEmpty() -> {
            timeSlots
                .mapNotNull { request ->
                  request.meetingDate
                      ?.toInstant()
                      ?.atZone(ZoneId.systemDefault())
                      ?.toLocalTime()
                      ?.hour
                }
                .minOrNull() ?: 9
          }
          viewDate == today -> LocalTime.now().hour
          else ->
              schedule?.regularHours?.get(viewDate.dayOfWeek.name)?.firstOrNull()?.startHour ?: 9
        }
    listState.scrollToItem(maxOf(0, scrollHour - 1)) // Scroll one hour earlier for context
  }

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
                  viewDate.format(
                      DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault())),
              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color =
                  if (viewDate == today) {
                    colorScheme.primary
                  } else {
                    colorScheme.onBackground
                  },
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("dayViewHeaderText"))
        }

    TimeGrid(
        modifier = Modifier.weight(1f).testTag("dayViewTimeGrid"),
        hourHeight = 60.dp,
        currentTime = if (viewDate == today) LocalTime.now() else null,
        showCurrentTimeLine = viewDate == today,
        numberOfColumns = 1,
        listState = listState,
        schedule = schedule,
        dates = listOf(viewDate)) { hour, _, contentModifier ->
          Box(modifier = contentModifier) {
            timeSlots.forEach { request ->
              val startTime =
                  request.meetingDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
                      ?: return@forEach

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
