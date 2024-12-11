package com.android.solvit.shared.model.provider

import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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
    require(startHour in 0..23) { "Start hour must be between 0 and 23" }
    require(startMinute in 0..59) { "Start minute must be between 0 and 59" }
    require(endHour in 0..23) { "End hour must be between 0 and 23" }
    require(endMinute in 0..59) { "End minute must be between 0 and 59" }
    require(startHour < endHour || (startHour == endHour && startMinute < endMinute)) {
      "Start time must be before end time"
    }
  }

  /** Check if this time slot overlaps with another */
  fun overlaps(other: TimeSlot): Boolean {
    return !(end.isBefore(other.start) || start.isAfter(other.end))
  }

  /** Merge this time slot with another overlapping slot */
  fun merge(other: TimeSlot): TimeSlot {
    require(this.overlaps(other)) { "Cannot merge non-overlapping time slots" }
    return TimeSlot(
        start = if (this.start.isBefore(other.start)) this.start else other.start,
        end = if (this.end.isAfter(other.end)) this.end else other.end)
  }
}

/** New data class for storing essential service request info */
data class AcceptedTimeSlot(
    val requestId: String,
    val startTime: Timestamp,
    val duration: Int = 60 // minutes, fixed at 1 hour
)

/**
 * Represents an exception to the regular schedule
 *
 * @param date The date of the exception
 * @param timeSlots The time slots for this specific date
 * @param type The type of exception (OFF_TIME or EXTRA_TIME)
 */
data class ScheduleException(
    private val _timestamp: Timestamp,
    private val _timeSlots: MutableList<TimeSlot>,
    val type: ExceptionType
) {
  constructor(
      date: LocalDateTime,
      slots: List<TimeSlot>,
      type: ExceptionType
  ) : this(
      Timestamp(Date.from(date.atZone(ZoneId.systemDefault()).toInstant())),
      slots.toMutableList(),
      type)

  constructor(
      slots: List<TimeSlot>,
      timestamp: Timestamp,
      type: ExceptionType
  ) : this(timestamp, slots.toMutableList(), type)

  val timestamp: Timestamp
    get() = _timestamp

  val timeSlots: MutableList<TimeSlot>
    get() = _timeSlots

  val date: LocalDateTime
    get() = LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())

  /** Merge this exception with another of the same type */
  fun merge(other: ScheduleException): ScheduleException {
    require(this.type == other.type) { "Cannot merge exceptions of different types" }
    require(this.date.toLocalDate() == other.date.toLocalDate()) {
      "Cannot merge exceptions from different dates"
    }

    val mergedSlots =
        (this.timeSlots + other.timeSlots)
            .sortedBy { it.start }
            .fold(mutableListOf<TimeSlot>()) { acc, slot ->
              if (acc.isEmpty() || !acc.last().overlaps(slot)) {
                acc.add(slot)
              } else {
                acc[acc.lastIndex] = acc.last().merge(slot)
              }
              acc
            }

    return ScheduleException(this.date, mergedSlots, this.type)
  }
}

/** Type of schedule exception */
enum class ExceptionType {
  OFF_TIME, // Provider is not working during these hours
  EXTRA_TIME // Provider is working additional hours
}

/** Result of adding or updating an exception, including feedback for the user */
data class ExceptionUpdateResult(
    val exception: ScheduleException,
    val mergedWith: List<ScheduleException> = emptyList(),
    val isUpdate: Boolean = false
)

/** Represents the working schedule of a provider */
data class Schedule(
    val regularHours: MutableMap<String, MutableList<TimeSlot>> = mutableMapOf(),
    val exceptions: MutableList<ScheduleException> = mutableListOf(),
    val acceptedTimeSlots: List<AcceptedTimeSlot> = emptyList()
) {
  /** Checks if the provider is available at a specific date and time */
  fun isAvailable(dateTime: LocalDateTime): Boolean {
    val time = dateTime.toLocalTime()

    // Check for exceptions first
    val exception = exceptions.find { it.date.toLocalDate() == dateTime.toLocalDate() }
    if (exception != null) {
      when (exception.type) {
        ExceptionType.OFF_TIME -> {
          // If there's an off-time exception overlapping with the time, provider is not available
          if (exception.timeSlots.any { slot ->
            (time.equals(slot.start) || time.isAfter(slot.start)) &&
                (time.equals(slot.end) || time.isBefore(slot.end))
          }) {
            return false
          }
        }
        ExceptionType.EXTRA_TIME -> {
          // If there's an extra-time exception overlapping with the time, provider is available
          if (exception.timeSlots.any { slot ->
            (time.equals(slot.start) || time.isAfter(slot.start)) &&
                (time.equals(slot.end) || time.isBefore(slot.end))
          }) {
            return true
          }
        }
      }
    }

    // Check regular hours
    val daySlots = regularHours[dateTime.dayOfWeek.name] ?: return false
    return daySlots.any { slot ->
      (time.equals(slot.start) || time.isAfter(slot.start)) &&
          (time.equals(slot.end) || time.isBefore(slot.end))
    }
  }

  /** Add a new exception to the schedule */
  fun addException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      type: ExceptionType
  ): ExceptionUpdateResult {
    val newException = ScheduleException(date, timeSlots, type)

    // Find existing exceptions of the same type on the same date
    val existingExceptions =
        exceptions.filter { it.date.toLocalDate() == date.toLocalDate() && it.type == type }

    if (existingExceptions.isEmpty()) {
      exceptions.add(newException)
      return ExceptionUpdateResult(newException)
    }

    // Merge with existing exceptions
    val mergedException =
        existingExceptions.fold(newException) { acc, existing ->
          exceptions.remove(existing)
          acc.merge(existing)
        }

    exceptions.add(mergedException)
    return ExceptionUpdateResult(mergedException, existingExceptions)
  }

  /** Update an existing exception or add a new one if none exists */
  fun updateException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      type: ExceptionType
  ): ExceptionUpdateResult {
    // Remove any existing exception on the same date
    val existingException = exceptions.find { it.date.toLocalDate() == date.toLocalDate() }

    if (existingException != null) {
      exceptions.remove(existingException)
    }

    // Add the new exception
    val newException = ScheduleException(date, timeSlots, type)
    exceptions.add(newException)

    return ExceptionUpdateResult(
        newException, listOfNotNull(existingException), existingException != null)
  }

  /** Merges overlapping time slots in a list */
  private fun mergeOverlappingSlots(slots: List<TimeSlot>): List<TimeSlot> {
    if (slots.isEmpty()) return emptyList()

    // Sort slots by start time and filter out invalid slots
    val validSlots =
        slots
            .filter { slot ->
              slot.startHour in 0..23 &&
                  slot.endHour in 0..23 &&
                  slot.startMinute in 0..59 &&
                  slot.endMinute in 0..59 &&
                  (slot.startHour < slot.endHour ||
                      (slot.startHour == slot.endHour && slot.startMinute < slot.endMinute))
            }
            .sortedBy { it.start }

    if (validSlots.isEmpty()) return emptyList()

    val mergedSlots = mutableListOf<TimeSlot>()
    var currentSlot = validSlots[0]

    for (i in 1 until validSlots.size) {
      val nextSlot = validSlots[i]
      if (currentSlot.overlaps(nextSlot)) {
        currentSlot = currentSlot.merge(nextSlot)
      } else {
        mergedSlots.add(currentSlot)
        currentSlot = nextSlot
      }
    }
    mergedSlots.add(currentSlot)

    return mergedSlots
  }

  /** Sets regular working hours for a specific day */
  fun setRegularHours(dayOfWeek: String, timeSlots: List<TimeSlot>) {
    require(dayOfWeek in DayOfWeek.values().map { it.name }) { "Invalid day of week" }
    regularHours[dayOfWeek] = mergeOverlappingSlots(timeSlots).toMutableList()
  }

  /** Removes all regular working hours for a specific day */
  fun clearRegularHours(dayOfWeek: String) {
    require(dayOfWeek in DayOfWeek.values().map { it.name }) { "Invalid day of week" }
    regularHours.remove(dayOfWeek)
  }

  /** Gets all exceptions within a date range */
  fun getExceptions(startDate: LocalDateTime, endDate: LocalDateTime): List<ScheduleException> {
    return exceptions.filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) }
  }

  /** Gets all exceptions of a specific type within a date range */
  fun getExceptions(
      startDate: LocalDateTime,
      endDate: LocalDateTime,
      type: ExceptionType? = null
  ): List<ScheduleException> {
    return exceptions.filter { exception ->
      (!exception.date.isBefore(startDate) &&
          !exception.date.isAfter(endDate) &&
          (type == null || exception.type == type))
    }
  }

  /** Check if there's a conflict between regular hours and an exception */
  private fun hasConflict(date: LocalDateTime, timeSlots: List<TimeSlot>): Boolean {
    val regularSlots = regularHours[date.dayOfWeek.name] ?: return false
    return timeSlots.any { newSlot -> regularSlots.any { it.overlaps(newSlot) } }
  }

  /** Check if a timeslot is available */
  fun isTimeSlotAvailable(timeSlot: TimeSlot, localDate: LocalDate): Boolean {
    if (!isAvailable(LocalDateTime.of(localDate, timeSlot.start))) return false

    // Check Conflicts with accepted requests
    return acceptedTimeSlots.none { accepted ->
      val acceptedStart =
          accepted.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
      val acceptedEnd = acceptedStart.plusMinutes(accepted.duration.toLong())

      timeSlot.overlaps(
          TimeSlot(
              startHour = acceptedStart.hour,
              startMinute = acceptedStart.minute,
              endHour = acceptedEnd.hour,
              endMinute = acceptedEnd.minute))
    }
  }

  /**
   * Retrieve availabilities of a provider within a day
   *
   * @param date the day within we want to check provider availabilities
   * @return list of available time slots
   */
  fun getProviderAvailabilities(date: LocalDate): List<TimeSlot> {

    val timeSlots = mutableListOf<TimeSlot>()

    val regularHours = regularHours[date.dayOfWeek.name]
    regularHours?.forEach {
      var currentTime = it.start
      while (currentTime < it.end) {
        val endTime = currentTime.plusHours(1)
        if (isTimeSlotAvailable(
            TimeSlot(
                startHour = currentTime.hour,
                startMinute = currentTime.minute,
                endHour = endTime.hour,
                endMinute = endTime.minute),
            localDate = date)) {
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
