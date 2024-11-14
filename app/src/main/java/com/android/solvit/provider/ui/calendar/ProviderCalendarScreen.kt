package com.android.solvit.provider.ui.calendar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderCalendarScreen(navigationActions: NavigationActions) {
  var selectedDate by remember { mutableStateOf(LocalDate.now()) }
  var currentViewDate by remember { mutableStateOf(LocalDate.now()) }
  var calendarView by remember { mutableStateOf(CalendarView.MONTH) }
  var showBottomSheet by remember { mutableStateOf(false) }
  var showDatePicker by remember { mutableStateOf(false) }

  // Lift the time slots state to this level
  var timeSlots by remember { mutableStateOf(generateInitialTimeSlots()) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "My Calendar",
                  fontFamily = FontFamily.Default,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.ExtraBold,
                  lineHeight = 24.sp,
                  textAlign = TextAlign.Left,
                  color = colorScheme.onBackground,
                  modifier = Modifier.testTag("calendarTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground)
                  }
            },
            actions = {
              IconButton(
                  onClick = { /* TODO: Implement menu action */},
                  modifier = Modifier.testTag("menuButton")) {
                    Icon(
                        Icons.Default.Menu, contentDescription = "Menu", tint = colorScheme.primary)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background))
      },
      containerColor = colorScheme.background) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("calendarColumn")) {
          CalendarViewToggle(calendarView) { newView ->
            calendarView = newView
            showBottomSheet = false
          }

          when (calendarView) {
            CalendarView.MONTH ->
                MonthView(
                    currentViewDate = currentViewDate,
                    selectedDate = selectedDate,
                    onDateSelected = {
                      selectedDate = it
                      showBottomSheet = true
                    },
                    onViewDateChanged = { currentViewDate = it },
                    onHeaderClick = { showDatePicker = true },
                    timeSlots = timeSlots)
            CalendarView.WEEK ->
                WeekView(
                    currentViewDate = currentViewDate,
                    selectedDate = selectedDate,
                    onDateSelected = {
                      selectedDate = it
                      showBottomSheet = true
                    },
                    onViewDateChanged = { currentViewDate = it },
                    onHeaderClick = { showDatePicker = true },
                    timeSlots = timeSlots,
                    onTimeSlotsChanged = { date, newTimeSlots ->
                      timeSlots = timeSlots.toMutableMap().apply { put(date, newTimeSlots) }
                    })
            CalendarView.DAY ->
                DayView(
                    currentViewDate = currentViewDate,
                    onViewDateChanged = { currentViewDate = it },
                    onHeaderClick = { showDatePicker = true },
                    timeSlots = timeSlots,
                    onTimeSlotsChanged = { date, newTimeSlots ->
                      timeSlots = timeSlots.toMutableMap().apply { put(date, newTimeSlots) }
                    })
          }
        }
      }

  if (showBottomSheet && calendarView != CalendarView.DAY) {
    ModalBottomSheet(
        onDismissRequest = { showBottomSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.testTag("bottomSheetDayView")) {
          DayView(
              currentViewDate = selectedDate,
              onViewDateChanged = { currentViewDate = it },
              onHeaderClick = { showDatePicker = true },
              timeSlots = timeSlots,
              onTimeSlotsChanged = { date, newTimeSlots ->
                timeSlots = timeSlots.toMutableMap().apply { put(date, newTimeSlots) }
              })
        }
  }

  if (showDatePicker) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
              onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                  val newDate =
                      Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                  selectedDate = newDate
                  currentViewDate = newDate
                  showDatePicker = false
                  // Open bottom sheet if in month or week view
                  if (calendarView == CalendarView.MONTH || calendarView == CalendarView.WEEK) {
                    showBottomSheet = true
                  }
                }
              },
              modifier = Modifier.testTag("confirmDateButton")) {
                Text("OK", color = colorScheme.primary)
              }
        },
        dismissButton = {
          TextButton(
              onClick = { showDatePicker = false },
              modifier = Modifier.testTag("cancelDateButton")) {
                Text("Cancel", color = colorScheme.primary)
              }
        },
        modifier = Modifier.testTag("datePickerDialog"),
        colors =
            DatePickerDefaults.colors(
                containerColor = colorScheme.surface,
                selectedDayContainerColor = colorScheme.primary,
                todayContentColor = colorScheme.primary,
                todayDateBorderColor = colorScheme.primary,
                selectedYearContainerColor = colorScheme.primary,
                currentYearContentColor = colorScheme.primary,
                dayContentColor = colorScheme.primary,
                selectedDayContentColor = colorScheme.onPrimary,
                weekdayContentColor = colorScheme.primary)) {
          DatePicker(state = datePickerState)
        }
  }
}

@Composable
fun CalendarViewToggle(currentView: CalendarView, onViewChange: (CalendarView) -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("calendarViewToggle"),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        CalendarView.entries.forEach { view ->
          Button(
              onClick = { onViewChange(view) },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor =
                          if (currentView == view) colorScheme.primary
                          else colorScheme.surfaceVariant),
              modifier = Modifier.testTag("toggleButton_${view.name.lowercase()}")) {
                Text(
                    view.name,
                    color =
                        if (currentView == view) colorScheme.onPrimary else colorScheme.onSurface)
              }
        }
      }
}

@Composable
fun MonthView(
    currentViewDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onViewDateChanged: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<TimeSlot>>
) {
  var offsetX by remember { mutableFloatStateOf(0f) }
  var currentMonth by remember(currentViewDate) { mutableStateOf(YearMonth.from(currentViewDate)) }
  var isDragging by remember { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.fillMaxSize().padding(16.dp).pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onHorizontalDrag = { change, dragAmount ->
                  change.consume()
                  if (isDragging) {
                    offsetX += dragAmount

                    if (abs(offsetX) > 100) {
                      currentMonth =
                          if (offsetX > 0) {
                            currentMonth.minusMonths(1)
                          } else {
                            currentMonth.plusMonths(1)
                          }
                      onViewDateChanged(currentMonth.atDay(1))
                      offsetX = 0f
                      isDragging = false
                    }
                  }
                })
          }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                  modifier =
                      Modifier.clickable(onClick = onHeaderClick)
                          .padding(vertical = 8.dp)
                          .testTag("monthHeader"),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = colorScheme.onSurface)
            }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().weight(1f)) {
              // Weekday headers
              items(7) { dayIndex ->
                Text(
                    text =
                        DayOfWeek.of(dayIndex + 1)
                            .getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurface)
              }

              // Calendar days
              val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
              repeat(firstDayOfWeek - 1) { item { Spacer(modifier = Modifier.aspectRatio(1f)) } }

              items(currentMonth.lengthOfMonth()) { dayOfMonth ->
                val date = currentMonth.atDay(dayOfMonth + 1)
                DayItem(
                    date = date,
                    isSelected = date == selectedDate,
                    isCurrentDay = date == LocalDate.now(),
                    onDateSelected = onDateSelected,
                    dayStatus = calculateDayStatus(timeSlots[date] ?: emptyList()))
              }
            }
      }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentDay: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    dayStatus: List<TimeSlotStatus>
) {
  val backgroundColor =
      when {
        isSelected -> colorScheme.primary
        isCurrentDay -> colorScheme.surfaceVariant
        else -> Color.Transparent
      }
  val textColor = if (isSelected || isCurrentDay) colorScheme.onPrimary else colorScheme.onSurface

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.aspectRatio(1f)
              .padding(2.dp)
              .clickable { onDateSelected(date) }
              .testTag("dayItem_${date}")) {
        Box(
            modifier = Modifier.size(32.dp).background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center) {
              Text(
                  text = date.dayOfMonth.toString(),
                  color = textColor,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium)
            }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          dayStatus.forEach { status -> StatusIndicator(status) }
        }
      }
}

@Composable
fun StatusIndicator(status: TimeSlotStatus) {
  Box(
      modifier =
          Modifier.size(8.dp)
              .background(
                  color =
                      when (status) {
                        TimeSlotStatus.AVAILABLE -> Available
                        TimeSlotStatus.UNAVAILABLE -> Unavailable
                        TimeSlotStatus.BUSY -> Busy
                      },
                  shape = CircleShape)) {
        Box(
            modifier =
                Modifier.size(4.dp)
                    .background(colorScheme.surface, CircleShape)
                    .align(Alignment.Center))
      }
}

@Composable
fun DayView(
    currentViewDate: LocalDate,
    onViewDateChanged: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<TimeSlot>>,
    onTimeSlotsChanged: (LocalDate, List<TimeSlot>) -> Unit
) {
  var offsetX by remember { mutableFloatStateOf(0f) }
  var currentDay by remember(currentViewDate) { mutableStateOf(currentViewDate) }
  var isDragging by remember { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.fillMaxSize().padding(16.dp).pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onHorizontalDrag = { change, dragAmount ->
                  change.consume()
                  if (isDragging) {
                    offsetX += dragAmount

                    if (abs(offsetX) > 100) {
                      currentDay =
                          if (offsetX > 0) {
                            currentDay.minusDays(1)
                          } else {
                            currentDay.plusDays(1)
                          }
                      onViewDateChanged(currentDay)
                      offsetX = 0f
                      isDragging = false
                    }
                  }
                })
          }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = currentDay.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                  modifier =
                      Modifier.clickable(onClick = onHeaderClick)
                          .padding(vertical = 8.dp)
                          .testTag("dayHeader"),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold)
            }
        Spacer(modifier = Modifier.height(16.dp))
        TimeSlots(
            timeSlots = timeSlots[currentDay] ?: emptyList(),
            textColor = colorScheme.onSurface,
            showDescription = true,
            onTimeSlotsChanged = { newTimeSlots -> onTimeSlotsChanged(currentDay, newTimeSlots) })
      }
}

@Composable
fun WeekView(
    currentViewDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onViewDateChanged: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<TimeSlot>>,
    onTimeSlotsChanged: (LocalDate, List<TimeSlot>) -> Unit
) {
  var offsetX by remember { mutableFloatStateOf(0f) }
  var currentWeekStart by
      remember(currentViewDate) {
        mutableStateOf(currentViewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
      }
  var isDragging by remember { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.fillMaxSize().padding(16.dp).pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onHorizontalDrag = { change, dragAmount ->
                  change.consume()
                  if (isDragging) {
                    offsetX += dragAmount

                    if (abs(offsetX) > 100) {
                      currentWeekStart =
                          if (offsetX > 0) {
                            currentWeekStart.minusWeeks(1)
                          } else {
                            currentWeekStart.plusWeeks(1)
                          }
                      onViewDateChanged(currentWeekStart)
                      offsetX = 0f
                      isDragging = false
                    }
                  }
                })
          }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text =
                      "${currentWeekStart.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))} - ${currentWeekStart.plusDays(6).format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}",
                  modifier =
                      Modifier.clickable(onClick = onHeaderClick)
                          .padding(vertical = 8.dp)
                          .testTag("weekHeader"),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold)
            }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
          items(7) { dayOffset ->
            val date = currentWeekStart.plusDays(dayOffset.toLong())
            WeekDayItem(
                date = date,
                isSelected = date == selectedDate,
                isCurrentDay = date == LocalDate.now(),
                onDateSelected = onDateSelected,
                timeSlots = timeSlots[date] ?: emptyList(),
                onTimeSlotsChanged = { newTimeSlots -> onTimeSlotsChanged(date, newTimeSlots) })
          }
        }
      }
}

@Composable
fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentDay: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    timeSlots: List<TimeSlot>,
    onTimeSlotsChanged: (List<TimeSlot>) -> Unit
) {
  val borderColor =
      when {
        isSelected -> colorScheme.primary
        isCurrentDay -> colorScheme.surfaceVariant
        else -> Color.Transparent
      }
  val textColor = colorScheme.onSurface
  val dayDigitColor =
      when {
        isSelected -> colorScheme.onPrimary
        else -> colorScheme.onSurface
      }
  val dayBackgroundColor =
      when {
        isSelected -> colorScheme.primary
        isCurrentDay -> colorScheme.surfaceVariant
        else -> Color.Transparent
      }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .border(1.dp, borderColor, RoundedCornerShape(8.dp))
              .testTag("weekDayItem_${date}")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onDateSelected(date) },
            verticalAlignment = Alignment.CenterVertically) {
              Box(
                  modifier = Modifier.size(32.dp).background(dayBackgroundColor, CircleShape),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontWeight = FontWeight.Bold,
                        color = dayDigitColor)
                  }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  text = date.format(DateTimeFormatter.ofPattern("EEEE")),
                  fontWeight = FontWeight.Bold,
                  color = textColor)
            }
        TimeSlots(
            timeSlots = timeSlots,
            textColor = colorScheme.onSurface,
            showDescription = false,
            onTimeSlotsChanged = onTimeSlotsChanged)
      }
}

@Composable
fun TimeSlots(
    timeSlots: List<TimeSlot>,
    textColor: Color = colorScheme.onSurface,
    showDescription: Boolean = true,
    onTimeSlotsChanged: (List<TimeSlot>) -> Unit
) {
  Column {
    timeSlots.forEachIndexed { index, slot ->
      TimeSlotItem(
          slot = slot,
          textColor = textColor,
          showDescription = showDescription,
          onTimeSlotClick = { newStatus ->
            val updatedTimeSlots = timeSlots.toMutableList()
            updatedTimeSlots[index] =
                when (newStatus) {
                  "Available" ->
                      slot.copy(
                          status = "Available",
                          name = "Available",
                          description = "Nothing planned yet...")
                  "Unavailable" ->
                      slot.copy(
                          status = "Unavailable",
                          name = "Unavailable",
                          description = "You are not available")
                  else -> slot
                }
            onTimeSlotsChanged(updatedTimeSlots)
          })
    }
  }
}

@Composable
fun TimeSlotItem(
    slot: TimeSlot,
    textColor: Color,
    showDescription: Boolean,
    onTimeSlotClick: (String) -> Unit
) {
  val backgroundColor =
      when (slot.status) {
        "Available" -> Available.copy(alpha = 0.1f)
        "Unavailable" -> Unavailable.copy(alpha = 0.1f)
        "Busy" -> Busy.copy(alpha = 0.1f)
        else -> colorScheme.surfaceVariant.copy(alpha = 0.1f)
      }

  val statusColor =
      when (slot.status) {
        "Available" -> Available
        "Unavailable" -> Unavailable
        "Busy" -> Busy
        else -> colorScheme.surfaceVariant
      }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 4.dp, horizontal = 8.dp)
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .clickable {
                when (slot.status) {
                  "Available" -> onTimeSlotClick("Unavailable")
                  "Unavailable" -> onTimeSlotClick("Available")
                  else -> {} // Do nothing for "Busy" status
                }
              }
              .padding(12.dp)
              .testTag("timeSlotItem_${slot.time}")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(12.dp).background(color = statusColor, shape = CircleShape))
          Spacer(Modifier.width(8.dp))
          Text(
              text = slot.time,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = textColor.copy(alpha = 0.7f),
              lineHeight = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = slot.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp)
        if (showDescription) {
          Spacer(Modifier.height(4.dp))
          Text(
              text = slot.description,
              fontSize = 12.sp,
              color = textColor.copy(alpha = 0.7f),
              lineHeight = 14.sp,
              letterSpacing = 0.25.sp)
        }
        Spacer(Modifier.height(4.dp))
      }
}

data class TimeSlot(
    val time: String,
    val status: String,
    val name: String,
    val description: String
)

enum class CalendarView {
  MONTH,
  WEEK,
  DAY
}

@Preview(showBackground = true)
@Composable
fun ProviderCalendarScreenPreview() {
  val context = LocalContext.current
  ProviderCalendarScreen(NavigationActions(FakeNavController(context)))
}

class FakeNavController(context: Context) : NavController(context) {
  override fun popBackStack(): Boolean = true
}

fun calculateDayStatus(timeSlots: List<TimeSlot>): List<TimeSlotStatus> {
  return timeSlots
      .map { slot ->
        when (slot.status) {
          "Available" -> TimeSlotStatus.AVAILABLE
          "Unavailable" -> TimeSlotStatus.UNAVAILABLE
          "Busy" -> TimeSlotStatus.BUSY
          else -> TimeSlotStatus.AVAILABLE
        }
      }
      .take(4) // Limit to 4 status indicators
}

enum class TimeSlotStatus {
  AVAILABLE,
  UNAVAILABLE,
  BUSY
}

fun generateInitialTimeSlots(): Map<LocalDate, List<TimeSlot>> {
  val startDate = LocalDate.of(2000, 1, 1) // A date far in the past
  val endDate = LocalDate.of(2100, 12, 31) // A date far in the future
  val today = LocalDate.now()

  val result = mutableMapOf<LocalDate, List<TimeSlot>>()
  var currentDate = startDate
  while (!currentDate.isAfter(endDate)) {
    result[currentDate] =
        if (currentDate == today) {
          listOf(
              TimeSlot("08:00-10:00", "Unavailable", "Unavailable", "You are not available"),
              TimeSlot("10:00-12:00", "Available", "Available", "Nothing planned yet..."),
              TimeSlot(
                  "13:00-15:00",
                  "Busy",
                  "Sink Repair",
                  "Sink detached from wall and needs to be repaired"),
              TimeSlot(
                  "15:00-17:00",
                  "Busy",
                  "Shower Installation",
                  "Shower needs to be installed in the master bedroom"))
        } else {
          listOf(
              TimeSlot("08:00-10:00", "Available", "Available", "Nothing planned yet..."),
              TimeSlot("10:00-12:00", "Available", "Available", "Nothing planned yet..."),
              TimeSlot("13:00-15:00", "Available", "Available", "Nothing planned yet..."),
              TimeSlot("15:00-17:00", "Available", "Available", "Nothing planned yet..."))
        }
    currentDate = currentDate.plusDays(1)
  }
  return result
}
