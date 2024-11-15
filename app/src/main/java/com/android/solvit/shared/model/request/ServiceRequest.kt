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
    val providerId: String? = null,
    val dueDate: Timestamp,
    val meetingDate: Timestamp? = null,
    val location: Location?,
    val imageUrl: String? = null,
    val packageId: String? = null,
    val agreedPrice: Double? = null,
    val status: ServiceRequestStatus = ServiceRequestStatus.PENDING,
)

enum class ServiceRequestStatus {
  PENDING,
  ACCEPTED,
  SCHEDULED,
  ENDED,
  ARCHIVED;

  companion object {
    fun format(status: ServiceRequestStatus): String {
      return status.name.lowercase().replaceFirstChar { it.uppercase() }
    }
  }
}
