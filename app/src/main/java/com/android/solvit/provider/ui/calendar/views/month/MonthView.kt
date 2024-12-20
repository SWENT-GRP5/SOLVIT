package com.android.solvit.provider.ui.calendar.views.month

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.ui.calendar.components.header.CalendarDaysHeader
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.ui.theme.Typography
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * A composable function that displays a calendar month view with selectable dates, service request
 * indicators, and a header showing the current month and year. The calendar dynamically adjusts to
 * show all days of the current month, including leading and trailing dates for context.
 *
 * @param viewDate The date representing the currently viewed month.
 * @param onDateSelected Callback invoked when a specific date is selected.
 * @param onHeaderClick Callback invoked when the month header is clicked.
 * @param timeSlots A map of scheduled service requests grouped by date.
 * @param modifier Modifier for customizing the appearance and layout of the month view.
 *
 * **Features:**
 * - **Header:** Displays the current month and year, clickable for month selection.
 * - **Weekday Labels:** Displays labels for days of the week (Mon-Sun).
 * - **Calendar Grid:**
 *     - Shows all days of the current month, including leading and trailing days.
 *     - Highlights the current day visually.
 *     - Dimmed text for days outside the current month.
 *     - Service request indicators for each day based on provided time slots.
 * - **Date Selection:** The entire date cell is clickable, triggering the date selection callback.
 */
@Composable
fun MonthView(
    viewDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    modifier: Modifier = Modifier
) {
  val currentMonth = YearMonth.from(viewDate)

  Column(modifier = modifier.fillMaxSize().testTag("monthView")) {
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
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().testTag("monthViewCalendarGrid")) {
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
            val isCurrentDay = date == LocalDate.now()
            val dayModifier = Modifier.aspectRatio(1f).testTag("monthDay_${date}")

            MonthDayItem(
                date = date,
                isCurrentDay = isCurrentDay,
                isCurrentMonth = isCurrentMonth,
                onDateSelected = onDateSelected,
                timeSlots = timeSlots[date] ?: emptyList(),
                modifier = dayModifier)
          }
        }
  }
}
