package com.android.solvit.shared.model.provider

import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.util.Date
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

  @Test
  fun `setRegularHours updates existing hours`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val initialSlots = listOf(TimeSlot(9, 0, 17, 0))
    val newSlots = listOf(TimeSlot(10, 0, 15, 0))

    // Set initial hours
    schedule.setRegularHours(DayOfWeek.MONDAY.name, initialSlots)
    assertEquals(1, schedule.regularHours[DayOfWeek.MONDAY.name]?.size)
    assertEquals(9, schedule.regularHours[DayOfWeek.MONDAY.name]?.first()?.startHour)

    // Update hours
    schedule.setRegularHours(DayOfWeek.MONDAY.name, newSlots)
    assertEquals(1, schedule.regularHours[DayOfWeek.MONDAY.name]?.size)
    assertEquals(10, schedule.regularHours[DayOfWeek.MONDAY.name]?.first()?.startHour)
  }

  @Test
  fun `clearRegularHours removes hours for specific day`() {
    val schedule =
        Schedule(
            mutableMapOf(
                DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0)),
                DayOfWeek.TUESDAY.name to mutableListOf(TimeSlot(10, 0, 16, 0))),
            mutableListOf())

    // Clear Monday's hours
    schedule.clearRegularHours(DayOfWeek.MONDAY.name)
    assertNull(schedule.regularHours[DayOfWeek.MONDAY.name])
    assertNotNull(schedule.regularHours[DayOfWeek.TUESDAY.name])
  }

  @Test
  fun `getExceptions filters by date range`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date1 = LocalDateTime.of(2024, 1, 1, 0, 0)
    val date2 = LocalDateTime.of(2024, 1, 15, 0, 0)
    val date3 = LocalDateTime.of(2024, 2, 1, 0, 0)

    // Add exceptions on different dates
    schedule.addException(date1, listOf(TimeSlot(9, 0, 17, 0)), ExceptionType.OFF_TIME)
    schedule.addException(date2, listOf(TimeSlot(10, 0, 16, 0)), ExceptionType.EXTRA_TIME)
    schedule.addException(date3, listOf(TimeSlot(11, 0, 15, 0)), ExceptionType.OFF_TIME)

    // Test date range filtering
    val januaryExceptions = schedule.getExceptions(date1, date2)
    assertEquals(2, januaryExceptions.size)

    val firstWeekExceptions = schedule.getExceptions(date1, date1.plusDays(7))
    assertEquals(1, firstWeekExceptions.size)

    val februaryExceptions = schedule.getExceptions(date3, date3.plusDays(1))
    assertEquals(1, februaryExceptions.size)
  }

  @Test
  fun `getExceptions filters by type`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)

    // Add both types of exceptions
    schedule.addException(date, listOf(TimeSlot(9, 0, 12, 0)), ExceptionType.OFF_TIME)
    schedule.addException(date, listOf(TimeSlot(14, 0, 17, 0)), ExceptionType.EXTRA_TIME)

    // Test filtering by type
    val offTimeExceptions = schedule.getExceptions(date, date.plusDays(1), ExceptionType.OFF_TIME)
    assertEquals(1, offTimeExceptions.size)
    assertEquals(ExceptionType.OFF_TIME, offTimeExceptions.first().type)

    val extraTimeExceptions =
        schedule.getExceptions(date, date.plusDays(1), ExceptionType.EXTRA_TIME)
    assertEquals(1, extraTimeExceptions.size)
    assertEquals(ExceptionType.EXTRA_TIME, extraTimeExceptions.first().type)
  }

  @Test
  fun `updateException handles type change`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    val timeSlot = TimeSlot(9, 0, 17, 0)

    // Add initial off-time exception
    schedule.addException(date, listOf(timeSlot), ExceptionType.OFF_TIME)

    // Update to extra-time
    val result = schedule.updateException(date, listOf(timeSlot), ExceptionType.EXTRA_TIME)

    // Verify type change
    assertEquals(ExceptionType.EXTRA_TIME, result.exception.type)
    assertEquals(1, schedule.exceptions.size)
    assertEquals(ExceptionType.EXTRA_TIME, schedule.exceptions.first().type)
  }

  @Test
  fun `overlapping exceptions with different types remain separate`() {
    val schedule = Schedule(mutableMapOf(), mutableListOf())
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    val timeSlot1 = TimeSlot(9, 0, 13, 0)
    val timeSlot2 = TimeSlot(11, 0, 15, 0)

    // Add overlapping exceptions of different types
    schedule.addException(date, listOf(timeSlot1), ExceptionType.OFF_TIME)
    schedule.addException(date, listOf(timeSlot2), ExceptionType.EXTRA_TIME)

    // Verify exceptions remain separate
    assertEquals(2, schedule.exceptions.size)
    val exceptions = schedule.getExceptions(date, date)
    assertEquals(2, exceptions.size)
    assertTrue(
        exceptions.any { it.type == ExceptionType.OFF_TIME && it.timeSlots.first().startHour == 9 })
    assertTrue(
        exceptions.any {
          it.type == ExceptionType.EXTRA_TIME && it.timeSlots.first().startHour == 11
        })
  }

  @Test
  fun `test TimeSlot constructor validates time values`() {
    // Valid time slots should be created without exception
    TimeSlot(9, 0, 17, 0)
    TimeSlot(0, 0, 23, 59)
    TimeSlot(8, 30, 8, 45)

    // Invalid hours
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(-1, 0, 17, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(24, 0, 17, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 0, -1, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 0, 24, 0) }

    // Invalid minutes
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, -1, 17, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 60, 17, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 0, 17, -1) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 0, 17, 60) }

    // End time before or equal to start time
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(17, 0, 9, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 0, 9, 0) }
    assertThrows(IllegalArgumentException::class.java) { TimeSlot(9, 30, 9, 15) }
  }

  @Test
  fun `isTimeSlotAvailable returns true for available slot`() {
    val schedule =
        Schedule(
            mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            mutableListOf(),
            emptyList())
    val slot = TimeSlot(10, 0, 11, 0)
    val date = LocalDate.of(2024, 1, 1) // Monday

    assertTrue(schedule.isTimeSlotAvailable(slot, date))
  }

  @Test
  fun `isTimeSlotAvailable returns false for unavailable slot due to accepted request`() {
    val acceptedSlot =
        AcceptedTimeSlot(
            requestId = "req1",
            startTime =
                Timestamp(
                    Date.from(
                        LocalDateTime.of(2024, 1, 1, 10, 0)
                            .atZone(ZoneId.systemDefault())
                            .toInstant())),
            duration = 60)
    val schedule =
        Schedule(
            mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            mutableListOf(),
            listOf(acceptedSlot))
    val slot = TimeSlot(10, 0, 11, 0)
    val date = LocalDate.of(2024, 1, 1) // Monday

    assertFalse(schedule.isTimeSlotAvailable(slot, date))
  }

  @Test
  fun `isTimeSlotAvailable returns false for slot during off-time exception`() {
    val schedule =
        Schedule(
            mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            mutableListOf(
                ScheduleException(
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    listOf(TimeSlot(10, 0, 11, 0)),
                    ExceptionType.OFF_TIME)),
            emptyList())
    val slot = TimeSlot(10, 0, 11, 0)
    val date = LocalDate.of(2024, 1, 1) // Monday

    assertFalse(schedule.isTimeSlotAvailable(slot, date))
  }

  @Test
  fun `getProviderAvailabilities returns correct slots for available day`() {
    val schedule =
        Schedule(
            mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            mutableListOf(),
            emptyList())
    val date = LocalDate.of(2024, 1, 1) // Monday
    val availabilities = schedule.getProviderAvailabilities(date)

    assertEquals(8, availabilities.size)
    assertTrue(availabilities.contains(TimeSlot(9, 0, 10, 0)))
    assertTrue(availabilities.contains(TimeSlot(16, 0, 17, 0)))
  }

  @Test
  fun `getProviderAvailabilities excludes slots during accepted requests`() {
    val acceptedSlot1 =
        AcceptedTimeSlot(
            requestId = "req1",
            startTime =
                Timestamp(
                    Date.from(
                        LocalDateTime.of(2024, 1, 1, 10, 0)
                            .atZone(ZoneId.systemDefault())
                            .toInstant())),
            duration = 60)
    val acceptedSlot2 =
        AcceptedTimeSlot(
            requestId = "req2",
            startTime =
                Timestamp(
                    Date.from(
                        LocalDateTime.of(2024, 1, 1, 16, 0)
                            .atZone(ZoneId.systemDefault())
                            .toInstant())),
            duration = 60)
    val schedule =
        Schedule(
            mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            mutableListOf(),
            listOf(acceptedSlot1, acceptedSlot2))
    val date = LocalDate.of(2024, 1, 1) // Monday
    val availabilities = schedule.getProviderAvailabilities(date)
    assertEquals(6, availabilities.size)
    assertFalse(availabilities.contains(TimeSlot(10, 0, 11, 0)))
    assertFalse(availabilities.contains(TimeSlot(16, 0, 17, 0)))
  }

  @Test
  fun `getNextFiveSlots returns correct slots`() {
    val schedule =
        Schedule(
            mutableMapOf(DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 17, 0))),
            mutableListOf(),
            emptyList())
    val startDate = LocalDate.of(2024, 1, 1) // Monday
    val slots = schedule.getNextFiveSlots(startDate)

    assertEquals(5, slots.size)
    assertTrue(slots.contains(TimeSlot(9, 0, 10, 0)))
    assertTrue(slots.contains(TimeSlot(13, 0, 14, 0)))
  }

  @Test
  fun `getNextFiveSlots spans multiple days`() {
    val schedule =
        Schedule(
            mutableMapOf(
                DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(9, 0, 12, 0)),
                DayOfWeek.TUESDAY.name to mutableListOf(TimeSlot(14, 0, 17, 0))),
            mutableListOf(),
            emptyList())
    val startDate = LocalDate.of(2024, 1, 1) // Monday
    val slots = schedule.getNextFiveSlots(startDate)

    assertEquals(5, slots.size)
    assertTrue(slots.contains(TimeSlot(9, 0, 10, 0)))
    assertTrue(slots.contains(TimeSlot(14, 0, 15, 0))) // From Tuesday
  }

  @Test
  fun `complex schedule with exceptions and accepted requests tests edge cases`() {
    val regularHours =
        mutableMapOf(
            DayOfWeek.MONDAY.name to mutableListOf(TimeSlot(8, 0, 18, 0)),
            DayOfWeek.TUESDAY.name to mutableListOf(TimeSlot(8, 0, 18, 0)),
            DayOfWeek.WEDNESDAY.name to mutableListOf(TimeSlot(8, 0, 18, 0)),
            DayOfWeek.THURSDAY.name to mutableListOf(TimeSlot(8, 0, 18, 0)),
            DayOfWeek.FRIDAY.name to mutableListOf(TimeSlot(8, 0, 18, 0)),
            DayOfWeek.SATURDAY.name to mutableListOf(TimeSlot(10, 0, 16, 0)),
            DayOfWeek.SUNDAY.name to mutableListOf(TimeSlot(10, 0, 14, 0)))

    val exceptions =
        mutableListOf(
            ScheduleException(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                listOf(TimeSlot(12, 0, 14, 0)), // OFF_TIME on Monday
                ExceptionType.OFF_TIME),
            ScheduleException(
                LocalDateTime.of(2024, 1, 2, 0, 0),
                listOf(TimeSlot(18, 0, 20, 0)), // EXTRA_TIME on Tuesday
                ExceptionType.EXTRA_TIME))

    // Add accepted requests
    val acceptedRequests =
        listOf(
            AcceptedTimeSlot(
                requestId = "req1",
                startTime =
                    Timestamp(
                        Date.from(
                            LocalDateTime.of(2024, 1, 1, 9, 0)
                                .atZone(ZoneId.systemDefault())
                                .toInstant())),
                duration = 60),
            AcceptedTimeSlot(
                requestId = "req2",
                startTime =
                    Timestamp(
                        Date.from(
                            LocalDateTime.of(2024, 1, 1, 15, 0)
                                .atZone(ZoneId.systemDefault())
                                .toInstant())),
                duration = 120),
            AcceptedTimeSlot(
                requestId = "req3",
                startTime =
                    Timestamp(
                        Date.from(
                            LocalDateTime.of(2024, 1, 2, 11, 0)
                                .atZone(ZoneId.systemDefault())
                                .toInstant())),
                duration = 60),
            AcceptedTimeSlot(
                requestId = "req4",
                startTime =
                    Timestamp(
                        Date.from(
                            LocalDateTime.of(2024, 1, 3, 13, 0)
                                .atZone(ZoneId.systemDefault())
                                .toInstant())),
                duration = 90))

    val schedule = Schedule(regularHours, exceptions, acceptedRequests)

    assertFalse(
        schedule.isTimeSlotAvailable(
            TimeSlot(9, 0, 10, 0), LocalDate.of(2024, 1, 1))) // Occupied by req1
    assertTrue(
        schedule.isTimeSlotAvailable(TimeSlot(10, 0, 11, 0), LocalDate.of(2024, 1, 1))) // Free
    assertFalse(
        schedule.isTimeSlotAvailable(TimeSlot(12, 0, 13, 0), LocalDate.of(2024, 1, 1))) // OFF_TIME

    val tuesdayAvailabilities = schedule.getProviderAvailabilities(LocalDate.of(2024, 1, 2))
    assertTrue(tuesdayAvailabilities.contains(TimeSlot(8, 0, 9, 0))) // Regular hours
    assertFalse(tuesdayAvailabilities.contains(TimeSlot(11, 0, 12, 0))) // Occupied by req3
    // assertTrue(tuesdayAvailabilities.contains(TimeSlot(18, 0, 19, 0))) // EXTRA_TIME

    val nextFiveSlots = schedule.getNextFiveSlots(LocalDate.of(2024, 1, 1))
    assertEquals(5, nextFiveSlots.size)
    assertTrue(nextFiveSlots.contains(TimeSlot(10, 0, 11, 0))) // Monday
    // assertTrue(nextFiveSlots.contains(TimeSlot(18, 0, 19, 0))) // Tuesday EXTRA_TIME
    assertTrue(nextFiveSlots.contains(TimeSlot(8, 0, 9, 0))) // Wednesday

    val allAvailabilitiesMonday = schedule.getProviderAvailabilities(LocalDate.of(2024, 1, 1))
    assertFalse(allAvailabilitiesMonday.contains(TimeSlot(12, 0, 13, 0))) // OFF_TIME
    assertFalse(allAvailabilitiesMonday.contains(TimeSlot(9, 0, 10, 0))) // Accepted request
  }
}
