package com.android.solvit.shared.model.provider

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Month
import org.junit.Assert.*
import org.junit.Test

class ScheduleTest {
  private val dec3 = LocalDateTime.of(2023, Month.DECEMBER, 3, 0, 0)

  @Test
  fun `time slots merge correctly when overlapping`() {
    val slot1 = TimeSlot(13, 0, 15, 0)
    val slot2 = TimeSlot(14, 0, 16, 0)
    val merged = slot1.merge(slot2)

    assertEquals(13, merged.startHour)
    assertEquals(0, merged.startMinute)
    assertEquals(16, merged.endHour)
    assertEquals(0, merged.endMinute)
  }

  @Test
  fun `adding overlapping off time exceptions results in merged slots`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)

    // Add first off-time exception (1 PM - 3 PM)
    val result1 =
        schedule.addException(date, listOf(TimeSlot(13, 0, 15, 0)), ExceptionType.OFF_TIME)

    // Verify initial state
    assertEquals(1, result1.exception.timeSlots.size)
    assertTrue(result1.mergedWith.isEmpty())

    // Add overlapping off-time (2 PM - 4 PM)
    val result2 =
        schedule.addException(date, listOf(TimeSlot(14, 0, 16, 0)), ExceptionType.OFF_TIME)

    // Verify merge result
    assertEquals(1, result2.exception.timeSlots.size)
    assertEquals(13, result2.exception.timeSlots[0].startHour)
    assertEquals(16, result2.exception.timeSlots[0].endHour)
    assertEquals(1, result2.mergedWith.size)
  }

  @Test
  fun `different types of exceptions on same day remain separate`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    val timeSlot = TimeSlot(13, 0, 15, 0)

    // Add off-time exception
    schedule.addException(date, listOf(timeSlot), ExceptionType.OFF_TIME)

    // Add extra-time exception for same period
    schedule.addException(date, listOf(timeSlot), ExceptionType.EXTRA_TIME)

    // Get all exceptions for the day
    val exceptions = schedule.getExceptions(date, date)

    // Verify both exceptions exist separately
    assertEquals(2, exceptions.size)
    assertTrue(exceptions.any { it.type == ExceptionType.OFF_TIME })
    assertTrue(exceptions.any { it.type == ExceptionType.EXTRA_TIME })
  }

  @Test
  fun `updating exception replaces existing one`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)

    // Add initial exception
    schedule.addException(date, listOf(TimeSlot(9, 0, 11, 0)), ExceptionType.OFF_TIME)

    // Update with new time slots
    val result =
        schedule.updateException(date, listOf(TimeSlot(14, 0, 16, 0)), ExceptionType.OFF_TIME)

    // Verify update result
    assertEquals(1, result.exception.timeSlots.size)
    assertEquals(14, result.exception.timeSlots[0].startHour)
    assertEquals(16, result.exception.timeSlots[0].endHour)
    assertEquals(1, result.mergedWith.size)
    assertEquals(1, schedule.exceptions.size)
  }

  @Test
  fun `adding overlapping exceptions merges them`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)

    // Add initial exception
    schedule.addException(date, listOf(TimeSlot(9, 0, 11, 0)), ExceptionType.OFF_TIME)

    // Add overlapping exception
    val result = schedule.addException(date, listOf(TimeSlot(10, 0, 12, 0)), ExceptionType.OFF_TIME)

    // Verify merge result
    assertEquals(1, result.exception.timeSlots.size)
    assertEquals(9, result.exception.timeSlots[0].startHour)
    assertEquals(12, result.exception.timeSlots[0].endHour)
    assertEquals(1, result.mergedWith.size)
    assertEquals(1, schedule.exceptions.size)
  }

  @Test
  fun `non-overlapping slots in same exception remain separate`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)

    // Add multiple non-overlapping slots
    val result =
        schedule.addException(
            date, listOf(TimeSlot(9, 0, 11, 0), TimeSlot(13, 0, 15, 0)), ExceptionType.OFF_TIME)

    // Verify slots remain separate
    assertEquals(2, result.exception.timeSlots.size)
    assertTrue(result.exception.timeSlots.any { it.startHour == 9 && it.endHour == 11 })
    assertTrue(result.exception.timeSlots.any { it.startHour == 13 && it.endHour == 15 })
    assertEquals(0, result.mergedWith.size)
  }

  @Test
  fun `isAvailable respects off time exceptions`() {
    val schedule =
        Schedule(mutableMapOf("MONDAY" to mutableListOf(TimeSlot(9, 0, 17, 0))), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 10, 0) // A Monday

    // Initially available during regular hours
    assertTrue(schedule.isAvailable(date))

    // Add off-time exception
    schedule.addException(date, listOf(TimeSlot(10, 0, 12, 0)), ExceptionType.OFF_TIME)

    // Should be unavailable during exception
    assertFalse(schedule.isAvailable(date))
    assertTrue(schedule.isAvailable(date.withHour(9)))
    assertTrue(schedule.isAvailable(date.withHour(13)))
  }

  @Test
  fun `isAvailable respects extra time exceptions`() {
    val schedule =
        Schedule(mutableMapOf("MONDAY" to mutableListOf(TimeSlot(9, 0, 17, 0))), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 18, 0) // A Monday

    // Initially unavailable outside regular hours
    assertFalse(schedule.isAvailable(date))

    // Add extra-time exception
    schedule.addException(date, listOf(TimeSlot(17, 0, 19, 0)), ExceptionType.EXTRA_TIME)

    // Should be available during exception
    assertTrue(schedule.isAvailable(date))
    assertFalse(schedule.isAvailable(date.withHour(20)))
  }

  @Test
  fun `regular hours are respected when setting exceptions`() {
    val regularHours = mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0)))
    val schedule = Schedule(regularHours, mutableListOf())
    val monday = LocalDateTime.of(2024, 1, 1, 10, 0) // A Monday at 10:00

    // Initially available during regular hours
    assertTrue(schedule.isAvailable(monday))
    assertFalse(schedule.isAvailable(monday.withHour(8)))
    assertFalse(schedule.isAvailable(monday.withHour(18)))

    // Add off-time exception during regular hours
    val result =
        schedule.addException(monday, listOf(TimeSlot(13, 0, 15, 0)), ExceptionType.OFF_TIME)

    // Verify the exception was added
    assertEquals(1, result.exception.timeSlots.size)
    assertEquals(13, result.exception.timeSlots[0].startHour)
    assertEquals(15, result.exception.timeSlots[0].endHour)

    // Verify availability with exception
    assertTrue(schedule.isAvailable(monday)) // 10:00 is outside exception
    assertFalse(schedule.isAvailable(monday.withHour(14))) // During exception
    assertTrue(schedule.isAvailable(monday.withHour(16))) // After exception, during regular hours
    assertFalse(schedule.isAvailable(monday.withHour(19))) // Outside regular hours
  }
}
