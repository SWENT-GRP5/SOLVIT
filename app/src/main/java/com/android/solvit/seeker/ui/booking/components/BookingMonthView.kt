package com.android.solvit.seeker.ui.booking.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.header.CalendarDaysHeader
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.ui.theme.Typography
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun BookingMonthView(
    viewDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<TimeSlot>>,
    serviceColor: Color = colorScheme.secondary,
    deadlineDate: LocalDate? = null,
    modifier: Modifier = Modifier
) {
  val currentMonth = YearMonth.from(viewDate)

  Column(modifier = modifier.fillMaxSize().testTag("bookingMonthView")) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(8.dp)
                .clickable { onHeaderClick() }
                .testTag("monthHeader"),
        horizontalArrangement = Arrangement.Center) {
          Text(
              text =
                  currentMonth.format(
                      DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.testTag("monthHeaderText"))
        }

    CalendarDaysHeader(modifier = Modifier.testTag("calendarDaysHeader"))

    LazyVerticalGrid(
        columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().testTag("monthDayGrid")) {
          val firstDayOfMonth = currentMonth.atDay(1)
          val lastDayOfMonth = currentMonth.atEndOfMonth()
          val firstDayOfGrid =
              firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          val lastDayOfGrid = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

          val daysInGrid =
              generateSequence(firstDayOfGrid) { date ->
                    val next = date.plusDays(1)
                    if (next.isAfter(lastDayOfGrid)) null else next
                  }
                  .toList()

          items(daysInGrid.size) { index ->
            val date = daysInGrid[index]
            val isCurrentMonth = YearMonth.from(date) == currentMonth
            val dayModifier = Modifier.aspectRatio(1f).testTag("monthDay_${date}")

            BookingMonthDayItem(
                date = date,
                isCurrentMonth = isCurrentMonth,
                timeSlots =
                    if (deadlineDate != null && date.isAfter(deadlineDate)) emptyList()
                    else timeSlots[date] ?: emptyList(),
                onDateSelected = onDateSelected,
                serviceColor = serviceColor,
                deadlineDate = deadlineDate,
                modifier = dayModifier)
          }
        }
  }
}
