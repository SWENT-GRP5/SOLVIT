package com.android.solvit.provider.ui.calendar.components.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun WeekDayHeader(
    date: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .then(modifier)
              .padding(4.dp)
              .clickable { onDateSelected(date) }
              .testTag("weekDayHeader_${date}"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())),
            style = MaterialTheme.typography.bodySmall,
            color =
                if (date == today) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                },
            modifier = Modifier.testTag("weekDayName_${date}"))

        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color =
                when (date) {
                  today -> MaterialTheme.colorScheme.primary
                  else -> MaterialTheme.colorScheme.onSurface
                },
            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.testTag("weekDayNumber_${date}"))
      }
}
