package com.android.solvit.provider.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CalendarView {
  DAY,
  WEEK,
  MONTH
}

class ProviderCalendarViewModel(
    private val authViewModel: AuthViewModel,
    private val serviceRequestViewModel: ServiceRequestViewModel
) : ViewModel() {

  private val _currentViewDate = MutableStateFlow(LocalDate.now())
  val currentViewDate: StateFlow<LocalDate> = _currentViewDate.asStateFlow()

  private val _selectedDate = MutableStateFlow(LocalDate.now())
  val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

  private val _calendarView = MutableStateFlow(CalendarView.WEEK)
  val calendarView: StateFlow<CalendarView> = _calendarView.asStateFlow()

  private val _timeSlots = MutableStateFlow<Map<LocalDate, List<ServiceRequest>>>(emptyMap())
  val timeSlots: StateFlow<Map<LocalDate, List<ServiceRequest>>> = _timeSlots.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    // Initialize service requests
    serviceRequestViewModel.getServiceRequests()

    // Observe changes in service requests and user
    viewModelScope.launch {
      combine(serviceRequestViewModel.requests, authViewModel.user) { requests, user ->
            _isLoading.value = true
            try {
              val userId = user?.uid
              if (userId == null) {
                _timeSlots.value = emptyMap()
                return@combine
              }

              val startDate =
                  when (_calendarView.value) {
                    CalendarView.MONTH -> _currentViewDate.value.withDayOfMonth(1)
                    CalendarView.WEEK ->
                        _currentViewDate.value.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    CalendarView.DAY -> _currentViewDate.value
                  }
              val endDate =
                  when (_calendarView.value) {
                    CalendarView.MONTH -> startDate.plusMonths(1).minusDays(1)
                    CalendarView.WEEK -> startDate.plusWeeks(1).minusDays(1)
                    CalendarView.DAY -> startDate
                  }

              // Filter and group requests
              val filteredRequests =
                  requests
                      .filter { it.providerId == userId }
                      .filter { request ->
                        val requestDate =
                            request.meetingDate
                                ?.toInstant()
                                ?.atZone(ZoneId.systemDefault())
                                ?.toLocalDate() ?: return@filter false
                        !requestDate.isBefore(startDate) && !requestDate.isAfter(endDate)
                      }

              // Group by date and sort by time
              val groupedRequests =
                  filteredRequests
                      .groupBy {
                        it.meetingDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                      }
                      .mapValues { (_, requests) ->
                        requests.sortedBy { it.meetingDate!!.toInstant() }
                      }

              _timeSlots.value = groupedRequests
            } finally {
              _isLoading.value = false
            }
          }
          .collect()
    }
  }

  fun onDateSelected(date: LocalDate) {
    _selectedDate.value = date
    _currentViewDate.value = date
  }

  fun onViewDateChanged(date: LocalDate) {
    _currentViewDate.value = date
  }

  fun onCalendarViewChanged(view: CalendarView) {
    _calendarView.value = view
  }

  fun onServiceRequestClick(request: ServiceRequest) {
    serviceRequestViewModel.selectRequest(request)
  }

  companion object {
    val Factory: (AuthViewModel, ServiceRequestViewModel) -> ViewModelProvider.Factory =
        { auth, service ->
          object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
              if (modelClass.isAssignableFrom(ProviderCalendarViewModel::class.java)) {
                return ProviderCalendarViewModel(auth, service) as T
              }
              throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
          }
        }
  }
}
