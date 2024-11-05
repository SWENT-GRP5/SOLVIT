package com.android.solvit.shared.model.request

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp

data class ServiceRequest(
    val uid: String,
    val title: String,
    val type: Services,
    val description: String,
    val userId: String,
    val dueDate: Timestamp,
    val location: Location?,
    val imageUrl: String?,
    val status: ServiceRequestStatus
)

enum class ServiceRequestStatus {
  PENDING,
  ACCEPTED,
  STARTED,
  ENDED,
  ARCHIVED
}
