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
import kotlin.math.abs
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
  var isScrolling by remember { mutableStateOf(false) }
  var previousScrollTime by remember { mutableStateOf(0L) }
  var scrollVelocity by remember { mutableStateOf(0f) }
  var currentSelectedValue by remember { mutableStateOf(selectedTime) }

  // Constants for calculations
  val itemHeight = 40
  val visibleItems = 3 // Number of fully visible items
  val centerOffset = (visibleItems * itemHeight) / 2 // Center of the viewport

  // Update current selected value when prop changes
  LaunchedEffect(selectedTime) { currentSelectedValue = selectedTime }

  // Debounce scroll state changes
  LaunchedEffect(listState.isScrollInProgress) {
    isScrolling = listState.isScrollInProgress
    onScrollStateChanged(isScrolling)
  }

  // Track scroll velocity
  LaunchedEffect(listState.firstVisibleItemScrollOffset) {
    val currentTime = System.currentTimeMillis()
    val timeDiff = currentTime - previousScrollTime
    if (timeDiff > 0) {
      scrollVelocity = listState.firstVisibleItemScrollOffset.toFloat() / timeDiff
    }
    previousScrollTime = currentTime
  }

  // Handle snap-to-position after scroll ends
  LaunchedEffect(listState.isScrollInProgress) {
    if (!listState.isScrollInProgress) {
      val firstIndex = listState.firstVisibleItemIndex
      val offset = listState.firstVisibleItemScrollOffset

      // Calculate the position relative to the center
      val currentPosition = (firstIndex * itemHeight) + offset
      val targetPosition = currentPosition + (itemHeight / 2)

      // Calculate target index based on position and velocity
      val velocityThreshold = 0.5f
      val baseIndex = (targetPosition / itemHeight).toInt()

      val targetIndex =
          when {
            scrollVelocity > velocityThreshold -> baseIndex + 1
            scrollVelocity < -velocityThreshold -> baseIndex - 1
            else -> baseIndex
          }.coerceIn(0, range.count() - 1)

      // Animate to target with spring-like behavior
      coroutineScope.launch {
        listState.animateScrollToItem(index = targetIndex, scrollOffset = 0)
        // Update the selected value and notify parent
        val newValue = range.elementAt(targetIndex)
        currentSelectedValue = newValue
        onValueSelected(newValue)
      }

      // Reset velocity
      scrollVelocity = 0f
    }
  }

  Box(modifier = modifier.height(120.dp), contentAlignment = Alignment.Center) {
    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight().testTag("${testTag}_list"),
        userScrollEnabled = true) {
          items(count = range.count() + 2, key = { it }) { index ->
            when {
              index == 0 || index > range.count() -> {
                Spacer(modifier = Modifier.height(40.dp))
              }
              else -> {
                val time = range.elementAt(index - 1)
                val isSelected = time == currentSelectedValue

                // Calculate distance from center for visual effects
                val itemPosition =
                    (index * itemHeight) -
                        (listState.firstVisibleItemIndex * itemHeight) -
                        listState.firstVisibleItemScrollOffset
                val distanceFromCenter = abs(centerOffset - itemPosition) / itemHeight.toFloat()

                val alpha = if (isSelected) 1f else maxOf(0.3f, 1f - (distanceFromCenter * 0.3f))
                val scale = if (isSelected) 1.2f else maxOf(0.8f, 1f - (distanceFromCenter * 0.15f))

                Box(
                    modifier =
                        Modifier.height(40.dp)
                            .fillMaxWidth()
                            .scale(scale)
                            .alpha(alpha)
                            .testTag("${testTag}_item_$time"),
                    contentAlignment = Alignment.Center) {
                      Text(
                          text = time.toString().padStart(2, '0'),
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight =
                                      if (isSelected) FontWeight.Bold else FontWeight.Normal),
                          color =
                              if (isScrolling) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onBackground,
                          textAlign = TextAlign.Center)
                    }
              }
            }
          }
        }
  }
}
