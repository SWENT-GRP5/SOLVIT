package com.android.solvit.seeker.ui.booking.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.grid.TimeGrid
import com.android.solvit.seeker.model.SeekerBookingViewModel
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
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
    deadlineDate: LocalDate? = null,
    serviceRequest: ServiceRequest,
    viewModel: SeekerBookingViewModel,
    modifier: Modifier = Modifier
) {
  val today = LocalDate.now()
  val currentTime = LocalTime.now()
  val listState = rememberLazyListState()

  // Filter out past time slots and slots after deadline
  val availableTimeSlots =
      if (deadlineDate != null && viewDate.isAfter(deadlineDate)) {
        emptyList()
      } else {
        timeSlots.filter { timeSlot ->
          when {
            viewDate.isAfter(today) -> true
            viewDate.isBefore(today) -> false
            else -> !timeSlot.start.isBefore(currentTime)
          }
        }
      }

  // Calculate initial scroll position
  LaunchedEffect(viewDate, availableTimeSlots) {
    val scrollHour =
        when {
          availableTimeSlots.isNotEmpty() -> {
            availableTimeSlots
                .minOf { it.start.hour }
                .let { firstHour ->
                  maxOf(0, firstHour - 1) // Scroll one hour earlier for context
                }
          }
          viewDate == today -> currentTime.hour
          else -> 9 // Default to 9 AM
        }
    listState.scrollToItem(scrollHour)
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
              color = if (viewDate == today) colorScheme.primary else colorScheme.onBackground,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("dayViewHeaderText"))
        }

    TimeGrid(
        modifier = Modifier.weight(1f).testTag("dayViewTimeGrid"),
        hourHeight = 60.dp,
        currentTime = if (viewDate == today) LocalTime.now() else null,
        showCurrentTimeLine = viewDate == today,
        listState = listState,
        numberOfColumns = 1) { hour, _, contentModifier ->
          Box(modifier = contentModifier) {
            availableTimeSlots.forEach { timeSlot ->
              if (timeSlot.start.hour == hour) {
                AvailableTimeSlot(
                    date = viewDate,
                    startTime = timeSlot.start,
                    endTime = timeSlot.end,
                    onBook = { _, _ ->
                      viewModel.addAcceptedRequest(serviceRequest, timeSlot, viewDate)
                      onTimeSlotSelected(timeSlot)
                    },
                    onCancel = { /* Do nothing */},
                    serviceColor = serviceColor,
                    deadlineDate = deadlineDate,
                    modifier =
                        Modifier.testTag(
                            "dayViewTimeSlot_${timeSlot.start.hour}_${timeSlot.start.minute}"))
              }
            }
          }
        }
  }
}
