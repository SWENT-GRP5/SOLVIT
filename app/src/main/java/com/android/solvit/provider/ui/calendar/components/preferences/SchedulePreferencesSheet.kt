package com.android.solvit.provider.ui.calendar.components.preferences

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePreferencesSheet(viewModel: ProviderCalendarViewModel, onDismiss: () -> Unit) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("Regular Hours", "Exceptions")
  var showError by remember { mutableStateOf<String?>(null) }

  // Clear error after delay
  LaunchedEffect(showError) {
    if (showError != null) {
      delay(3000)
      showError = null
    }
  }

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      containerColor = MaterialTheme.colorScheme.background,
      modifier = Modifier.fillMaxHeight().testTag("schedulePreferencesSheet"),
      windowInsets = WindowInsets(0)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          Text(
              text = "Schedule Preferences",
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.padding(vertical = 16.dp).testTag("schedulePreferencesTitle"))

          TabRow(
              selectedTabIndex = selectedTabIndex,
              containerColor = MaterialTheme.colorScheme.background) {
                tabs.forEachIndexed { index, title ->
                  Tab(
                      selected = selectedTabIndex == index,
                      onClick = { selectedTabIndex = index },
                      text = { Text(title) })
                }
              }

          Spacer(modifier = Modifier.height(16.dp))

          // Show error if present with a subtle design
          showError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(modifier = Modifier.height(8.dp))
          }

          when (selectedTabIndex) {
            0 -> RegularHoursTab(viewModel = viewModel, onError = { error -> showError = error })
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
  val provider by viewModel.currentProvider.collectAsStateWithLifecycle()
  var hasError by remember { mutableStateOf(false) }

  Column(
      modifier = Modifier.fillMaxWidth().testTag("regularHoursTab"),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (provider.uid.isEmpty()) {
          Text(
              "Loading provider information...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
              modifier = Modifier.padding(16.dp).testTag("loadingText"))
          return@Column
        }

        Text(
            "Set your regular working hours for each day of the week.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

        DayOfWeek.values().forEach { day ->
          val schedule = provider.schedule
          val currentSchedule = schedule.regularHours[day.name] ?: emptyList()
          var isExpanded by remember { mutableStateOf(false) }

          Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = MaterialTheme.shapes.medium,
              color = MaterialTheme.colorScheme.background,
              tonalElevation = 1.dp,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                DayScheduleCard(
                    day = day,
                    currentSchedule = currentSchedule,
                    isExpanded = isExpanded,
                    onExpandClick = { isExpanded = !isExpanded },
                    onSaveHours = { startTime, endTime ->
                      viewModel.setRegularHours(day.name, listOf(TimeSlot(startTime, endTime))) {
                          success ->
                        if (success) {
                          hasError = false
                          onError(null)
                        } else {
                          hasError = true
                          onError("Failed to set hours")
                        }
                      }
                    },
                    onClearHours = {
                      viewModel.clearRegularHours(day.name) { success ->
                        if (success) {
                          hasError = false
                          onError(null)
                        } else {
                          hasError = true
                          onError("Failed to clear hours")
                        }
                      }
                    },
                    hasError = hasError)
              }
        }
      }
}

@Composable
private fun ExceptionsTab(viewModel: ProviderCalendarViewModel, onError: (String?) -> Unit) {
  var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
  var showDatePicker by remember { mutableStateOf(false) }
  var isAddingOffTime by remember { mutableStateOf(true) }
  var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
  var endTime by remember { mutableStateOf(LocalTime.of(17, 0)) }
  var showSuccess by remember { mutableStateOf<String?>(null) }
  var isCreatorExpanded by remember { mutableStateOf(false) }

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
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
              modifier = Modifier.padding(16.dp).testTag("loadingText"))
          return@Column
        }

        // Add Exception Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
              Column(
                  modifier = Modifier.animateContentSize(),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable { isCreatorExpanded = !isCreatorExpanded }
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              "Add Exception",
                              style = MaterialTheme.typography.titleMedium,
                              color = MaterialTheme.colorScheme.onBackground,
                              modifier = Modifier.testTag("addExceptionTitle"))

                          Icon(
                              imageVector =
                                  if (isCreatorExpanded) Icons.Default.KeyboardArrowUp
                                  else Icons.Default.KeyboardArrowDown,
                              contentDescription = if (isCreatorExpanded) "Collapse" else "Expand",
                              tint = MaterialTheme.colorScheme.onBackground)
                        }

                    AnimatedVisibility(
                        visible = isCreatorExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()) {
                          Column(
                              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Exception Type Selection as segmented buttons
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .height(32.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant,
                                                MaterialTheme.shapes.small),
                                    horizontalArrangement = Arrangement.Center) {
                                      Surface(
                                          modifier = Modifier.weight(1f).fillMaxHeight(),
                                          color =
                                              if (isAddingOffTime)
                                                  MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                              else MaterialTheme.colorScheme.background,
                                          border =
                                              if (isAddingOffTime)
                                                  BorderStroke(
                                                      1.dp,
                                                      MaterialTheme.colorScheme.error.copy(
                                                          alpha = 0.5f))
                                              else null,
                                          onClick = { isAddingOffTime = true }) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center) {
                                                  Text(
                                                      "Off Time",
                                                      style = MaterialTheme.typography.labelMedium,
                                                      color =
                                                          if (isAddingOffTime)
                                                              MaterialTheme.colorScheme.error
                                                          else
                                                              MaterialTheme.colorScheme.onBackground
                                                                  .copy(alpha = 0.7f))
                                                }
                                          }
                                      Surface(
                                          modifier = Modifier.weight(1f).fillMaxHeight(),
                                          color =
                                              if (!isAddingOffTime)
                                                  MaterialTheme.colorScheme.primary.copy(
                                                      alpha = 0.1f)
                                              else MaterialTheme.colorScheme.background,
                                          border =
                                              if (!isAddingOffTime)
                                                  BorderStroke(
                                                      1.dp,
                                                      MaterialTheme.colorScheme.primary.copy(
                                                          alpha = 0.5f))
                                              else null,
                                          onClick = { isAddingOffTime = false }) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center) {
                                                  Text(
                                                      "Extra Time",
                                                      style = MaterialTheme.typography.labelMedium,
                                                      color =
                                                          if (!isAddingOffTime)
                                                              MaterialTheme.colorScheme.primary
                                                          else
                                                              MaterialTheme.colorScheme.onBackground
                                                                  .copy(alpha = 0.7f))
                                                }
                                          }
                                    }

                                // Date Selection with icon
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.fillMaxWidth().testTag("datePickerButton"),
                                    colors =
                                        ButtonDefaults.outlinedButtonColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.background)) {
                                      Row(
                                          modifier = Modifier.fillMaxWidth(),
                                          horizontalArrangement = Arrangement.SpaceBetween,
                                          verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text =
                                                    selectedDate?.format(
                                                        DateTimeFormatter.ofPattern(
                                                            "EEEE, MMM d, yyyy")) ?: "Select Date",
                                                color = MaterialTheme.colorScheme.onBackground)
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Select date",
                                                tint = MaterialTheme.colorScheme.primary)
                                          }
                                    }

                                // Time Selection
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                  Text(
                                      "Time Range",
                                      style = MaterialTheme.typography.bodyMedium,
                                      color =
                                          MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                                  Row(
                                      modifier = Modifier.fillMaxWidth(),
                                      horizontalArrangement = Arrangement.spacedBy(16.dp),
                                      verticalAlignment = Alignment.CenterVertically) {
                                        ScrollableTimePickerRow(
                                            selectedTime = startTime,
                                            onTimeSelected = { startTime = it },
                                            modifier = Modifier.weight(1f),
                                            testTagPrefix = "start")

                                        Text(
                                            "to",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = 0.7f))

                                        ScrollableTimePickerRow(
                                            selectedTime = endTime,
                                            onTimeSelected = { endTime = it },
                                            modifier = Modifier.weight(1f),
                                            testTagPrefix = "end")
                                      }
                                }

                                // Add Button
                                Button(
                                    onClick = {
                                      if (selectedDate != null) {
                                        val timeSlot = TimeSlot(startTime, endTime)
                                        if (isAddingOffTime) {
                                          viewModel.addOffTimeException(
                                              selectedDate!!, listOf(timeSlot)) { success, message
                                                ->
                                                if (success) {
                                                  showSuccess = message
                                                  onError(null)
                                                  selectedDate = null
                                                  startTime = LocalTime.of(9, 0)
                                                  endTime = LocalTime.of(17, 0)
                                                  isCreatorExpanded = false
                                                } else {
                                                  onError(message)
                                                }
                                              }
                                        } else {
                                          viewModel.addExtraTimeException(
                                              selectedDate!!, listOf(timeSlot)) { success, message
                                                ->
                                                if (success) {
                                                  showSuccess = message
                                                  onError(null)
                                                  selectedDate = null
                                                  startTime = LocalTime.of(9, 0)
                                                  endTime = LocalTime.of(17, 0)
                                                  isCreatorExpanded = false
                                                } else {
                                                  onError(message)
                                                }
                                              }
                                        }
                                      }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("addButton"),
                                    enabled = selectedDate != null && endTime.isAfter(startTime),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (isAddingOffTime) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.primary)) {
                                      Text(
                                          if (isAddingOffTime) "Add Off Time" else "Add Extra Time")
                                    }
                              }
                        }
                  }
            }

        // Existing Exceptions List
        AnimatedVisibility(visible = exceptions.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Existing Exceptions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp))

            exceptions.forEach { (date, exception) ->
              ExceptionCard(
                  date = date,
                  type = exception.type,
                  timeSlots = exception.timeSlots,
                  onDelete = {
                    viewModel.deleteException(date) { success, message ->
                      if (success) {
                        showSuccess = message
                        onError(null)
                      } else {
                        onError(message)
                      }
                    }
                  })
            }
          }
        }

        if (showDatePicker) {
          DatePickerDialog(
              onDismissRequest = { showDatePicker = false },
              onDateSelected = { date ->
                selectedDate = date.atTime(LocalTime.NOON)
                showDatePicker = false
              },
              selectedDate = selectedDate?.toLocalDate() ?: LocalDate.now())
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
              .animateContentSize()
              .testTag(
                  tag =
                      "exceptionCard_${date}_${type}_${timeSlots.first().startHour}_${timeSlots.first().startMinute}_${timeSlots.first().endHour}_${timeSlots.first().endMinute}"),
      color =
          when (type) {
            ExceptionType.OFF_TIME -> MaterialTheme.colorScheme.error
            ExceptionType.EXTRA_TIME -> MaterialTheme.colorScheme.primary
          }.copy(alpha = 0.05f),
      tonalElevation = 1.dp,
      border =
          BorderStroke(
              1.dp,
              when (type) {
                ExceptionType.OFF_TIME -> MaterialTheme.colorScheme.error
                ExceptionType.EXTRA_TIME -> MaterialTheme.colorScheme.primary
              }.copy(alpha = 0.3f)),
      shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Column {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        timeSlots.first().let {
                          "${it.startHour}:${it.startMinute.toString().padStart(2, '0')} - ${it.endHour}:${it.endMinute.toString().padStart(2, '0')}"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text(
                    text = if (type == ExceptionType.OFF_TIME) "Off Time" else "Extra Time",
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        when (type) {
                          ExceptionType.OFF_TIME -> MaterialTheme.colorScheme.error
                          ExceptionType.EXTRA_TIME -> MaterialTheme.colorScheme.primary
                        })
              }

              Surface(
                  onClick = onDelete,
                  modifier = Modifier.size(32.dp),
                  shape = MaterialTheme.shapes.small,
                  color = MaterialTheme.colorScheme.background,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Icon(
                          imageVector = Icons.Default.Clear,
                          contentDescription = "Delete",
                          modifier = Modifier.size(16.dp),
                          tint = MaterialTheme.colorScheme.error)
                    }
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
    onClearHours: () -> Unit,
    hasError: Boolean
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onExpandClick).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Column {
            Text(
                text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground)
            if (currentSchedule.isNotEmpty()) {
              Text(
                  text =
                      currentSchedule.first().let {
                        "${it.startHour}:${it.startMinute.toString().padStart(2, '0')} - ${it.endHour}:${it.endMinute.toString().padStart(2, '0')}"
                      },
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
          }

          Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically) {
                if (currentSchedule.isNotEmpty()) {
                  Surface(
                      onClick = onClearHours,
                      modifier = Modifier.size(32.dp),
                      shape = MaterialTheme.shapes.small,
                      color = MaterialTheme.colorScheme.background,
                      border =
                          BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center) {
                              Icon(
                                  imageVector = Icons.Default.Clear,
                                  contentDescription = "Clear hours",
                                  modifier = Modifier.size(16.dp),
                                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            }
                      }
                }

                Icon(
                    imageVector =
                        if (isExpanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onBackground)
              }
        }

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()) {
          Column(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
                var endTime by remember { mutableStateOf(LocalTime.of(17, 0)) }

                if (currentSchedule.isNotEmpty()) {
                  startTime =
                      LocalTime.of(
                          currentSchedule.first().startHour, currentSchedule.first().startMinute)
                  endTime =
                      LocalTime.of(
                          currentSchedule.first().endHour, currentSchedule.first().endMinute)
                }

                TimeSelectionSection(
                    startTime = startTime,
                    endTime = endTime,
                    onStartTimeSelected = { startTime = it },
                    onEndTimeSelected = { endTime = it },
                    day = day)

                Button(
                    onClick = { onSaveHours(startTime, endTime) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = endTime.isAfter(startTime)) {
                      Text("Save Hours")
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
