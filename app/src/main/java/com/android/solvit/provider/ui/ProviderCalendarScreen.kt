package com.android.solvit.provider.ui.calendar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                  modifier = Modifier.testTag("calendarTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black)
                  }
            },
            actions = {
              IconButton(
                  onClick = { /* TODO: Implement menu action */},
                  modifier = Modifier.testTag("menuButton")) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF5669FF))
                  }
            },
            modifier = Modifier.testTag("topAppBar"))
      },
      containerColor = Color.White) { paddingValues ->
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
                )
            CalendarView.WEEK ->
                WeekView(
                    currentViewDate = currentViewDate,
                    selectedDate = selectedDate,
                    onDateSelected = {
                      selectedDate = it
                      showBottomSheet = true
                    })
            CalendarView.DAY ->
                DayView(
                    currentViewDate = currentViewDate, onViewDateChanged = { currentViewDate = it })
          }
        }
      }

  if (showBottomSheet && calendarView != CalendarView.DAY) {
    ModalBottomSheet(
        onDismissRequest = { showBottomSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.testTag("bottomSheet")) {
          DayView(currentViewDate = selectedDate, onViewDateChanged = {})
        }
  }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
              onClick = {
                showDatePicker = false
                datePickerState.selectedDateMillis?.let { millis ->
                  val newDate =
                      Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                  selectedDate = newDate
                  showBottomSheet = true
                }
              }) {
                Text("OK")
              }
        },
        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        modifier = Modifier.testTag("datePickerDialog")) {
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
                          if (currentView == view) Color(0xFF0099FF) else Color.LightGray),
              modifier = Modifier.testTag("toggleButton_${view.name.lowercase()}")) {
                Text(view.name, color = if (currentView == view) Color.White else Color.Black)
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
) {
  var offsetX by remember { mutableFloatStateOf(0f) }
  var currentMonth by remember(currentViewDate) { mutableStateOf(YearMonth.from(currentViewDate)) }
  var isDragging by remember { mutableStateOf(false) }

  Column(
      modifier =
          Modifier.pointerInput(Unit) {
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
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            modifier = Modifier.padding(16.dp).testTag("monthYearText"),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default)

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
          items(7) { dayIndex ->
            Text(
                text =
                    DayOfWeek.of(dayIndex + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.padding(8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center)
          }

          val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
          repeat(firstDayOfWeek - 1) { item { Spacer(modifier = Modifier.aspectRatio(1f)) } }

          items(currentMonth.lengthOfMonth()) { dayOfMonth ->
            val date = currentMonth.atDay(dayOfMonth + 1)
            DayItem(
                date = date,
                isSelected = date == selectedDate,
                isCurrentDay = date == LocalDate.now(),
                onDateSelected = onDateSelected)
          }
        }
      }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentDay: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.aspectRatio(1f)
              .padding(2.dp)
              .clickable { onDateSelected(date) }
              .testTag("dayItem")) {
        Box(
            modifier =
                Modifier.size(32.dp)
                    .background(
                        color =
                            when {
                              isSelected -> Color(0xFF0099FF)
                              isCurrentDay -> Color.LightGray
                              else -> Color.Transparent
                            },
                        shape = CircleShape),
            contentAlignment = Alignment.Center) {
              Text(
                  text = date.dayOfMonth.toString(),
                  color = if (isSelected) Color.White else Color.Black,
                  fontSize = 14.sp)
            }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          repeat(4) { index ->
            Box(
                modifier =
                    Modifier.size(4.dp)
                        .background(
                            color =
                                when {
                                  index < 2 -> Color(0xFF00C853) // First 2 are green
                                  else -> Color(0xFFEC5865) // Last 2 are red
                                },
                            shape = CircleShape))
          }
        }
      }
}

@Composable
fun WeekView(
    currentViewDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
  val weekStart = currentViewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

  Column {
    Text(
        text = "Week of ${weekStart.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
        modifier = Modifier.padding(16.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.Default)

    LazyColumn {
      items(7) { dayOffset ->
        val date = weekStart.plusDays(dayOffset.toLong())
        WeekDayItem(date, selectedDate, onDateSelected)
      }
    }
  }
}

@Composable
fun WeekDayItem(date: LocalDate, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
  val isSelected = date == selectedDate

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .background(
                  if (isSelected) Color(0xFF5669FF) else Color.Transparent,
                  RoundedCornerShape(8.dp))
              .clickable { onDateSelected(date) }) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            color = if (isSelected) Color.White else Color.Black)
        TimeSlots()
      }
}

@Composable
fun DayView(
    currentViewDate: LocalDate,
    onViewDateChanged: (LocalDate) -> Unit,
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
        Text(
            text = currentDay.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        TimeSlots()
      }
}

@Composable
fun TimeSlots() {
  val timeSlots =
      listOf(
          TimeSlot("08:00-12:00", "Available", "Available", "Nothing planned yet..."),
          TimeSlot("10:00-12:00", "Available", "Available", "Nothing planned yet..."),
          TimeSlot(
              "13:00-15:00",
              "Busy",
              "Brainstorm with the team",
              "Define the problem or question that..."),
          TimeSlot(
              "15:00-17:00", "Busy", "Workout with Ella", "We will do the legs and back workout"))

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    timeSlots.forEach { slot -> TimeSlotItem(slot) }
  }
}

@Composable
fun TimeSlotItem(slot: TimeSlot) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .testTag("timeSlotItem")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
              modifier =
                  Modifier.size(12.dp)
                      .background(
                          when (slot.status) {
                            "Available" -> Color(0xFF00C853)
                            "Busy" -> Color(0xFFEC5865)
                            else -> Color.Gray
                          },
                          CircleShape))
          Spacer(Modifier.width(8.dp))
          Text(
              text = slot.time,
              fontSize = 12.sp,
              fontWeight = FontWeight.Normal,
              color = Color.Gray,
              lineHeight = 14.sp)
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = slot.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 19.sp,
            letterSpacing = 1.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = slot.description,
            fontSize = 12.sp,
            color = Color.Gray,
            lineHeight = 14.sp,
            letterSpacing = 0.75.sp)
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
