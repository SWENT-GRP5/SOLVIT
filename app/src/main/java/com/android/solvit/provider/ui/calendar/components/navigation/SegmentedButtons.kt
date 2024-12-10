package com.android.solvit.provider.ui.calendar.components.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.solvit.provider.model.CalendarView
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SegmentedButtons(
    currentView: CalendarView,
    onViewChange: (CalendarView) -> Unit,
    modifier: Modifier = Modifier
) {
  val views = CalendarView.entries
  val selectedIndex = views.indexOf(currentView)
  val totalButtons = views.size
  val transitionDuration = 300

  BoxWithConstraints(
      modifier =
          modifier
              .fillMaxWidth()
              .height(48.dp)
              .background(colorScheme.background, RoundedCornerShape(24.dp))
              .padding(2.dp)
              .testTag("segmentedButtonsBox")) {
        val buttonWidth = constraints.maxWidth / totalButtons.toFloat() / 3

        val animatedOffset by
            animateFloatAsState(
                targetValue = selectedIndex * buttonWidth * 3,
                animationSpec = tween(durationMillis = transitionDuration),
                label = "")

        Box(
            modifier =
                Modifier.fillMaxHeight()
                    .width(buttonWidth.dp)
                    .offset { IntOffset(x = animatedOffset.roundToInt(), y = 0) }
                    .background(color = colorScheme.primary, shape = RoundedCornerShape(24.dp))
                    .testTag("segmentedButtonIndicator"))

        Row(
            modifier = Modifier.fillMaxSize().testTag("segmentedButtonsRow"),
            horizontalArrangement = Arrangement.Center) {
              views.forEach { view ->
                val isSelected = currentView == view

                TextButton(
                    onClick = { onViewChange(view) },
                    modifier =
                        Modifier.weight(1f).fillMaxHeight().testTag("segmentedButton_${view.name}"),
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor =
                                if (isSelected) {
                                  colorScheme.onPrimary
                                } else {
                                  colorScheme.onBackground.copy(alpha = 0.6f)
                                },
                            containerColor = Color.Transparent),
                    elevation =
                        ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 0.dp, pressedElevation = 0.dp)) {
                      Text(
                          text = view.name.replaceFirstChar { it.uppercase(Locale.getDefault()) },
                          style =
                              MaterialTheme.typography.bodyMedium.copy(
                                  fontWeight =
                                      if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                          modifier = Modifier.testTag("segmentedButtonText_${view.name}"))
                    }
              }
            }
      }
}
