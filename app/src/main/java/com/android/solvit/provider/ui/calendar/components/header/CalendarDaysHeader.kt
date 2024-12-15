package com.android.solvit.provider.ui.calendar.components.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.ui.theme.Typography
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarDaysHeader(modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("calendarDaysHeader"),
      horizontalArrangement = Arrangement.Center) {
        val daysOfWeek = DayOfWeek.entries.toTypedArray()
        for (dayOfWeek in daysOfWeek) {
          Text(
              text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
              modifier = Modifier.weight(1f).padding(4.dp).testTag("dayOfWeek_${dayOfWeek.name}"),
              textAlign = TextAlign.Center,
              style = Typography.bodySmall,
          )
        }
      }
}
