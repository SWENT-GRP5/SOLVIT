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
import androidx.compose.ui.unit.sp
import com.android.solvit.provider.model.CalendarView
import com.android.solvit.provider.ui.calendar.components.container.SwipeableCalendarContainer
import com.android.solvit.provider.ui.calendar.views.day.DayView
import com.android.solvit.provider.ui.calendar.views.month.MonthView
import com.android.solvit.seeker.model.SeekerBookingViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCalendarScreen(
    navigationActions: NavigationActions,
    viewModel: SeekerBookingViewModel
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val availableTimeSlots by viewModel.availableTimeSlots.collectAsState()
    val showDayView by viewModel.showDayView.collectAsState()
    var shouldAnimate by remember { mutableStateOf(true) }

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
                        modifier = Modifier.testTag("bookingCalendarTitle")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("backButton")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!showDayView) {
                SwipeableCalendarContainer(
                    currentViewDate = selectedDate,
                    onViewDateChanged = { date ->
                        shouldAnimate = true
                        viewModel.onDateSelected(date)
                    },
                    calendarView = CalendarView.MONTH,
                    shouldAnimate = shouldAnimate,
                    modifier = Modifier.testTag("swipeableMonthView")
                ) { date ->
                    MonthView(
                        viewDate = date,
                        onDateSelected = { selectedDate ->
                            viewModel.onDateSelected(selectedDate, switchToDayView = true)
                            viewModel.setShowDayView(true)
                        },
                        onHeaderClick = {}, // No date picker needed for booking
                        timeSlots = mapOf(selectedDate to availableTimeSlots.map { timeSlot -> 
                            ServiceRequest(
                                uid = timeSlot.toString(),
                                title = "",
                                description = "",
                                meetingDate = Timestamp(Date.from(selectedDate.atTime(timeSlot.startHour, timeSlot.startMinute)
                                    .atZone(ZoneId.systemDefault()).toInstant())),
                                status = ServiceRequestStatus.PENDING
                            )
                        }),
                        isBookingView = true
                    )
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
                    modifier = Modifier.testTag("swipeableDayView")
                ) { date ->
                    DayView(
                        viewDate = date,
                        timeSlots = availableTimeSlots.map { timeSlot -> 
                            ServiceRequest(
                                uid = timeSlot.toString(),
                                title = "",
                                description = "",
                                meetingDate = Timestamp(Date.from(date.atTime(timeSlot.startHour, timeSlot.startMinute)
                                    .atZone(ZoneId.systemDefault()).toInstant())),
                                status = ServiceRequestStatus.PENDING
                            )
                        },
                        onServiceRequestClick = { viewModel.onTimeSlotSelected(it) },
                        isBookingView = true,
                        onHeaderClick = { viewModel.setShowDayView(false) }
                    )
                }
            }
        }
    }
}
