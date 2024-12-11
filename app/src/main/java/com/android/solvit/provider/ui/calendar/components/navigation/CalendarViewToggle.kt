package com.android.solvit.provider.ui.calendar.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.model.CalendarView

@Composable
fun CalendarViewToggle(
    currentView: CalendarView,
    onViewChange: (CalendarView) -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .testTag("calendarViewToggleRow"),
      horizontalArrangement = Arrangement.Center) {
        SegmentedButtons(
            currentView = currentView,
            onViewChange = onViewChange,
            modifier = Modifier.testTag("segmentedButtons"))
      }
}
