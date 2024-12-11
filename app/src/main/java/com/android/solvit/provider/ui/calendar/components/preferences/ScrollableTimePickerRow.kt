package com.android.solvit.provider.ui.calendar.components.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlinx.coroutines.launch

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
        TimeColumn(
            selectedTime = selectedTime.hour,
            range = 0..23,
            onValueSelected = { onTimeSelected(selectedTime.withHour(it)) },
            isActive = activeColumn == 0,
            onScrollStateChanged = { isScrolling -> activeColumn = if (isScrolling) 0 else null },
            modifier = Modifier.weight(1f).testTag("${testTagPrefix}_hour"),
            testTag = "${testTagPrefix}_hour")

        Text(
            text = ":",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.onBackground)

        TimeColumn(
            selectedTime = selectedTime.minute,
            range = 0..59 step 5,
            onValueSelected = { onTimeSelected(selectedTime.withMinute(it)) },
            isActive = activeColumn == 1,
            onScrollStateChanged = { isScrolling -> activeColumn = if (isScrolling) 1 else null },
            modifier = Modifier.weight(1f).testTag("${testTagPrefix}_minute"),
            testTag = "${testTagPrefix}_minute")
      }
}

@Composable
private fun TimeColumn(
    selectedTime: Int,
    range: IntProgression,
    onValueSelected: (Int) -> Unit,
    isActive: Boolean,
    onScrollStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = range.indexOf(selectedTime))
  val coroutineScope = rememberCoroutineScope()

  // Track if the column is being scrolled
  var isScrolling by remember { mutableStateOf(false) }

  LaunchedEffect(listState.isScrollInProgress) {
    isScrolling = listState.isScrollInProgress
    onScrollStateChanged(isScrolling)
    if (!listState.isScrollInProgress) {
      val firstIndex = listState.firstVisibleItemIndex
      val offset = listState.firstVisibleItemScrollOffset
      val targetIndex = if (offset > 20) firstIndex + 1 else firstIndex

      coroutineScope.launch {
        listState.animateScrollToItem(targetIndex)
        onValueSelected(range.elementAt(targetIndex))
      }
    }
  }

  Box(modifier = modifier.height(120.dp), contentAlignment = Alignment.Center) {
    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight().testTag("${testTag}_list")) {
          items(range.count() + 2) { index ->
            when {
              index == 0 || index > range.count() -> {
                Spacer(modifier = Modifier.height(40.dp))
              }
              else -> {
                val value = range.elementAt(index - 1)
                val isVisible =
                    index ==
                        remember { derivedStateOf { listState.firstVisibleItemIndex } }.value + 1

                Text(
                    text = value.toString().padStart(2, '0'),
                    modifier =
                        Modifier.height(40.dp)
                            .padding(vertical = 8.dp)
                            .scale(if (isVisible) 1.2f else 1f)
                            .alpha(if (isVisible) 1f else 0.3f)
                            .testTag("${testTag}_item_$value"),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal),
                    color =
                        when {
                          isScrolling -> MaterialTheme.colorScheme.primary
                          isVisible -> MaterialTheme.colorScheme.onBackground
                          else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        },
                    textAlign = TextAlign.Center)
              }
            }
          }
        }
  }
}
