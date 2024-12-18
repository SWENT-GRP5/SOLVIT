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
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalendarScreen(navigationActions: NavigationActions, viewModel: SeekerBookingViewModel) {
  val selectedDate by viewModel.selectedDate.collectAsState()
  val availableTimeSlots by viewModel.availableTimeSlots.collectAsState()
  val showDayView by viewModel.showDayView.collectAsState()
  val currentProvider by viewModel.currentProvider.collectAsState()
  val selectedRequest by viewModel.selectedRequest.collectAsState()
  var shouldAnimate by remember { mutableStateOf(true) }

  // Early return if selectedRequest is null
  val request = selectedRequest ?: return

  val serviceColor = Services.getColor(request.type)

  // Trigger initial load of time slots
  LaunchedEffect(currentProvider, selectedDate) {
    currentProvider?.let { viewModel.updateAvailableTimeSlots() }
  }

  // Reset animation flag after view date changes
  LaunchedEffect(selectedDate) { shouldAnimate = true }

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
        Box(modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // BookingInfoCard at the top
            BookingInfoCard(
                provider = currentProvider,
                serviceRequest = request,
                selectedDate = selectedDate,
                selectedTime = if (showDayView) null else null, // We'll add time selection later
                showDayView = showDayView,
                onBackToMonthView = { viewModel.setShowDayView(false) },
                modifier = Modifier.padding(vertical = 8.dp))

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
                            deadlineDate =
                                request.dueDate?.let { timestamp ->
                                  Instant.ofEpochMilli(timestamp.seconds * 1000)
                                      .atZone(ZoneId.systemDefault())
                                      .toLocalDate()
                                },
                            timeSlots = viewModel.getMonthAvailabilities(date))
                      }
                }
              }
            } else {
              SwipeableCalendarContainer(
                  currentViewDate = selectedDate,
                  onViewDateChanged = { date ->
                    shouldAnimate = false // Disable animation during swipe
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
                          viewModel.onTimeSlotSelected(timeSlot)
                          navigationActions.navigateTo(Route.BOOKING_DETAILS)
                        },
                        serviceColor = serviceColor,
                        deadlineDate =
                            request.dueDate?.let { timestamp ->
                              Instant.ofEpochMilli(timestamp.seconds * 1000)
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalDate()
                            })
                  }
            }
          }
        }
      }
}
