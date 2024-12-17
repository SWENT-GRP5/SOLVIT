package com.android.solvit.provider.ui.calendar.components.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.ui.theme.Typography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            style = Typography.bodySmall,
            color =
                if (date == today) {
                  colorScheme.primary
                } else {
                  colorScheme.onSurface.copy(alpha = 0.7f)
                },
            modifier = Modifier.testTag("weekDayName_${date}"))

        Text(
            text = date.dayOfMonth.toString(),
            style = Typography.bodyMedium,
            color =
                when (date) {
                  today -> colorScheme.primary
                  else -> colorScheme.onSurface
                },
            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.testTag("weekDayNumber_${date}"))
      }
}
