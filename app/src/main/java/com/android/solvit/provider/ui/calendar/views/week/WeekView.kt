package com.android.solvit.provider.ui.calendar.views.week

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.ui.calendar.components.grid.TimeGrid
import com.android.solvit.provider.ui.calendar.components.header.WeekDayHeader
import com.android.solvit.provider.ui.calendar.components.timeslot.ServiceRequestTimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.ui.theme.Typography
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun WeekView(
    date: LocalDate,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    onServiceRequestClick: (ServiceRequest) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
  val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
  val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
  val today = LocalDate.now()
  val todayIndex = weekDates.indexOf(today).takeIf { it >= 0 }

  Column(modifier = modifier.fillMaxSize()) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(8.dp)
                .clickable { onHeaderClick() }
                .testTag("weekViewHeader"),
        horizontalArrangement = Arrangement.Center) {
          Text(
              text =
                  "${startOfWeek.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))} - ${
                    startOfWeek.plusDays(6).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
                }",
              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.testTag("weekViewHeaderText"))
        }

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 56.dp).testTag("weekViewDaysRow"),
        horizontalArrangement = Arrangement.Center) {
          weekDates.forEach { weekDate ->
            WeekDayHeader(
                date = weekDate,
                today = today,
                onDateSelected = onDateSelected,
                modifier = Modifier.weight(1f).testTag("weekDayHeader_${weekDate}"))
          }
        }

    TimeGrid(
        modifier = Modifier.weight(1f).fillMaxWidth().testTag("weekViewTimeGrid"),
        hourHeight = 60.dp,
        currentTime = if (todayIndex != null) LocalTime.now() else null,
        showCurrentTimeLine = todayIndex != null,
        todayIndex = todayIndex,
        numberOfColumns = 7) { hour, dayIndex, contentModifier ->
          Box(
              modifier =
                  contentModifier.zIndex(1f).testTag("weekViewHour_${hour}_Day_${dayIndex}")) {
                val day = weekDates[dayIndex]
                timeSlots[day]?.forEach { request ->
                  val startTime =
                      request.meetingDate
                          ?.toInstant()
                          ?.atZone(ZoneId.systemDefault())
                          ?.toLocalTime() ?: return@forEach

                  if (startTime.hour == hour) {
                    ServiceRequestTimeSlot(
                        request = request,
                        hourHeight = 60.dp,
                        onClick = onServiceRequestClick,
                        calendarView = CalendarView.WEEK,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .testTag("weekViewServiceRequest_${request.uid}"))
                  }
                }
              }
        }
  }
}
