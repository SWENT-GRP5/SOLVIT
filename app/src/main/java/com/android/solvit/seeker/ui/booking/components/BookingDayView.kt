package com.android.solvit.seeker.ui.booking.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.grid.TimeGrid
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.ui.theme.Typography
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BookingDayView(
    viewDate: LocalDate,
    timeSlots: List<TimeSlot>,
    onHeaderClick: () -> Unit,
    onTimeSlotSelected: (TimeSlot) -> Unit,
    serviceColor: Color = colorScheme.secondary,
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
                  viewDate.format(
                      DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault())),
              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = if (viewDate == today) colorScheme.primary else colorScheme.onBackground,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("dayViewHeaderText"))
        }

    TimeGrid(
        modifier = Modifier.weight(1f).testTag("dayViewTimeGrid"),
        hourHeight = 60.dp,
        currentTime = if (viewDate == today) LocalTime.now() else null,
        showCurrentTimeLine = viewDate == today,
        numberOfColumns = 1) { hour, _, contentModifier ->
          Box(modifier = contentModifier) {
            timeSlots.forEach { timeSlot ->
              if (timeSlot.start.hour == hour) {
                AvailableTimeSlot(
                    date = viewDate,
                    startTime = timeSlot.start,
                    endTime = timeSlot.end,
                    onBook = { _, _ -> onTimeSlotSelected(timeSlot) },
                    onCancel = { /* Do nothing */},
                    serviceColor = serviceColor,
                    modifier =
                        Modifier.testTag(
                            "dayViewTimeSlot_${timeSlot.start.hour}_${timeSlot.start.minute}"))
              }
            }
          }
        }
  }
}
