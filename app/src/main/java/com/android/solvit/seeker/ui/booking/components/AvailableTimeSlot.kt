package com.android.solvit.seeker.ui.booking.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AvailableTimeSlot(
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    onBook: (LocalDate, LocalTime) -> Unit,
    onCancel: () -> Unit,
    serviceColor: Color,
    modifier: Modifier = Modifier
) {
  var showConfirmation by remember { mutableStateOf(false) }
  var isBooked by remember { mutableStateOf(false) }

  val transition = updateTransition(showConfirmation, label = "confirmationTransition")

  // Animate background gradient
  val gradientAlpha by
      transition.animateFloat(label = "gradientAlpha", transitionSpec = { tween(500) }) { confirmed
        ->
        if (confirmed) 1f else 0f
      }

  // Animate button width
  val bookButtonWidth by
      transition.animateFloat(label = "bookButtonWidth", transitionSpec = { tween(300) }) {
          confirmed ->
        if (confirmed) 56f else 120f
      }

  // Animate confirm button position
  val confirmButtonOffset by
      transition.animateFloat(label = "confirmButtonOffset", transitionSpec = { tween(300) }) {
          confirmed ->
        if (confirmed) 64f else 0f
      }

  // Animate text color
  val textColor by
      transition.animateColor(label = "textColor", transitionSpec = { tween(500) }) { confirmed ->
        if (confirmed) Color.White else colorScheme.onSurface
      }

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .height(60.dp)
              .clip(RoundedCornerShape(12.dp))
              .drawBehind {
                // Draw animated gradient background
                drawRect(
                    brush =
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    serviceColor.copy(alpha = 0.1f + (0.8f * gradientAlpha)),
                                    serviceColor.copy(alpha = 0.05f + (0.05f * gradientAlpha)))))
              }
              .border(1.dp, serviceColor, RoundedCornerShape(12.dp))
              .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        // Time slot details
        Text(
            text =
                "${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = textColor)

        // Buttons
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(120.dp)) {
              // Cancel button
              AnimatedVisibility(
                  visible = showConfirmation,
                  enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                  exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)) {
                    Box(
                        modifier =
                            Modifier.width(56.dp)
                                .height(40.dp)
                                .background(colorScheme.error, RoundedCornerShape(8.dp))
                                .clickable {
                                  showConfirmation = false
                                  onCancel()
                                },
                        contentAlignment = Alignment.Center) {
                          Icon(
                              Icons.Default.Close,
                              contentDescription = "Cancel booking",
                              tint = Color.White)
                        }
                  }

              Spacer(modifier = Modifier.width(8.dp))

              // Book/Confirm button
              Box(
                  modifier =
                      Modifier.width(bookButtonWidth.dp)
                          .height(40.dp)
                          .background(
                              if (showConfirmation) colorScheme.secondary else serviceColor,
                              RoundedCornerShape(8.dp))
                          .clickable {
                            if (!showConfirmation) {
                              showConfirmation = true
                            } else {
                              isBooked = true
                              onBook(date, startTime)
                            }
                          },
                  contentAlignment = Alignment.Center) {
                    if (!showConfirmation) {
                      Text("Book", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    } else {
                      Icon(
                          Icons.Default.Check,
                          contentDescription = "Confirm booking",
                          tint = Color.White)
                    }
                  }
            }
      }
}
