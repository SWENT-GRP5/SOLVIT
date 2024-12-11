package com.android.solvit.seeker.model.provider

import androidx.lifecycle.ViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.TimeSlot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookProvider() : ViewModel() {

  private val _currentProvider = MutableStateFlow(Provider())
  val currentProvider: StateFlow<Provider> = _currentProvider.asStateFlow()

  /**
   * Set the current provider seeker want to book
   *
   * @param provider current provider seeker want to book
   */
  fun setProvider(provider: Provider) {
    _currentProvider.value = provider
  }
  /**
   * Retrieve availabilities of a provider within a day
   *
   * @param date the day within we want to check provider availabilities
   * @return list of available time slots
   */
  fun getProviderAvailabilities(date: LocalDate): List<TimeSlot> {
    val startTime = LocalTime.of(8, 0)
    val endTime = LocalTime.of(19, 0)

    val startDateTime = LocalDateTime.of(date, startTime)
    val endDateTime = LocalDateTime.of(date, endTime)

    val dateTimeList = mutableListOf<LocalDateTime>()
    val timeSlots = mutableListOf<TimeSlot>()
    var currentDateTime = startDateTime
    while (currentDateTime <= endDateTime) {
      dateTimeList.add(currentDateTime)
      currentDateTime = currentDateTime.plusHours(1)
    }
    dateTimeList.forEach {
      if (currentProvider.value.schedule.isAvailable(it)) {
        timeSlots.add(
            TimeSlot(
                startHour = it.hour, startMinute = it.minute, endHour = it.hour + 1, endMinute = 0))
      }
    }
    return timeSlots
  }

  /**
   * Retrieve the next availabilities within the next days
   *
   * @return the next five available slots of a provider
   */
  fun getNextFiveSlots(startDate: LocalDate): List<TimeSlot> {
    var date = startDate
    val nextFivePossibleSlots = mutableListOf<TimeSlot>()
    var currentSize = 0
    while (currentSize < 5) {
      val timeSlots = getProviderAvailabilities(date)
      if (timeSlots.size + currentSize >= 5) {
        nextFivePossibleSlots.addAll(currentSize, timeSlots.subList(0, 5 - currentSize))
        currentSize = 5
      } else {
        if (timeSlots.isNotEmpty()) {
          nextFivePossibleSlots.addAll(currentSize, timeSlots)
          currentSize += timeSlots.size
        }
        date = date.plusDays(1)
      }
    }
    return nextFivePossibleSlots
  }
}
