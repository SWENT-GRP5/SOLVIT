package com.android.solvit.provider.ui.calendar.components.preferences

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.provider.ui.calendar.components.dialog.DatePickerDialog
import com.android.solvit.shared.model.provider.ExceptionType
import com.android.solvit.shared.model.provider.ScheduleException
import com.android.solvit.shared.model.provider.TimeSlot
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePreferencesSheet(viewModel: ProviderCalendarViewModel, onDismiss: () -> Unit) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("Regular Hours", "Exceptions")
  var showError by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      modifier = Modifier.fillMaxHeight().testTag("schedulePreferencesSheet"),
      windowInsets = WindowInsets(0)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          Text(
              text = "Schedule Preferences",
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(vertical = 16.dp).testTag("schedulePreferencesTitle"))

          TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
              Tab(
                  selected = selectedTabIndex == index,
                  onClick = { selectedTabIndex = index },
                  text = { Text(title) })
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Show error if present
          showError?.let { error ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                  Text(
                      text = error,
                      color = MaterialTheme.colorScheme.onErrorContainer,
                      modifier = Modifier.padding(8.dp))
                }
            Spacer(modifier = Modifier.height(8.dp))
          }

          when (selectedTabIndex) {
            0 -> RegularHoursTab(viewModel = viewModel, onError = { showError = it })
            1 -> ExceptionsTab(viewModel = viewModel, onError = { showError = it })
          }

          // Add bottom padding to ensure content is not cut off
          Spacer(modifier = Modifier.height(80.dp))
        }
      }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
private fun RegularHoursTab(viewModel: ProviderCalendarViewModel, onError: (String?) -> Unit) {
  var expandedDay by remember { mutableStateOf<DayOfWeek?>(null) }
  var showSuccess by remember { mutableStateOf<String?>(null) }
  var currentSchedule by remember {
    mutableStateOf(viewModel.currentProvider.value.schedule.regularHours)
  }

  val provider by viewModel.currentProvider.collectAsStateWithLifecycle()

  // Update currentSchedule when provider changes
  LaunchedEffect(provider) { currentSchedule = provider.schedule.regularHours }

  // Clear success message after delay
  LaunchedEffect(showSuccess) {
    if (showSuccess != null) {
      delay(3000)
      showSuccess = null
    }
  }

  Column(modifier = Modifier.fillMaxWidth().testTag("regularHoursTab")) {
    showSuccess?.let { message ->
      Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth().testTag("successMessage")) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp).testTag("successMessageText"))
          }
    }

    DayOfWeek.entries.forEach { day ->
      DayScheduleCard(
          day = day,
          currentSchedule = currentSchedule[day.name] ?: emptyList(),
          isExpanded = expandedDay == day,
          onExpandClick = { expandedDay = if (expandedDay == day) null else day },
          onSaveHours = { startTime, endTime ->
            val timeSlot = TimeSlot(startTime.hour, startTime.minute, endTime.hour, endTime.minute)
            viewModel.setRegularHours(day.name, listOf(timeSlot)) { success ->
              if (success) {
                showSuccess = "Regular hours updated successfully"
                onError(null)
                // Update UI immediately
                currentSchedule =
                    currentSchedule.toMutableMap().apply {
                      put(day.name, listOf(timeSlot).toMutableList())
                    }
                expandedDay = null
              } else {
                onError("Failed to update regular hours")
              }
            }
          },
          onClearHours = {
            viewModel.clearRegularHours(day.name) { success ->
              if (success) {
                showSuccess = "Regular hours cleared successfully"
                onError(null)
                // Update UI immediately
                currentSchedule = currentSchedule.toMutableMap().apply { remove(day.name) }
              } else {
                onError("Failed to clear regular hours")
              }
            }
          })
    }
  }
}

@Composable
private fun ExceptionsTab(viewModel: ProviderCalendarViewModel, onError: (String?) -> Unit) {
  var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var isAddingOffTime by remember { mutableStateOf(true) }
  var startTime by remember { mutableStateOf<LocalTime?>(null) }
  var endTime by remember { mutableStateOf<LocalTime?>(null) }
  var timePickerMode by remember { mutableStateOf(TimePickerMode.START) }
  var showSuccess by remember { mutableStateOf<String?>(null) }

  val provider by viewModel.currentProvider.collectAsStateWithLifecycle()

  // Convert exceptions map to sorted list
  val exceptions =
      provider.schedule.exceptions
          .map { exception -> ExceptionEntry(exception.date, exception) }
          .sortedBy { it.date }

  // Clear success message after delay
  LaunchedEffect(showSuccess) {
    if (showSuccess != null) {
      delay(3000)
      showSuccess = null
    }
  }

  Column(
      modifier = Modifier.fillMaxWidth().testTag("exceptionsTab"),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (provider.uid.isEmpty()) {
          Text(
              "Loading provider information...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(16.dp).testTag("loadingText"))
          return
        }

        // Success message
        showSuccess?.let { message ->
          Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.fillMaxWidth().testTag("successMessage")) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp).testTag("successText"))
              }
        }

        Row(
            modifier = Modifier.fillMaxWidth().testTag("buttonsRow"),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              FilledTonalButton(
                  onClick = {
                    isAddingOffTime = true
                    showDatePicker = true
                  },
                  modifier = Modifier.weight(1f).testTag("addOffTimeButton"),
                  colors =
                      ButtonDefaults.filledTonalButtonColors(
                          containerColor = MaterialTheme.colorScheme.errorContainer,
                          contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
                    Text("Add Off Time")
                  }
              FilledTonalButton(
                  onClick = {
                    isAddingOffTime = false
                    showDatePicker = true
                  },
                  modifier = Modifier.weight(1f).testTag("addExtraTimeButton"),
                  colors =
                      ButtonDefaults.filledTonalButtonColors(
                          containerColor = MaterialTheme.colorScheme.primaryContainer,
                          contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                    Text("Add Extra Time")
                  }
            }

        // Show existing exceptions
        if (exceptions.isEmpty()) {
          Surface(
              modifier = Modifier.fillMaxWidth().testTag("noExceptionsSurface"),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = MaterialTheme.shapes.medium) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp).testTag("noExceptionsBox"),
                    contentAlignment = Alignment.Center) {
                      Text(
                          "No exceptions scheduled",
                          style = MaterialTheme.typography.bodyLarge,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.testTag("noExceptionsText"))
                    }
              }
        } else {
          LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f).testTag("exceptionsList")) {
                items(exceptions) { exception ->
                  ExceptionCard(
                      date = exception.date,
                      type = exception.exception.type,
                      timeSlots = exception.exception.timeSlots,
                      onDelete = {
                        viewModel.deleteException(exception.date) { success ->
                          if (success) {
                            showSuccess = "Exception removed successfully"
                            onError(null)
                          } else {
                            onError("Failed to remove exception")
                          }
                        }
                      })
                }
              }
        }
      }

  if (showDatePicker) {
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        onDateSelected = { date ->
          selectedDate = LocalDateTime.of(date, LocalTime.MIN)
          showDatePicker = false
          showTimePicker = true
          timePickerMode = TimePickerMode.START
        },
        selectedDate = LocalDate.now())
  }

  if (showTimePicker && selectedDate != null) {
    val timePickerTitle =
        when (timePickerMode) {
          TimePickerMode.START -> "Select Start Time"
          TimePickerMode.END -> "Select End Time"
        }

    TimePickerDialog(
        onDismiss = {
          showTimePicker = false
          selectedDate = null
          startTime = null
          endTime = null
        },
        onTimeSelected = { time ->
          when (timePickerMode) {
            TimePickerMode.START -> {
              startTime = time
              timePickerMode = TimePickerMode.END
            }
            TimePickerMode.END -> {
              try {
                val timeSlot =
                    TimeSlot(
                        startHour = startTime!!.hour,
                        startMinute = startTime!!.minute,
                        endHour = time.hour,
                        endMinute = time.minute)
                endTime = time
                if (isAddingOffTime) {
                  viewModel.addOffTimeException(selectedDate!!, listOf(timeSlot)) { success, message
                    ->
                    if (success) {
                      showSuccess = message
                      onError(null)
                    } else {
                      onError(message)
                    }
                    showTimePicker = false
                    selectedDate = null
                    startTime = null
                    endTime = null
                  }
                } else {
                  viewModel.addExtraTimeException(selectedDate!!, listOf(timeSlot)) {
                      success,
                      message ->
                    if (success) {
                      showSuccess = message
                      onError(null)
                    } else {
                      onError(message)
                    }
                    showTimePicker = false
                    selectedDate = null
                    startTime = null
                    endTime = null
                  }
                }
              } catch (e: IllegalArgumentException) {
                onError(e.message ?: "Invalid time selection")
                endTime = null
                timePickerMode = TimePickerMode.END
              }
            }
          }
        },
        initialTime =
            when (timePickerMode) {
              TimePickerMode.START -> LocalTime.of(9, 0)
              TimePickerMode.END -> startTime?.plusHours(1) ?: LocalTime.of(17, 0)
            },
        title = timePickerTitle)
  }
}

@Composable
private fun ExceptionCard(
    date: LocalDateTime,
    type: ExceptionType,
    timeSlots: List<TimeSlot>,
    onDelete: () -> Unit
) {
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .testTag(
                  tag =
                      "exceptionCard_${date}_${type}_${timeSlots.first().startHour}_${timeSlots.first().startMinute}_${timeSlots.first().endHour}_${timeSlots.first().endMinute}"),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Column {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        timeSlots.first().let {
                          "${it.startHour}:${it.startMinute.toString().padStart(2, '0')} - ${it.endHour}:${it.endMinute.toString().padStart(2, '0')}"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (type == ExceptionType.OFF_TIME) "Off Time" else "Extra Time",
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (type == ExceptionType.OFF_TIME) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary)
              }

              IconButton(
                  onClick = onDelete,
                  modifier =
                      Modifier.size(32.dp)
                          .testTag(
                              "deleteExceptionButton_${date}_${type}_${timeSlots.first().startHour}_${timeSlots.first().startMinute}_${timeSlots.first().endHour}_${timeSlots.first().endMinute}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                  }
            }
      }
}

@Composable
private fun DayScheduleCard(
    day: DayOfWeek,
    currentSchedule: List<TimeSlot>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onSaveHours: (LocalTime, LocalTime) -> Unit,
    onClearHours: () -> Unit
) {
  var startTime by
      remember(currentSchedule) {
        mutableStateOf(
            if (currentSchedule.isNotEmpty())
                LocalTime.of(currentSchedule[0].startHour, currentSchedule[0].startMinute)
            else LocalTime.of(9, 0))
      }
  var endTime by
      remember(currentSchedule) {
        mutableStateOf(
            if (currentSchedule.isNotEmpty())
                LocalTime.of(currentSchedule[0].endHour, currentSchedule[0].endMinute)
            else LocalTime.of(17, 0))
      }
  val isValidTimeRange = endTime.isAfter(startTime)

  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 4.dp)
              .testTag("dayScheduleCard_${day.name}")
              .animateContentSize(
                  animationSpec =
                      spring(
                          dampingRatio = Spring.DampingRatioMediumBouncy,
                          stiffness = Spring.StiffnessLow))
              .testTag("dayScheduleCard_${day.name}"),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth().clickable { onExpandClick() },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text =
                        day.name.lowercase().replaceFirstChar {
                          if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                    style = MaterialTheme.typography.titleMedium)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      if (!isExpanded && currentSchedule.isNotEmpty()) {
                        Text(
                            text =
                                "${currentSchedule.first().startHour}:${currentSchedule.first().startMinute.toString().padStart(2, '0')} - ${currentSchedule.first().endHour}:${currentSchedule.first().endMinute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(
                            modifier = Modifier.testTag("clearButton_${day.name}"),
                            onClick = onClearHours,
                            colors =
                                ButtonDefaults.textButtonColors(
                                    contentColor =
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f))) {
                              Text("Clear", style = MaterialTheme.typography.bodySmall)
                            }
                      }
                      IconButton(
                          onClick = onExpandClick,
                          modifier = Modifier.testTag("expandButton_${day.name}")) {
                            Icon(
                                imageVector =
                                    if (isExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand")
                          }
                    }
              }

          AnimatedVisibility(
              visible = isExpanded,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut()) {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("time_picker_${day.name}"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                      TimeSelectionSection(
                          startTime = startTime,
                          endTime = endTime,
                          onStartTimeSelected = { newStartTime -> startTime = newStartTime },
                          onEndTimeSelected = { newEndTime -> endTime = newEndTime },
                          modifier = Modifier.testTag("time_picker_${day.name}_section"),
                          day = day)

                      if (!isValidTimeRange) {
                        Text(
                            text = "End time must be after start time",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier =
                                Modifier.padding(top = 8.dp).testTag("timeError_${day.name}"))
                      }

                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.End) {
                            Box(modifier = Modifier.alpha(if (isValidTimeRange) 1f else 0.5f)) {
                              Button(
                                  onClick = {
                                    if (isValidTimeRange) onSaveHours(startTime, endTime)
                                  },
                                  modifier = Modifier.testTag("saveButton_${day.name}"),
                                  enabled = isValidTimeRange) {
                                    Text("Save")
                                  }
                            }
                          }
                    }
              }
        }
      }
}

@Composable
private fun TimeSelectionSection(
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    day: DayOfWeek
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Column(
              modifier = Modifier.weight(1f).testTag("time_picker_${day.name}_start"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Start Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("time_picker_${day.name}_start_label"))
                ScrollableTimePickerRow(
                    selectedTime = startTime,
                    onTimeSelected = { newTime ->
                      // Ensure start time is not after end time
                      if (newTime.isBefore(endTime) || newTime.equals(endTime)) {
                        onStartTimeSelected(newTime)
                      }
                    },
                    testTagPrefix = "time_picker_${day.name}_start")
              }

          Column(
              modifier = Modifier.weight(1f).testTag("time_picker_${day.name}_end"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "End Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("time_picker_${day.name}_end_label"))
                ScrollableTimePickerRow(
                    selectedTime = endTime,
                    onTimeSelected = { newTime ->
                      // Ensure end time is not before start time
                      if (newTime.isAfter(startTime) || newTime.equals(startTime)) {
                        onEndTimeSelected(newTime)
                      }
                    },
                    testTagPrefix = "time_picker_${day.name}_end")
              }
        }
  }
}

private data class ExceptionEntry(val date: LocalDateTime, val exception: ScheduleException)

private enum class TimePickerMode {
  START,
  END
}
