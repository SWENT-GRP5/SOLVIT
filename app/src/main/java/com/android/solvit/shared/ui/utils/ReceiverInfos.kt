package com.android.solvit.shared.ui.utils

import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.shared.model.provider.Provider
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun getReceiverName(receiver: Any): String {

  val receiverName =
      when (receiver) {
        is Provider -> (receiver as Provider).name
        is SeekerProfile -> (receiver as SeekerProfile).username
        else -> "Unknown"
      }
  return receiverName
}

fun getReceiverImageUrl(receiver: Any): String {
  val receiverPicture =
      when (receiver) {
        is Provider -> (receiver as Provider).imageUrl
        is SeekerProfile -> (receiver as SeekerProfile).imageUrl
        else -> "Unknown"
      }

  return receiverPicture
}

// Get the format date of received message(minutes, days ago, or ----/--/--)
fun formatTimestamp(timestamp: Long): String {
  val now = ZonedDateTime.now(ZoneId.systemDefault())
  val dateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())

  val minutesDifference = ChronoUnit.MINUTES.between(dateTime, now)
  val hoursDifference = ChronoUnit.HOURS.between(dateTime, now)
  val daysDifference = ChronoUnit.DAYS.between(dateTime, now)

  return when {
    // If the message was sent within the same day
    daysDifference == 0L ->
        when {
          minutesDifference < 60 -> "$minutesDifference minute(s) ago"
          hoursDifference < 24 -> "$hoursDifference hour(s) ago"
          else -> "Today"
        }

    // If the message was sent within the same week
    daysDifference < 7 -> {
      val dayOfWeek = dateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
      dayOfWeek
    }

    // Otherwise, return the date in a formatted string
    else -> {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      dateTime.format(formatter)
    }
  }
}
