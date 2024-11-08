package com.android.solvit.shared.model.jobs

import com.google.firebase.firestore.GeoPoint
import java.time.LocalDate
import java.time.LocalTime

data class Job(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "",
    val location: GeoPoint? = null, // Firestore GeoPoint for location
    val date: LocalDate? = null, // Firestore Timestamp for date
    val time: LocalTime? = null, // Firestore Timestamp for time
    val locationName: String = ""
) {
  // fun getDateAsLocalDate(): LocalDate? =
  // date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
  // fun getTimeAsLocalTime(): LocalTime? =
  // time?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
}
