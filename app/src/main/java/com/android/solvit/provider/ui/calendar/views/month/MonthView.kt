package com.android.solvit.provider.ui.calendar.views.month

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.header.CalendarDaysHeader
import com.android.solvit.shared.model.request.ServiceRequest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

@Composable
fun MonthView(
    viewDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    modifier: Modifier = Modifier
) {
  val currentMonth = YearMonth.from(viewDate)

  Column(modifier = modifier.fillMaxSize()) {
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
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("monthHeaderText"))
        }

    CalendarDaysHeader()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().testTag("monthCalendarGrid")) {
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
            MonthDayItem(
                date = date,
                isCurrentDay = date == LocalDate.now(),
                isCurrentMonth = YearMonth.from(date) == currentMonth,
                onDateSelected = onDateSelected,
                timeSlots = timeSlots[date] ?: emptyList(),
                modifier = Modifier.testTag("monthDay_${date}"))
          }
        }
  }
}
