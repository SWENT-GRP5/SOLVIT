package com.android.solvit.seeker.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepositoryFirebase
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class SeekerBookingViewModel(
    private val providerRepository: ProviderRepository,
    private val serviceRequestViewModel: ServiceRequestViewModel
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow<TimeSlot?>(null)
    val selectedTimeSlot: StateFlow<TimeSlot?> = _selectedTimeSlot.asStateFlow()

    private val _availableTimeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val availableTimeSlots: StateFlow<List<TimeSlot>> = _availableTimeSlots.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentProvider = MutableStateFlow<Provider?>(null)
    val currentProvider: StateFlow<Provider?> = _currentProvider.asStateFlow()

    private val _showDayView = MutableStateFlow(false)
    val showDayView: StateFlow<Boolean> = _showDayView.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            // Get the selected provider from the repository
            val selectedProviderId = serviceRequestViewModel.selectedProviderId.value
            if (selectedProviderId != null) {
                providerRepository.getProvider(
                    selectedProviderId,
                    onSuccess = { provider ->
                        _currentProvider.value = provider
                        updateAvailableTimeSlots()
                        _isLoading.value = false
                    },
                    onFailure = { 
                        _isLoading.value = false
                    }
                )
            } else {
                _isLoading.value = false
            }
        }

        // Add listener for provider updates to keep schedule in sync
        serviceRequestViewModel.selectedProviderId.value?.let { providerId ->
            providerRepository.getProvider(
                providerId,
                onSuccess = { provider ->
                    _currentProvider.value = provider
                    updateAvailableTimeSlots()
                },
                onFailure = { /* Handle error */ }
            )
        }
    }

    /**
     * Updates the selected date and fetches available time slots for that date
     */
    fun onDateSelected(date: LocalDate, switchToDayView: Boolean = true) {
        _selectedDate.value = date
        _showDayView.value = switchToDayView
        _selectedTimeSlot.value = null
        updateAvailableTimeSlots()
    }

    /**
     * Updates the list of available time slots for the selected date
     */
    private fun updateAvailableTimeSlots() {
        _currentProvider.value?.let { provider ->
            _availableTimeSlots.value = provider.schedule.getProviderAvailabilities(_selectedDate.value)
        }
    }

    /**
     * Schedules a service request for the selected time slot
     */
    fun onTimeSlotSelected(request: ServiceRequest) {
        val selectedRequest = serviceRequestViewModel.selectedRequest.value ?: return
        val timeSlot = TimeSlot(
            startHour = request.meetingDate!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime().hour,
            startMinute = request.meetingDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime().minute,
            endHour = request.meetingDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime().hour + 1,
            endMinute = request.meetingDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime().minute
        )
        _selectedTimeSlot.value = timeSlot

        // Create the meeting date by combining the selected date and time slot
        val meetingDateTime = LocalDateTime.of(
            _selectedDate.value,
            timeSlot.start
        )
        val meetingDate = Date.from(meetingDateTime.atZone(ZoneId.systemDefault()).toInstant())

        // Update the service request with the meeting date and schedule it
        val updatedRequest = selectedRequest.copy(
            meetingDate = Timestamp(meetingDate)
        )
        serviceRequestViewModel.scheduleRequest(updatedRequest)
    }

    fun setShowDayView(show: Boolean) {
        _showDayView.value = show
    }

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Creates dependencies inline during ViewModel instantiation
            val providerRepository = ProviderRepositoryFirestore(Firebase.firestore, FirebaseStorage.getInstance())
            val serviceRequestViewModel = ServiceRequestViewModel(
                ServiceRequestRepositoryFirebase(Firebase.firestore, Firebase.storage)
            )

            return SeekerBookingViewModel(providerRepository, serviceRequestViewModel) as T
        }
    }
}
