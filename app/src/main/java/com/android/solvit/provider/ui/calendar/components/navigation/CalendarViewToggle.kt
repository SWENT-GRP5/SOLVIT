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

/**
 * A composable function that displays a toggle control for switching between different calendar
 * views (e.g., Day, Week, Month). The currently selected view is highlighted, and changes are
 * handled through a callback.
 *
 * @param currentView The currently selected calendar view.
 * @param onViewChange Callback invoked when a new calendar view is selected.
 * @param modifier Modifier for customizing the appearance and layout of the toggle control.
 *
 * This function uses a segmented button style for selecting between different views and provides
 * accessibility through test tags for easy UI testing.
 */
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
