package com.android.solvit.provider.ui.calendar.components.container

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.android.solvit.provider.model.CalendarView
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableCalendarContainer(
    currentViewDate: LocalDate,
    onViewDateChanged: (LocalDate) -> Unit,
    calendarView: CalendarView,
    shouldAnimate: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    content: @Composable (LocalDate) -> Unit
) {
  var currentDate by remember { mutableStateOf(currentViewDate) }
  var swipeDirection by remember { mutableIntStateOf(0) }

  LaunchedEffect(currentViewDate, calendarView) {
    currentDate = currentViewDate
    swipeDirection = 0
  }

  Box(
      modifier =
          modifier
              .fillMaxSize()
              .pointerInput(calendarView) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                      if (abs(swipeDirection) > 50) {
                        val newDate =
                            when {
                              swipeDirection > 0 ->
                                  when (calendarView) {
                                    CalendarView.MONTH -> currentDate.minusMonths(1)
                                    CalendarView.WEEK -> currentDate.minusWeeks(1)
                                    CalendarView.DAY -> currentDate.minusDays(1)
                                  }
                              else ->
                                  when (calendarView) {
                                    CalendarView.MONTH -> currentDate.plusMonths(1)
                                    CalendarView.WEEK -> currentDate.plusWeeks(1)
                                    CalendarView.DAY -> currentDate.plusDays(1)
                                  }
                            }
                        onViewDateChanged(newDate)
                      }
                      swipeDirection = 0
                    },
                    onHorizontalDrag = { _, dragAmount ->
                      swipeDirection += dragAmount.roundToInt()
                    })
              }
              .testTag("swipeableCalendarContainer")) {
        AnimatedContent(
            targetState = currentDate,
            transitionSpec = {
              if (shouldAnimate) {
                val direction = if (targetState.isAfter(initialState)) 1 else -1
                val slideIn = slideInHorizontally { width -> direction * width }
                val slideOut = slideOutHorizontally { width -> -direction * width }
                (slideIn + fadeIn()).togetherWith(slideOut + fadeOut())
              } else {
                ContentTransform(EnterTransition.None, ExitTransition.None)
              }
            },
            modifier = Modifier.fillMaxSize(),
            label = "") { date ->
              content(date)
            }
      }
}
