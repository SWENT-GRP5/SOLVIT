package com.android.solvit.provider.ui.calendar.components.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.util.Locale

@Composable
fun TimeGrid(
    modifier: Modifier = Modifier,
    hourHeight: Dp = 60.dp,
    currentTime: LocalTime? = null,
    showCurrentTimeLine: Boolean = false,
    todayIndex: Int? = null,
    numberOfColumns: Int = 1,
    content: @Composable (hour: Int, dayIndex: Int, contentModifier: Modifier) -> Unit
) {
  val hours = (0..23).toList()
  val currentHour = currentTime?.hour ?: 0
  val currentMinute = currentTime?.minute ?: 0

  Box(modifier = modifier.fillMaxSize()) {
    // Draw vertical lines
    Canvas(modifier = Modifier.fillMaxSize()) {
      val width = size.width
      val height = size.height
      val timeColumnWidth = 56.dp.toPx()
      val columnWidth =
          if (numberOfColumns > 0) (width - timeColumnWidth) / numberOfColumns else width

      if (numberOfColumns > 1) {
        for (i in 1 until numberOfColumns) {
          drawLine(
              color = Color.LightGray,
              start = Offset(x = timeColumnWidth + i * columnWidth, y = 0f),
              end = Offset(x = timeColumnWidth + i * columnWidth, y = height),
              strokeWidth = 1.dp.toPx())
        }
        drawLine(
            color = Color.LightGray,
            start = Offset(x = timeColumnWidth, y = 0f),
            end = Offset(x = timeColumnWidth, y = height),
            strokeWidth = 1.dp.toPx())
      }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), state = rememberLazyListState()) {
      items(hours) { hour ->
        Box(
            modifier =
                Modifier.height(hourHeight)
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    .testTag("hourRow_$hour")) {
              Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.width(56.dp).padding(end = 4.dp),
                    contentAlignment = Alignment.CenterEnd) {
                      Text(
                          text = String.format(Locale.ROOT, "%02d:00", hour),
                          style = MaterialTheme.typography.bodySmall,
                          color = colorScheme.onSurface.copy(alpha = 0.6f),
                          modifier = Modifier.testTag("timeLabel_$hour"))
                    }

                Row(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                      for (column in 0 until numberOfColumns) {
                        Box(
                            modifier =
                                Modifier.weight(1f)
                                    .fillMaxHeight()
                                    .testTag("dayColumn_${column}_hour_$hour")) {
                              content(
                                  hour, column, Modifier.fillMaxSize().padding(horizontal = 2.dp))

                              if (showCurrentTimeLine) {
                                if ((numberOfColumns == 1 && hour == currentHour) ||
                                    (numberOfColumns > 1 &&
                                        column == todayIndex &&
                                        hour == currentHour)) {
                                  val offsetY = (currentMinute / 60f * hourHeight.value).dp
                                  Row(
                                      modifier =
                                          Modifier.offset(y = offsetY).fillMaxWidth().height(2.dp),
                                      verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier =
                                                Modifier.size(8.dp)
                                                    .background(colorScheme.primary, CircleShape)
                                                    .testTag("currentTimeIndicatorCircle"))
                                        Box(
                                            modifier =
                                                Modifier.weight(1f)
                                                    .height(2.dp)
                                                    .background(colorScheme.primary)
                                                    .testTag("currentTimeIndicatorLine"))
                                      }
                                }
                              }
                            }
                      }
                    }
              }
            }
      }
    }
  }
}