package com.android.solvit.seeker.model.provider

import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.Schedule
import com.android.solvit.shared.model.provider.TimeSlot
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before

class BookProviderTest {
  private lateinit var viewModel: BookProvider
  private val testProvider =
      Provider(
          name = "Test Provider",
          schedule =
              Schedule(
                  regularHours =
                      mutableMapOf(
                          "WEDNESDAY" to
                              mutableListOf(
                                  TimeSlot(
                                      startHour = 8, startMinute = 0, endHour = 12, endMinute = 0),
                                  TimeSlot(
                                      startHour = 13,
                                      startMinute = 0,
                                      endHour = 17,
                                      endMinute = 0)))))

  @Before
  fun setUp() {
    viewModel = BookProvider()
  }

  @Test
  fun `setProvider updates currentProvider`() = runTest {
    viewModel.setProvider(testProvider)
    val provider = viewModel.currentProvider.first()
    assertEquals(testProvider, provider)
  }

  @Test
  fun `getProviderAvailabilities returns correct time slots`() {
    viewModel.setProvider(testProvider)

    val date = LocalDate.of(2024, 12, 11)
    val expectedTimeSlots =
        listOf(
            TimeSlot(startHour = 8, startMinute = 0, endHour = 9, endMinute = 0),
            TimeSlot(startHour = 9, startMinute = 0, endHour = 10, endMinute = 0),
            TimeSlot(startHour = 10, startMinute = 0, endHour = 11, endMinute = 0),
            TimeSlot(startHour = 11, startMinute = 0, endHour = 12, endMinute = 0),
            TimeSlot(startHour = 13, startMinute = 0, endHour = 14, endMinute = 0),
            TimeSlot(startHour = 14, startMinute = 0, endHour = 15, endMinute = 0),
            TimeSlot(startHour = 15, startMinute = 0, endHour = 16, endMinute = 0),
            TimeSlot(startHour = 16, startMinute = 0, endHour = 17, endMinute = 0))

    val timeSlots = viewModel.getProviderAvailabilities(date)
    assertEquals(expectedTimeSlots, timeSlots)
  }

  @Test
  fun `getNextFiveSlots returns correct number of slots`() {
    viewModel.setProvider(testProvider)

    val startDate = LocalDate.of(2024, 12, 11)
    val nextFiveSlots = viewModel.getNextFiveSlots(startDate)

    // Only five slots should be returned
    assertEquals(5, nextFiveSlots.size)
    val timeSlots = viewModel.getProviderAvailabilities(startDate)
    assertEquals(nextFiveSlots, timeSlots)
  }
}
