package com.android.solvit.seeker.model.provider

import androidx.lifecycle.ViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.TimeSlot
import java.time.LocalDate
import java.time.LocalDateTime
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

    val timeSlots = mutableListOf<TimeSlot>()

    val regularHours = currentProvider.value.schedule.regularHours[date.dayOfWeek.name]
    regularHours?.forEach {
      var currentTime = it.start
      while (currentTime < it.end) {
        val endTime = currentTime.plusHours(1)
        if (currentProvider.value.schedule.isAvailable(LocalDateTime.of(date, currentTime))) {
          timeSlots.add(
              TimeSlot(
                  startHour = it.startHour,
                  startMinute = it.startMinute,
                  endHour = endTime.hour,
                  endMinute = endTime.minute))
        }
        currentTime = currentTime.plusHours(1)
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
