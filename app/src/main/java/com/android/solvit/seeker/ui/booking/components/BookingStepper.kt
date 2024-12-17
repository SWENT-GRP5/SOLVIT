package com.android.solvit.seeker.ui.booking.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BookingStepper(
    currentStep: Int,
    onStepSelected: (Int) -> Unit,
    serviceColor: Color = colorScheme.primary,
    modifier: Modifier = Modifier
) {
  val steps =
      listOf(
          "Select Day" to "Choose a day for your appointment",
          "Choose Time" to "Pick an available time slot")

  Row(
      modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("bookingStepper"),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, (title, description) ->
          val stepNumber = index + 1
          val isActive = currentStep == stepNumber
          val isCompleted = currentStep > stepNumber

          // Animate colors based on state
          val backgroundColor by
              animateColorAsState(
                  targetValue =
                      when {
                        isActive -> serviceColor.copy(alpha = 0.15f)
                        isCompleted -> Color.White
                        else -> Color.White.copy(alpha = 0.7f)
                      },
                  label = "stepBackgroundColor")

          val contentColor by
              animateColorAsState(
                  targetValue =
                      when {
                        isActive -> serviceColor
                        isCompleted -> colorScheme.surfaceVariant
                        else -> colorScheme.surfaceVariant.copy(alpha = 0.7f)
                      },
                  label = "stepContentColor")

          Box(
              modifier =
                  Modifier.weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .background(backgroundColor)
                      .clickable(enabled = !isActive) { onStepSelected(stepNumber) }
                      .padding(12.dp)
                      .testTag("bookingStep_$stepNumber")) {
                Column(horizontalAlignment = Alignment.Start) {
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth()) {
                        // Step number circle
                        Box(
                            modifier =
                                Modifier.size(24.dp)
                                    .background(
                                        when {
                                          isActive -> serviceColor
                                          isCompleted -> colorScheme.surfaceVariant
                                          else -> colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                        },
                                        CircleShape),
                            contentAlignment = Alignment.Center) {
                              Text(
                                  text = stepNumber.toString(),
                                  style = MaterialTheme.typography.bodyMedium,
                                  color =
                                      when {
                                        isActive -> Color.White
                                        else -> colorScheme.background
                                      },
                                  fontWeight = FontWeight.Bold)
                            }

                        // Step title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color =
                                when {
                                  isActive -> serviceColor
                                  else -> colorScheme.onSurfaceVariant
                                },
                            fontWeight = FontWeight.Medium)
                      }

                  // Step description
                  if (isActive) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = serviceColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp))
                  }
                }
              }

          // Connector line between steps
          if (index < steps.size - 1) {
            Spacer(
                modifier =
                    Modifier.height(1.dp)
                        .width(24.dp)
                        .background(
                            if (isCompleted) serviceColor.copy(alpha = 0.3f)
                            else colorScheme.surfaceVariant.copy(alpha = 0.3f)))
          }
        }
      }
}
