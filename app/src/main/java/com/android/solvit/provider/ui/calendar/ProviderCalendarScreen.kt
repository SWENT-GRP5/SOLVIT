package com.android.solvit.provider.ui.calendar

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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
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
fun ProviderCalendarScreen(
    navigationActions: NavigationActions,
    viewModel: ProviderCalendarViewModel
) {
  val serviceRequests by
      viewModel.serviceRequests.collectAsStateWithLifecycle(initialValue = emptyList())

  var selectedDate by remember { mutableStateOf(LocalDate.now()) }
  var currentViewDate by remember { mutableStateOf(LocalDate.now()) }
  var calendarView by remember { mutableStateOf(CalendarView.MONTH) }
  var showBottomSheet by remember { mutableStateOf(false) }
  var showDatePicker by remember { mutableStateOf(false) }

  val timeSlotsByDate =
      serviceRequests.groupBy { request ->
        val date = request.meetingDate?.toInstant() ?: request.dueDate.toInstant()
        date.atZone(ZoneId.systemDefault()).toLocalDate()
      }

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
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = navigationActions.currentRoute())
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
                    timeSlots = timeSlotsByDate,
                    calendarView = calendarView)
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
                    timeSlots = timeSlotsByDate,
                    calendarView = calendarView)
            CalendarView.DAY ->
                DayView(
                    currentViewDate = currentViewDate,
                    onViewDateChanged = { currentViewDate = it },
                    onHeaderClick = { showDatePicker = true },
                    timeSlots = timeSlotsByDate,
                    calendarView = calendarView)
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
              timeSlots = timeSlotsByDate,
              calendarView = calendarView)
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
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    calendarView: CalendarView
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
                MonthDayItem(
                    date = date,
                    isSelected = date == selectedDate,
                    isCurrentDay = date == LocalDate.now(),
                    isCurrentMonth = YearMonth.from(date) == currentMonth,
                    onDateSelected = onDateSelected,
                    timeSlots = timeSlots[date] ?: emptyList(),
                    calendarView = calendarView)
              }
            }
      }
}

@Composable
fun MonthDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentDay: Boolean,
    isCurrentMonth: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    timeSlots: List<ServiceRequest>,
    calendarView: CalendarView
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
          calculateDayStatus(timeSlots).forEach { status -> StatusIndicator(status) }
        }
      }
}

@Composable
fun StatusIndicator(status: ServiceRequestStatus) {
  Box(
      modifier =
          Modifier.size(8.dp)
              .background(
                  color = ServiceRequestStatus.getStatusColor(status), shape = CircleShape)) {
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
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    calendarView: CalendarView
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
            currentView = calendarView)
      }
}

@Composable
fun WeekView(
    currentViewDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onViewDateChanged: (LocalDate) -> Unit,
    onHeaderClick: () -> Unit,
    timeSlots: Map<LocalDate, List<ServiceRequest>>,
    calendarView: CalendarView
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
                calendarView = calendarView)
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
    timeSlots: List<ServiceRequest>,
    calendarView: CalendarView
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
              Spacer(Modifier.width(8.dp))
              Text(
                  text = date.format(DateTimeFormatter.ofPattern("EEEE")),
                  fontWeight = FontWeight.Bold,
                  color = textColor)
            }
        TimeSlots(
            timeSlots = timeSlots,
            textColor = colorScheme.onSurface,
            showDescription = false,
            currentView = calendarView)
      }
}

@Composable
fun TimeSlots(
    timeSlots: List<ServiceRequest>,
    textColor: Color = colorScheme.onSurface,
    showDescription: Boolean = true,
    currentView: CalendarView
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    timeSlots
        .sortedBy { request ->
          val date = request.meetingDate?.toInstant() ?: request.dueDate.toInstant()
          date
        }
        .forEach { request -> TimeSlotItem(request, textColor, showDescription, currentView) }
  }
}

@Composable
fun TimeSlotItem(
    request: ServiceRequest,
    textColor: Color,
    showDescription: Boolean,
    currentView: CalendarView
) {
  val backgroundColor = ServiceRequestStatus.getStatusColor(request.status).copy(alpha = 0.1f)
  val statusColor = ServiceRequestStatus.getStatusColor(request.status)
  val meetingTime =
      request.meetingDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
          ?: request.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 4.dp, horizontal = 8.dp)
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .border(1.dp, statusColor, RoundedCornerShape(8.dp))
              .padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      StatusIndicator(request.status)
                      Text(
                          text = meetingTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                          color = textColor,
                          style =
                              MaterialTheme.typography.bodyMedium.copy(
                                  fontWeight = FontWeight.Bold))
                      Text(
                          text = request.title,
                          color = textColor,
                          style =
                              MaterialTheme.typography.bodyMedium.copy(
                                  fontWeight = FontWeight.Bold),
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis)
                    }
                if ((showDescription || currentView == CalendarView.DAY) &&
                    request.description.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                      text = request.description,
                      color = textColor,
                      style = MaterialTheme.typography.bodySmall,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis)
                }
              }
            }
      }
}

enum class CalendarView {
  MONTH,
  WEEK,
  DAY
}

fun calculateDayStatus(requests: List<ServiceRequest>): List<ServiceRequestStatus> {
  return requests
      .sortedBy { request -> request.meetingDate?.toInstant() ?: request.dueDate.toInstant() }
      .map { it.status }
      .take(4) // Limit to 4 status indicators
}
