package com.android.solvit.shared.model.provider

import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class ScheduleTest {
  @Test
  fun testTimeSlotConstructorWithLocalTime() {
    val start = LocalTime.of(9, 0)
    val end = LocalTime.of(17, 0)
    val timeSlot = TimeSlot(start, end)

    assertEquals(9, timeSlot.startHour)
    assertEquals(0, timeSlot.startMinute)
    assertEquals(17, timeSlot.endHour)
    assertEquals(0, timeSlot.endMinute)
  }

  @Test
  fun testTimeSlotConstructorWithHoursAndMinutes() {
    val timeSlot = TimeSlot(9, 30, 17, 45)

    assertEquals(9, timeSlot.startHour)
    assertEquals(30, timeSlot.startMinute)
    assertEquals(17, timeSlot.endHour)
    assertEquals(45, timeSlot.endMinute)
  }

  @Test
  fun testTimeSlotDefaultConstructor() {
    val timeSlot = TimeSlot()

    assertEquals(0, timeSlot.startHour)
    assertEquals(0, timeSlot.startMinute)
    assertEquals(1, timeSlot.endHour) // Default 1-hour duration
    assertEquals(0, timeSlot.endMinute)

    // Verify LocalTime getters
    assertEquals(LocalTime.of(0, 0), timeSlot.start)
    assertEquals(LocalTime.of(1, 0), timeSlot.end)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testTimeSlotInvalidTimes() {
    TimeSlot(17, 0, 9, 0) // End time before start time
  }

  @Test(expected = IllegalArgumentException::class)
  fun testTimeSlotSameTimeInvalidMinutes() {
    TimeSlot(9, 30, 9, 0) // Same hour but end minute before start minute
  }

  @Test
  fun testTimeSlotGetters() {
    val timeSlot = TimeSlot(9, 30, 17, 45)

    assertEquals(LocalTime.of(9, 30), timeSlot.start)
    assertEquals(LocalTime.of(17, 45), timeSlot.end)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testTimeSlotSameTimeEqualMinutes() {
    TimeSlot(9, 30, 9, 30) // Same time is not allowed
  }

  @Test
  fun testScheduleExceptions() {
    val schedule = Schedule()
    val date = LocalDateTime.now()
    val timeSlots = listOf(TimeSlot(9, 0, 17, 0))
    val exception = ScheduleException(date, timeSlots)

    // Add exception and get new schedule
    val updatedSchedule = schedule.addException(exception)

    // Test exceptions getter
    assertEquals(1, updatedSchedule.exceptions.size)
    assertEquals(exception, updatedSchedule.exceptions[0])
  }

  @Test
  fun testIsAvailableWithException() {
    var schedule = Schedule()
    val date = LocalDateTime.of(2024, 1, 1, 10, 0) // 10:00 AM
    val timeSlots = listOf(TimeSlot(9, 0, 17, 0))
    val exception = ScheduleException(date, timeSlots)

    // Add exception and update schedule
    schedule = schedule.addException(exception)

    // Test availability within exception time slot
    val timeInSlot = LocalDateTime.of(2024, 1, 1, 10, 30) // 10:30 AM
    assertTrue(schedule.isAvailable(timeInSlot))

    // Test availability outside exception time slot
    val timeOutsideSlot = LocalDateTime.of(2024, 1, 1, 8, 0) // 8:00 AM
    assertFalse(schedule.isAvailable(timeOutsideSlot))
  }

  @Test
  fun testIsAvailableWithExceptionEmptySlots() {
    var schedule = Schedule()
    val date = LocalDateTime.of(2024, 1, 1, 10, 0)
    val exception = ScheduleException(date, emptyList()) // Provider is off

    // Add exception and update schedule
    schedule = schedule.addException(exception)

    // Test availability on exception date
    val timeOnDate = LocalDateTime.of(2024, 1, 1, 10, 30)
    assertFalse(schedule.isAvailable(timeOnDate))
  }
}
