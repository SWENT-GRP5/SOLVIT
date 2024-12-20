package com.android.solvit.seeker.ui.booking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.model.provider.TimeSlot
import java.time.LocalDate

@Composable
fun BookingMonthDayItem(
    date: LocalDate,
    isCurrentMonth: Boolean,
    timeSlots: List<TimeSlot>,
    onDateSelected: (LocalDate) -> Unit,
    serviceColor: Color = colorScheme.secondary,
    deadlineDate: LocalDate? = null,
    modifier: Modifier = Modifier
) {
  val now = LocalDate.now()
  val isSelectable = date.isEqual(now) || date.isAfter(now)
  val hasAvailabilities = timeSlots.isNotEmpty()

  val backgroundColor =
      when {
        date == deadlineDate -> colorScheme.error.copy(alpha = 0.15f)
        date == now -> colorScheme.primary.copy(alpha = 0.15f)
        hasAvailabilities && isSelectable -> {
          if (isCurrentMonth) {
            serviceColor.copy(alpha = 0.2f)
          } else {
            serviceColor.copy(alpha = 0.1f)
          }
        }
        else -> Color.Transparent
      }

  val textColor =
      when {
        date == deadlineDate -> colorScheme.error
        date == now -> colorScheme.primary
        !isCurrentMonth || !isSelectable -> colorScheme.onBackground.copy(alpha = 0.5f)
        else -> colorScheme.onBackground
      }

  val borderColor =
      when {
        date == deadlineDate -> colorScheme.error
        date == now -> colorScheme.primary
        else -> Color.Transparent
      }

  Column(
      modifier =
          modifier
              .aspectRatio(1f)
              .padding(2.dp)
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .border(
                  width = if (date == deadlineDate || date == now) 2.dp else 0.dp,
                  color = borderColor,
                  shape = RoundedCornerShape(8.dp))
              .clickable(enabled = isSelectable && hasAvailabilities) { onDateSelected(date) }
              .padding(4.dp)
              .testTag("bookingMonthDayItem_${date}"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = date.dayOfMonth.toString(),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (date == deadlineDate) FontWeight.Bold else FontWeight.Normal),
            color = textColor,
            modifier = Modifier.testTag("bookingMonthDayNumber_${date}"))
      }
}
