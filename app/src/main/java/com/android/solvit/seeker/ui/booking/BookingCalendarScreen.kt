package com.android.solvit.seeker.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.ui.calendar.components.container.SwipeableCalendarContainer
import com.android.solvit.seeker.model.SeekerBookingViewModel
import com.android.solvit.seeker.ui.booking.components.BookingDayView
import com.android.solvit.seeker.ui.booking.components.BookingInfoCard
import com.android.solvit.seeker.ui.booking.components.BookingMonthView
import com.android.solvit.seeker.ui.booking.components.BookingStepper
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalendarScreen(navigationActions: NavigationActions, viewModel: SeekerBookingViewModel) {
  val selectedDate by viewModel.selectedDate.collectAsState()
  val availableTimeSlots by viewModel.availableTimeSlots.collectAsState()
  val showDayView by viewModel.showDayView.collectAsState()
  val currentProvider by viewModel.currentProvider.collectAsState()
  val selectedRequest by viewModel.selectedRequest.collectAsState()
  var shouldAnimate by remember { mutableStateOf(true) }

  val serviceColor = Services.getColor(selectedRequest?.type!!)

  // Trigger initial load of time slots
  LaunchedEffect(currentProvider, selectedDate) {
    currentProvider?.let { viewModel.updateAvailableTimeSlots() }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Book Service",
                  fontFamily = FontFamily.Default,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.ExtraBold,
                  lineHeight = 24.sp,
                  textAlign = TextAlign.Left,
                  color = MaterialTheme.colorScheme.onBackground,
                  modifier = Modifier.testTag("bookingCalendarTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground))
      }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          BookingStepper(
              currentStep = if (showDayView) 2 else 1,
              onStepSelected = { step ->
                if (step == 1) {
                  viewModel.setShowDayView(false)
                }
              },
              serviceColor = serviceColor)

          if (!showDayView) {
            Column(modifier = Modifier.fillMaxSize()) {
              // Calendar takes up available space
              Box(modifier = Modifier.weight(1f)) {
                SwipeableCalendarContainer(
                    currentViewDate = selectedDate,
                    onViewDateChanged = { date ->
                      shouldAnimate = true
                      viewModel.onDateSelected(date)
                    },
                    calendarView = CalendarView.MONTH,
                    shouldAnimate = shouldAnimate,
                    modifier = Modifier.testTag("swipeableMonthView")) { date ->
                      BookingMonthView(
                          viewDate = date,
                          onDateSelected = { selectedDate ->
                            viewModel.onDateSelected(selectedDate)
                            viewModel.setShowDayView(true)
                          },
                          onHeaderClick = {}, // No date picker needed for booking
                          serviceColor = serviceColor,
                          timeSlots = viewModel.getMonthAvailabilities(date))
                    }
              }

              // Place the BookingInfoCard at the bottom with no weight
              BookingInfoCard(
                  provider = currentProvider,
                  serviceRequest = selectedRequest,
                  modifier = Modifier.padding(vertical = 16.dp))
            }
          } else {
            SwipeableCalendarContainer(
                currentViewDate = selectedDate,
                onViewDateChanged = { date ->
                  shouldAnimate = true
                  viewModel.onDateSelected(date)
                },
                calendarView = CalendarView.DAY,
                shouldAnimate = shouldAnimate,
                modifier = Modifier.testTag("swipeableDayView")) { date ->
                  BookingDayView(
                      viewDate = date,
                      timeSlots = availableTimeSlots,
                      onHeaderClick = { viewModel.setShowDayView(false) },
                      onTimeSlotSelected = { timeSlot ->
                        val selectedRequest = viewModel.selectedRequest.value
                        if (selectedRequest != null) {
                          val meetingDateTime = date.atTime(timeSlot.start)
                          val meetingDate =
                              Date.from(meetingDateTime.atZone(ZoneId.systemDefault()).toInstant())
                          val updatedRequest =
                              selectedRequest.copy(meetingDate = Timestamp(meetingDate))
                          viewModel.onTimeSlotSelected(updatedRequest)
                          navigationActions.navigateTo(Route.BOOKING_DETAILS)
                        }
                      },
                      serviceColor = serviceColor)
                }
          }
        }
      }
}
