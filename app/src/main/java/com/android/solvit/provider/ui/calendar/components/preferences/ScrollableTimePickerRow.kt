package com.android.solvit.provider.ui.calendar.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlinx.coroutines.launch

/**
 * A composable function that displays a scrollable time picker row with hour and minute selectors.
 * It allows users to select a specific time by scrolling through hours and minutes with snapping
 * behavior.
 *
 * @param selectedTime The currently selected time as a `LocalTime` object.
 * @param onTimeSelected Callback invoked when the user selects a new time.
 * @param modifier Modifier for customizing the appearance and layout of the time picker row.
 * @param testTagPrefix A prefix used for testing tags applied to components for UI testing.
 *
 * This function separates time selection into two columns:
 * - **Hour Selector:** Scrolls through 0-23.
 * - **Minute Selector:** Scrolls through 0-59 in steps of 5.
 *
 * Selected values are highlighted, and the picker snaps to the nearest value when scrolling stops.
 */
@Composable
fun ScrollableTimePickerRow(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = ""
) {
  var activeColumn by remember { mutableStateOf<Int?>(null) }

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .height(120.dp)
              .padding(horizontal = 16.dp)
              .testTag("${testTagPrefix}_row"),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).testTag("${testTagPrefix}_hour")) {
          TimeColumn(
              selectedTime = selectedTime.hour,
              range = 0..23,
              onValueSelected = { onTimeSelected(selectedTime.withHour(it)) },
              isActive = activeColumn == 0,
              onScrollStateChanged = { isScrolling -> activeColumn = if (isScrolling) 0 else null },
              testTag = "${testTagPrefix}_hour_picker")
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.onBackground)

        Box(modifier = Modifier.weight(1f).testTag("${testTagPrefix}_minute")) {
          TimeColumn(
              selectedTime = selectedTime.minute,
              range = 0..59 step 5,
              onValueSelected = { onTimeSelected(selectedTime.withMinute(it)) },
              isActive = activeColumn == 1,
              onScrollStateChanged = { isScrolling -> activeColumn = if (isScrolling) 1 else null },
              testTag = "${testTagPrefix}_minute_picker")
        }
      }
}

/**
 * A composable function representing a scrollable list of time values (hours or minutes). It
 * supports snapping behavior and highlights the currently selected value.
 *
 * @param selectedTime The currently selected time value.
 * @param range The range of selectable time values (e.g., 0-23 for hours, 0-59 for minutes).
 * @param onValueSelected Callback invoked when a new time value is selected.
 * @param isActive Indicates whether the column is actively being scrolled.
 * @param onScrollStateChanged Callback triggered when the scroll state changes.
 * @param testTag A unique test tag for UI testing.
 *
 * This function ensures that the selected value snaps into place when scrolling stops. It uses a
 * lazy column with visual feedback for selection.
 */
@Composable
private fun TimeColumn(
    selectedTime: Int,
    range: IntProgression,
    onValueSelected: (Int) -> Unit,
    isActive: Boolean,
    onScrollStateChanged: (Boolean) -> Unit,
    testTag: String
) {
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = range.indexOf(selectedTime))
  val coroutineScope = rememberCoroutineScope()
  var isScrolling by remember { mutableStateOf(false) }
  var currentSelectedValue by remember { mutableStateOf(selectedTime) }

  // Update selected value and scroll position when prop changes
  LaunchedEffect(selectedTime) {
    if (selectedTime != currentSelectedValue) {
      currentSelectedValue = selectedTime
      listState.scrollToItem(range.indexOf(selectedTime))
    }
  }

  // Handle scroll state and snapping
  LaunchedEffect(listState.isScrollInProgress) {
    val wasScrolling = isScrolling
    isScrolling = listState.isScrollInProgress

    if (wasScrolling && !isScrolling) {
      val targetIndex =
          ((listState.firstVisibleItemIndex * 40 + listState.firstVisibleItemScrollOffset + 20) /
                  40)
              .coerceIn(0, range.count() - 1)

      coroutineScope.launch {
        listState.animateScrollToItem(targetIndex)
        val newValue = range.elementAt(targetIndex)
        if (newValue != currentSelectedValue) {
          currentSelectedValue = newValue
          onValueSelected(newValue)
        }
      }
    }
    onScrollStateChanged(isScrolling)
  }

  LazyColumn(
      state = listState,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxHeight().testTag(testTag),
      userScrollEnabled = true) {
        items(range.count() + 2) { index ->
          when {
            index == 0 || index == range.count() + 1 -> {
              Spacer(modifier = Modifier.height(40.dp))
            }
            else -> {
              val time = range.elementAt(index - 1)
              val isSelected = time == currentSelectedValue

              Box(
                  modifier =
                      Modifier.height(40.dp)
                          .fillMaxWidth()
                          .alpha(if (isSelected) 1f else 0.6f)
                          .testTag("${testTag}_item_$time")
                          .clickable { onValueSelected(time) },
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = time.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground)
                  }
            }
          }
        }
      }
}
