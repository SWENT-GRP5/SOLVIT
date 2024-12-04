package com.android.solvit.shared.model.request

import androidx.compose.ui.graphics.Color
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.theme.ACCEPTED_color
import com.android.solvit.shared.ui.theme.ARCHIVED_color
import com.android.solvit.shared.ui.theme.CANCELLED_color
import com.android.solvit.shared.ui.theme.ENDED_color
import com.android.solvit.shared.ui.theme.PENDING_color
import com.android.solvit.shared.ui.theme.STARTED_color
import com.google.firebase.Timestamp

data class ServiceRequest(
    val uid: String = "",
    val title: String = "",
    val type: Services = Services.TUTOR,
    val description: String = "",
    val userId: String = "",
    val providerId: String? = null,
    val dueDate: Timestamp = Timestamp.now(),
    val meetingDate: Timestamp? = null,
    val location: Location? = Location(0.0, 0.0, ""),
    val imageUrl: String? = null,
    val packageId: String? = null,
    val agreedPrice: Double? = null,
    val status: ServiceRequestStatus = ServiceRequestStatus.PENDING,
)

enum class ServiceRequestStatus {
  PENDING,
  ACCEPTED,
  SCHEDULED,
  COMPLETED,
  CANCELED,
  ARCHIVED;

  companion object {
    fun format(status: ServiceRequestStatus): String {
      return status.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun getStatusColor(status: ServiceRequestStatus): Color {
      return when (status) {
        PENDING -> PENDING_color
        ACCEPTED -> ACCEPTED_color
        SCHEDULED -> STARTED_color
        COMPLETED -> ENDED_color
        CANCELED -> CANCELLED_color
        ARCHIVED -> ARCHIVED_color
      }
    }
  }
}
