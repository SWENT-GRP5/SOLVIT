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

/**
 * Represents a service request created by a user or provider.
 *
 * @property uid The unique identifier of the service request.
 * @property title The title describing the service requested.
 * @property type The type of service requested, represented by the `Services` enum.
 * @property description A detailed description of the service request.
 * @property userId The unique identifier of the user creating the service request.
 * @property providerId The unique identifier of the provider assigned to the request (if any).
 * @property dueDate The deadline for completing the service request.
 * @property meetingDate The scheduled meeting date for the service (if applicable).
 * @property location The location where the service will be provided.
 * @property imageUrl An optional image URL representing the request (if uploaded).
 * @property packageId An optional package ID associated with the service request.
 * @property agreedPrice The agreed price for the service request (if applicable).
 * @property status The current status of the service request, represented by
 *   `ServiceRequestStatus`.
 */
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

/** Enum representing the different statuses a service request can have. */
enum class ServiceRequestStatus {
  PENDING,
  ACCEPTED,
  SCHEDULED,
  COMPLETED,
  CANCELED,
  ARCHIVED;

  companion object {
    /**
     * Formats the status to a more readable string with capitalization.
     *
     * @param status The current service request status.
     * @return The formatted string representation of the status.
     */
    fun format(status: ServiceRequestStatus): String {
      return status.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * Returns a color corresponding to the current status of the service request.
     *
     * @param status The current service request status.
     * @return The color associated with the status.
     */
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
