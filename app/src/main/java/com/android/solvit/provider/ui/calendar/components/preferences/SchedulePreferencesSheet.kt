package com.android.solvit.provider.ui.calendar.components.preferences

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * A composable function that displays a modal bottom sheet for managing provider schedule
 * preferences, including regular working hours and time exceptions such as off-time and extra
 * working hours.
 *
 * @param viewModel ViewModel responsible for managing provider calendar data.
 * @param onDismiss Callback invoked when the sheet is dismissed.
 *
 * This function contains two primary tabs:
 * - **Regular Hours Tab:** Allows setting default working hours for each day of the week.
 * - **Exceptions Tab:** Allows managing specific date-based time exceptions, including off-time and
 *   extra hours.
 *
 * Both tabs provide intuitive interfaces for selecting times, viewing current settings, and adding
 * or removing time entries with appropriate error handling and feedback.
 */
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

/**
 * A composable function representing the "Regular Hours" tab for setting default working hours for
 * each day of the week.
 *
 * @param viewModel ViewModel responsible for managing calendar data.
 * @param onError Callback invoked when an error occurs while updating regular hours.
 *
 * The function displays each day of the week with current working hours. Users can add, edit, and
 * clear specific hours. Provides visual feedback on success or failure during data updates.
 */
@SuppressLint("MutableCollectionMutableState")
@Composable
private fun RegularHoursTab(viewModel: ProviderCalendarViewModel, onError: (String?) -> Unit) {
  val provider by viewModel.currentProvider.collectAsStateWithLifecycle()
  var hasError by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).testTag("regularHoursTab"),
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

        DayOfWeek.entries.forEach { day ->
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
                          isExpanded = false
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
                    })
              }
        }

        // Add bottom padding to ensure content is not cut off
        Spacer(modifier = Modifier.height(40.dp))
      }
}

/**
 * A composable function representing the "Exceptions" tab for managing date-based schedule
 * exceptions. Users can add off-time or extra working hours for specific dates.
 *
 * @param viewModel ViewModel responsible for managing provider schedule data.
 * @param onError Callback invoked when an error occurs while managing time exceptions.
 *
 * The function provides interfaces for selecting dates, time ranges, and specifying the exception
 * type (off-time or extra time). It also lists existing exceptions with delete functionality.
 */
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
                                .padding(16.dp)
                                .testTag("addButton"),
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

/**
 * A composable function that displays a day’s schedule card for managing working hours on a
 * specific day of the week.
 *
 * @param day The day of the week to be displayed.
 * @param currentSchedule The current working hours set for the day.
 * @param isExpanded Whether the card is expanded to allow editing the schedule.
 * @param onExpandClick Callback invoked when the card is clicked to expand or collapse.
 * @param onSaveHours Callback invoked when working hours are saved.
 * @param onClearHours Callback invoked when working hours are cleared.
 * @param hasError Indicates whether an error occurred while updating the schedule.
 *
 * This function supports viewing, editing, and clearing working hours with clear UI indicators.
 */
@Composable
fun DayScheduleCard(
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
                LocalTime.of(currentSchedule.first().startHour, currentSchedule.first().startMinute)
            else LocalTime.of(9, 0))
      }
  var endTime by
      remember(currentSchedule) {
        mutableStateOf(
            if (currentSchedule.isNotEmpty())
                LocalTime.of(currentSchedule.first().endHour, currentSchedule.first().endMinute)
            else LocalTime.of(17, 0))
      }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onExpandClick)
                .padding(16.dp)
                .testTag("day_schedule_card_${day.name}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Column {
            Text(
                text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("day_schedule_title_${day.name}"))
            if (currentSchedule.isEmpty()) {
              Text(
                  text = "No hours set",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                  modifier = Modifier.testTag("no_hours_text_${day.name}"))
            } else {
              Text(
                  text =
                      currentSchedule.first().let {
                        "${it.startHour}:${it.startMinute.toString().padStart(2, '0')} - ${it.endHour}:${it.endMinute.toString().padStart(2, '0')}"
                      },
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                  modifier = Modifier.testTag("day_schedule_hours_${day.name}"))
            }
          }

          Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically) {
                if (currentSchedule.isNotEmpty()) {
                  IconButton(
                      onClick = onClearHours,
                      modifier = Modifier.testTag("clear_hours_${day.name}"),
                      colors =
                          IconButtonDefaults.iconButtonColors(
                              contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear hours for ${day.name}",
                            tint = MaterialTheme.colorScheme.error)
                      }
                }

                Icon(
                    imageVector =
                        if (isExpanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("expand_icon_${day.name}"))
              }
        }

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()) {
          Column(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TimeSelectionSection(
                    startTime = startTime,
                    endTime = endTime,
                    onStartTimeSelected = { startTime = it },
                    onEndTimeSelected = { endTime = it },
                    day = day)

                Button(
                    onClick = { onSaveHours(startTime, endTime) },
                    modifier = Modifier.fillMaxWidth().testTag("save_hours_button_${day.name}"),
                    enabled = endTime.isAfter(startTime)) {
                      Text("Save Hours")
                    }
              }
        }
  }
}
/**
 * A composable function that displays a time selection UI with start and end time pickers for a
 * specific day of the week.
 *
 * @param startTime The currently selected start time.
 * @param endTime The currently selected end time.
 * @param onStartTimeSelected Callback invoked when the start time is updated.
 * @param onEndTimeSelected Callback invoked when the end time is updated.
 * @param modifier Modifier for customizing the layout.
 * @param day The day of the week for which time is being selected.
 *
 * The function ensures that time selection stays logically valid, preventing the end time from
 * being before the start time.
 */
@Composable
fun TimeSelectionSection(
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
                      if (newTime.isBefore(endTime) || newTime == endTime) {
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
                      if (newTime.isAfter(startTime) || newTime == startTime) {
                        onEndTimeSelected(newTime)
                      }
                    },
                    testTagPrefix = "time_picker_${day.name}_end")
              }
        }
  }
}

/**
 * A composable function that displays a summary card for a specific time exception entry with
 * options to delete the entry.
 *
 * @param date The date of the exception entry.
 * @param type The type of the exception (Off-Time or Extra-Time).
 * @param timeSlots A list of time slots associated with the exception entry.
 * @param onDelete Callback invoked when the delete action is triggered.
 *
 * The card shows detailed information about the exception, including its date, time range, and
 * type. It provides a delete button for removing the entry.
 */
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
                  modifier = Modifier.size(32.dp).testTag("deleteExceptionButton"),
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

private data class ExceptionEntry(val date: LocalDateTime, val exception: ScheduleException)
