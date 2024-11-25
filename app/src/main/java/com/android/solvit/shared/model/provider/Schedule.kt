package com.android.solvit.shared.model.provider

import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*

/** Represents a time slot with a start and end time */
data class TimeSlot(
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 1,
    val endMinute: Int = 0
) {
  constructor(
      start: LocalTime,
      end: LocalTime
  ) : this(start.hour, start.minute, end.hour, end.minute)

  val start: LocalTime
    get() = LocalTime.of(startHour, startMinute)

  val end: LocalTime
    get() = LocalTime.of(endHour, endMinute)

  init {
    require(startHour < endHour || (startHour == endHour && startMinute < endMinute)) {
      "Start time must be before end time"
    }
  }
}

/**
 * Represents an exception to the regular schedule
 *
 * @param date The date of the exception
 * @param timeSlots The time slots for this specific date, empty means provider is off
 */
data class ScheduleException(
    val timestamp: Timestamp = Timestamp.now(),
    val timeSlots: MutableList<TimeSlot> = mutableListOf()
) {
  constructor(
      date: LocalDateTime,
      slots: List<TimeSlot>
  ) : this(Timestamp(Date.from(date.toInstant(ZoneOffset.UTC))), slots.toMutableList())

  val date: LocalDateTime
    get() = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneOffset.UTC)
}

/** Represents the working schedule of a provider */
data class Schedule(
    val regularHours: MutableMap<String, MutableList<TimeSlot>> = mutableMapOf(),
    private val _exceptions: MutableList<ScheduleException> = mutableListOf()
) {
  val exceptions: List<ScheduleException>
    get() = _exceptions.toList()

  /** Sets the regular working hours for a specific day of the week */
  fun setRegularHours(day: DayOfWeek, timeSlots: List<TimeSlot>): Schedule {
    val newSchedule =
        this.copy(
            regularHours = regularHours.toMutableMap(), _exceptions = _exceptions.toMutableList())
    newSchedule.regularHours[day.name] = timeSlots.toMutableList()
    return newSchedule
  }

  /** Adds an exception to the regular schedule */
  fun addException(exception: ScheduleException): Schedule {
    val newSchedule =
        this.copy(
            regularHours = regularHours.toMutableMap(), _exceptions = _exceptions.toMutableList())
    newSchedule._exceptions.add(exception)
    return newSchedule
  }

  /** Removes an exception from the schedule */
  fun removeException(date: LocalDateTime): Schedule {
    val newSchedule =
        this.copy(
            regularHours = regularHours.toMutableMap(), _exceptions = _exceptions.toMutableList())
    newSchedule._exceptions.removeIf { it.date.toLocalDate() == date.toLocalDate() }
    return newSchedule
  }

  /** Checks if the provider is available at a specific date and time */
  fun isAvailable(dateTime: LocalDateTime): Boolean {
    // Check for exceptions first
    val exception = _exceptions.find { it.date.toLocalDate() == dateTime.toLocalDate() }
    if (exception != null) {
      return exception.timeSlots.any { slot ->
        dateTime.toLocalTime().isAfter(slot.start) && dateTime.toLocalTime().isBefore(slot.end)
      }
    }

    // Check regular hours
    val daySlots = regularHours[dateTime.dayOfWeek.name] ?: return false
    return daySlots.any { slot ->
      dateTime.toLocalTime().isAfter(slot.start) && dateTime.toLocalTime().isBefore(slot.end)
    }
  }

  /** Gets all available time slots for a specific date */
  fun getAvailableSlots(date: LocalDateTime): List<TimeSlot> {
    // Check for exceptions first
    val exception = _exceptions.find { it.date.toLocalDate() == date.toLocalDate() }
    if (exception != null) {
      return exception.timeSlots
    }

    // Return regular hours for that day
    return regularHours[date.dayOfWeek.name] ?: emptyList()
  }
}
