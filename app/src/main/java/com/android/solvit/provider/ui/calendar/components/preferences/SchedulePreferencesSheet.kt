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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePreferencesSheet(viewModel: ProviderCalendarViewModel, onDismiss: () -> Unit) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("Regular Hours", "Exceptions")
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      modifier = Modifier.fillMaxHeight().testTag("schedulePreferencesSheet"),
      windowInsets = WindowInsets(0)) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    text = { Text(title) },
                    modifier = Modifier.testTag("schedulePreferencesTab_$title"))
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
              0 -> RegularHoursTab(
                  viewModel = viewModel,
                  onError = { error ->
                    scope.launch {
                      snackbarHostState.showSnackbar(
                          message = error,
                          duration = SnackbarDuration.Short,
                          withDismissAction = true
                      )
                    }
                  })
              1 -> ExceptionsTab(
                  viewModel = viewModel,
                  onError = { error ->
                    scope.launch {
                      snackbarHostState.showSnackbar(
                          message = error,
                          duration = SnackbarDuration.Short,
                          withDismissAction = true
                      )
                    }
                  })
            }

            Spacer(modifier = Modifier.height(80.dp))
          }

          // Place SnackbarHost above everything else
          Box(
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
          ) {
              SnackbarHost(
                  hostState = snackbarHostState,
                  modifier = Modifier.testTag("schedulePreferencesSnackbar"),
                  snackbar = { data ->
                      Snackbar(
                          modifier = Modifier.padding(horizontal = 16.dp),
                          action = {
                              IconButton(onClick = { data.dismiss() }) {
                                  Icon(
                                      imageVector = Icons.Default.Close,
                                      contentDescription = "Dismiss",
                                      tint = MaterialTheme.colorScheme.onSurface
                                  )
                              }
                          }
                      ) {
                          Text(data.visuals.message)
                      }
                  }
              )
          }
        }
      }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
private fun RegularHoursTab(
    viewModel: ProviderCalendarViewModel,
    onError: (String) -> Unit
) {
  val provider by viewModel.currentProvider.collectAsStateWithLifecycle()
  var expandedDay by remember { mutableStateOf<DayOfWeek?>(null) }

  if (provider.uid.isEmpty()) {
    Text(
        "Loading provider information...",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag("loadingText"))
    return
  }

  LazyColumn(
      modifier = Modifier.fillMaxWidth().testTag("regularHoursTab"),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DayOfWeek.values()) { day ->
          val currentSchedule = provider.schedule.regularHours[day.name] ?: emptyList()
          DayScheduleCard(
              day = day,
              currentSchedule = currentSchedule,
              isExpanded = expandedDay == day,
              onExpandClick = { expandedDay = if (expandedDay == day) null else day },
              onSaveHours = { startTime, endTime ->
                try {
                  val timeSlot = TimeSlot(startTime, endTime)
                  viewModel.setRegularHours(day.name, listOf(timeSlot)) { success ->
                    if (!success) {
                      onError("Failed to save hours for ${day.name.lowercase().capitalize()}")
                    } else {
                      expandedDay = null
                    }
                  }
                } catch (e: IllegalArgumentException) {
                  onError(e.message ?: "Invalid time selection")
                }
              },
              onClearHours = {
                viewModel.clearRegularHours(day.name) { success ->
                  if (!success) {
                    onError("Failed to clear hours for ${day.name.lowercase().capitalize()}")
                  }
                }
              })
        }
      }
}

@Composable
private fun ExceptionsTab(
    viewModel: ProviderCalendarViewModel,
    onError: (String) -> Unit
) {
  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  var isAddingOffTime by remember { mutableStateOf(true) }
  var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
  var endTime by remember { mutableStateOf(LocalTime.of(17, 0)) }

  val provider by viewModel.currentProvider.collectAsStateWithLifecycle()

  // Convert exceptions map to sorted list
  val exceptions =
      provider.schedule.exceptions
          .map { exception -> ExceptionEntry(exception.date, exception) }
          .sortedBy { it.date }

  Column(
      modifier = Modifier.fillMaxWidth().testTag("exceptionsTab"),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (provider.uid.isEmpty()) {
          Text(
              "Loading provider information...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.testTag("loadingText"))
          return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                  onClick = {
                    isAddingOffTime = true
                    showDatePicker = true
                  },
                  modifier = Modifier.weight(1f).testTag("addOffTimeButton")) {
                    Text("Add Off Time")
                  }
              Button(
                  onClick = {
                    isAddingOffTime = false
                    showDatePicker = true
                  },
                  modifier = Modifier.weight(1f).testTag("addExtraTimeButton")) {
                    Text("Add Extra Time")
                  }
        }

        // Show time selection if date is selected
        if (selectedDate != null) {
          Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Select Time for ${selectedDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    style = MaterialTheme.typography.titleMedium)

                TimeSelectionSection(
                    startTime = startTime,
                    endTime = endTime,
                    onStartTimeSelected = { newTime ->
                      if (selectedDate?.isEqual(LocalDate.now()) == true &&
                          newTime.isBefore(LocalTime.now())) {
                        onError("Cannot select a time in the past")
                        return@TimeSelectionSection
                      }
                      startTime = newTime
                    },
                    onEndTimeSelected = { newTime ->
                      endTime = newTime
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                      TextButton(
                          onClick = {
                            selectedDate = null
                            startTime = LocalTime.of(9, 0)
                            endTime = LocalTime.of(17, 0)
                          }) {
                            Text("Cancel")
                          }
                      Button(
                          onClick = {
                            if (selectedDate?.isEqual(LocalDate.now()) == true &&
                                startTime.isBefore(LocalTime.now())) {
                              onError("Cannot select a time in the past")
                              return@Button
                            }

                            try {
                              val timeSlot = TimeSlot(startTime, endTime)
                              val dateTime = selectedDate!!.atStartOfDay()

                              if (isAddingOffTime) {
                                viewModel.addOffTimeException(
                                    dateTime,
                                    listOf(timeSlot)
                                ) { success, message ->
                                  if (!success) {
                                    onError(message)
                                  } else {
                                    selectedDate = null
                                    startTime = LocalTime.of(9, 0)
                                    endTime = LocalTime.of(17, 0)
                                  }
                                }
                              } else {
                                viewModel.addExtraTimeException(
                                    dateTime,
                                    listOf(timeSlot)
                                ) { success, message ->
                                  if (!success) {
                                    onError(message)
                                  } else {
                                    selectedDate = null
                                    startTime = LocalTime.of(9, 0)
                                    endTime = LocalTime.of(17, 0)
                                  }
                                }
                              }
                            } catch (e: IllegalArgumentException) {
                              onError(e.message ?: "Invalid time selection")
                            }
                          },
                          modifier = Modifier.testTag("addExceptionButton")) {
                            Text("Add ${if (isAddingOffTime) "Off Time" else "Extra Time"}")
                          }
                    }
              }
        }

        // Exceptions list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(
                  items = exceptions,
                  key = { "${it.date}_${it.exception.type}" }) { exceptionEntry ->
                    ExceptionCard(
                        date = exceptionEntry.date,
                        type = exceptionEntry.exception.type,
                        timeSlots = exceptionEntry.exception.timeSlots,
                        onDelete = {
                          viewModel.deleteException(exceptionEntry.date) { success ->
                            if (!success) {
                              onError("Failed to delete exception")
                            }
                          }
                        })
                  }
            }

        if (showDatePicker) {
          DatePickerDialog(
              selectedDate = selectedDate ?: LocalDate.now(),
              onDismissRequest = {
                showDatePicker = false
                selectedDate = null
              },
              onDateSelected = { date ->
                if (date.isBefore(LocalDate.now())) {
                  onError("Cannot select a date in the past")
                  return@DatePickerDialog
                }
                selectedDate = date
                showDatePicker = false
              })
        }
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
    day: DayOfWeek? = null
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Column(
              modifier = Modifier.weight(1f).testTag(
                  if (day != null) "time_picker_${day.name}_start" else "time_picker_start"
              ),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Start Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(
                        if (day != null) "time_picker_${day.name}_start_label" else "time_picker_start_label"
                    ))
                ScrollableTimePickerRow(
                    selectedTime = startTime,
                    onTimeSelected = { newTime ->
                      // Ensure start time is not after end time
                      if (newTime.isBefore(endTime) || newTime.equals(endTime)) {
                        onStartTimeSelected(newTime)
                      }
                    },
                    testTagPrefix = if (day != null) "time_picker_${day.name}_start" else "time_picker_start")
              }

          Column(
              modifier = Modifier.weight(1f).testTag(
                  if (day != null) "time_picker_${day.name}_end" else "time_picker_end"
              ),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "End Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(
                        if (day != null) "time_picker_${day.name}_end_label" else "time_picker_end_label"
                    ))
                ScrollableTimePickerRow(
                    selectedTime = endTime,
                    onTimeSelected = { newTime ->
                      // Ensure end time is not before start time
                      if (newTime.isAfter(startTime) || newTime.equals(startTime)) {
                        onEndTimeSelected(newTime)
                      }
                    },
                    testTagPrefix = if (day != null) "time_picker_${day.name}_end" else "time_picker_end")
              }
        }
  }
}

private data class ExceptionEntry(val date: LocalDateTime, val exception: ScheduleException)

private enum class TimePickerMode {
  START,
  END
}
