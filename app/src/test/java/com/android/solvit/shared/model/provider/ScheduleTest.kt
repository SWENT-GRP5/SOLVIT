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
  fun testIsAvailableWithException() {
    val schedule =
        Schedule(
            regularHours = mapOf(),
            exceptions =
                listOf(
                    ScheduleException(
                        LocalDateTime.of(2024, 1, 1, 10, 0), listOf(TimeSlot(9, 0, 17, 0)))))

    // Test availability within exception time slot
    val timeInSlot = LocalDateTime.of(2024, 1, 1, 10, 30) // 10:30 AM
    assertTrue(schedule.isAvailable(timeInSlot))

    // Test availability outside exception time slot
    val timeOutsideSlot = LocalDateTime.of(2024, 1, 1, 8, 0) // 8:00 AM
    assertFalse(schedule.isAvailable(timeOutsideSlot))
  }

  @Test
  fun testIsAvailableWithRegularHours() {
    val schedule =
        Schedule(
            regularHours = mapOf("MONDAY" to listOf(TimeSlot(9, 0, 17, 0))), exceptions = listOf())

    // Test availability within regular hours
    val timeInSlot = LocalDateTime.of(2024, 1, 1, 10, 30) // Monday 10:30 AM
    assertTrue(schedule.isAvailable(timeInSlot))

    // Test availability outside regular hours
    val timeOutsideSlot = LocalDateTime.of(2024, 1, 1, 8, 0) // Monday 8:00 AM
    assertFalse(schedule.isAvailable(timeOutsideSlot))

    // Test availability on different day
    val differentDay = LocalDateTime.of(2024, 1, 2, 10, 30) // Tuesday 10:30 AM
    assertFalse(schedule.isAvailable(differentDay))
  }

  @Test
  fun testGetAvailableSlots() {
    val mondaySlots = listOf(TimeSlot(9, 0, 12, 0), TimeSlot(13, 0, 17, 0))
    val schedule = Schedule(regularHours = mapOf("MONDAY" to mondaySlots), exceptions = listOf())

    // Test getting slots for a Monday
    val monday = LocalDateTime.of(2024, 1, 1, 0, 0)
    val mondayAvailableSlots = schedule.getAvailableSlots(monday)
    assertEquals(mondaySlots, mondayAvailableSlots)

    // Test getting slots for a day with no hours
    val tuesday = LocalDateTime.of(2024, 1, 2, 0, 0)
    val tuesdayAvailableSlots = schedule.getAvailableSlots(tuesday)
    assertTrue(tuesdayAvailableSlots.isEmpty())
  }

  @Test
  fun testGetAvailableSlotsWithException() {
    val regularSlots = listOf(TimeSlot(9, 0, 17, 0))
    val exceptionSlots = listOf(TimeSlot(14, 0, 18, 0))
    val schedule =
        Schedule(
            regularHours = mapOf("MONDAY" to regularSlots),
            exceptions =
                listOf(ScheduleException(LocalDateTime.of(2024, 1, 1, 0, 0), exceptionSlots)))

    // Test getting slots for the exception day
    val exceptionDay = LocalDateTime.of(2024, 1, 1, 0, 0)
    val exceptionDaySlots = schedule.getAvailableSlots(exceptionDay)
    assertEquals(exceptionSlots, exceptionDaySlots)

    // Test getting slots for a regular day
    val regularDay = LocalDateTime.of(2024, 1, 8, 0, 0)
    val regularDaySlots = schedule.getAvailableSlots(regularDay)
    assertEquals(regularSlots, regularDaySlots)
  }
}
