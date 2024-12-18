package com.android.solvit.provider.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.provider.ui.calendar.components.container.SwipeableCalendarContainer
import com.android.solvit.provider.ui.calendar.components.dialog.DatePickerDialog
import com.android.solvit.provider.ui.calendar.components.navigation.CalendarViewToggle
import com.android.solvit.provider.ui.calendar.components.preferences.SchedulePreferencesSheet
import com.android.solvit.provider.ui.calendar.components.timeslot.BottomSheetTimeSlots
import com.android.solvit.provider.ui.calendar.views.day.DayView
import com.android.solvit.provider.ui.calendar.views.month.MonthView
import com.android.solvit.provider.ui.calendar.views.week.WeekView
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderCalendarScreen(
    navigationActions: NavigationActions,
    viewModel: ProviderCalendarViewModel
) {
  val currentViewDate by viewModel.currentViewDate.collectAsStateWithLifecycle()
  val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
  val calendarView by viewModel.calendarView.collectAsStateWithLifecycle()
  val timeSlots by viewModel.timeSlots.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

  var showDatePicker by remember { mutableStateOf(false) }
  var showBottomSheet by remember { mutableStateOf(false) }
  var showPreferences by remember { mutableStateOf(false) }
  var shouldAnimate by remember { mutableStateOf(true) }

  Scaffold(
      topBar = {
        TopAppBarInbox(
            title = "My Calendar",
            testTagTitle = "calendarTitle",
            rightButtonForm = Icons.Default.Settings,
            rightButtonAction = { showPreferences = true },
            testTagRight = "settingsButton")
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = Route.CALENDAR)
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          CalendarViewToggle(
              currentView = calendarView,
              onViewChange = {
                shouldAnimate = true
                viewModel.onCalendarViewChanged(it)
              },
              modifier = Modifier.testTag("calendarViewToggle"))

          Box(modifier = Modifier.weight(1f)) {
            SwipeableCalendarContainer(
                currentViewDate = currentViewDate,
                onViewDateChanged = { date ->
                  shouldAnimate = true
                  viewModel.onViewDateChanged(date)
                },
                calendarView = calendarView,
                shouldAnimate = shouldAnimate,
                modifier = Modifier.testTag("swipeableCalendarContainer")) { date ->
                  when (calendarView) {
                    CalendarView.DAY -> {
                      DayView(
                          date = date,
                          onHeaderClick = {
                            shouldAnimate = false
                            showDatePicker = true
                          },
                          timeSlots = timeSlots,
                          onServiceRequestClick = { request ->
                            viewModel.onServiceRequestClick(request)
                            navigationActions.navigateTo(Route.BOOKING_DETAILS)
                          },
                          modifier = Modifier.testTag("dayView"))
                    }
                    CalendarView.WEEK -> {
                      WeekView(
                          date = date,
                          onHeaderClick = {
                            shouldAnimate = false
                            showDatePicker = true
                          },
                          timeSlots = timeSlots,
                          onServiceRequestClick = { request ->
                            viewModel.onServiceRequestClick(request)
                            navigationActions.navigateTo(Route.BOOKING_DETAILS)
                          },
                          onDateSelected = { selected ->
                            shouldAnimate = false
                            viewModel.onDateSelected(selected)
                            showBottomSheet = true
                          },
                          modifier = Modifier.testTag("weekView"))
                    }
                    CalendarView.MONTH -> {
                      MonthView(
                          viewDate = date,
                          onDateSelected = { selected ->
                            shouldAnimate = false
                            viewModel.onDateSelected(selected)
                            showBottomSheet = true
                          },
                          onHeaderClick = {
                            shouldAnimate = false
                            showDatePicker = true
                          },
                          timeSlots = timeSlots,
                          modifier = Modifier.testTag("monthView"))
                    }
                  }
                }

            if (isLoading) {
              CircularProgressIndicator(
                  modifier =
                      Modifier.size(48.dp).align(Alignment.Center).testTag("loadingIndicator"))
            }
          }
        }
      }

  if (showBottomSheet && calendarView != CalendarView.DAY) {
    ModalBottomSheet(
        onDismissRequest = { showBottomSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.testTag("bottomSheet")) {
          Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("bottomSheetHeader"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)) {
                        Text(
                            text =
                                selectedDate.format(
                                    DateTimeFormatter.ofPattern(
                                        "EEEE d MMMM yyyy", Locale.getDefault())),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier =
                                Modifier.clickable {
                                      shouldAnimate = false
                                      showDatePicker = true
                                    }
                                    .testTag("selectedDateText"))
                      }
                }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp).testTag("bottomSheetDivider"))

            BottomSheetTimeSlots(
                timeSlots = timeSlots[selectedDate] ?: emptyList(),
                onServiceRequestClick = { request ->
                  viewModel.onServiceRequestClick(request)
                  showBottomSheet = false
                  navigationActions.navigateTo(Route.BOOKING_DETAILS)
                },
                modifier = Modifier.testTag("bottomSheetTimeSlots"))
          }
        }
  }

  if (showDatePicker) {
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        onDateSelected = { date ->
          viewModel.onDateSelected(date)
          showDatePicker = false
          shouldAnimate = false
          if (calendarView != CalendarView.DAY) {
            showBottomSheet = true
          }
        },
        selectedDate = selectedDate,
        modifier = Modifier.testTag("datePickerDialog"))
  }

  if (showPreferences) {
    SchedulePreferencesSheet(viewModel = viewModel, onDismiss = { showPreferences = false })
  }
}
