package com.android.solvit.model.requests

import com.android.solvit.model.map.Location
import com.google.firebase.Timestamp

data class ServiceRequest(
    val uid: String,
    val title: String,
    val type: ServiceRequestType,
    val description: String,
    val assigneeName: String,
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

enum class ServiceRequestType {
  CLEANING,
  PLUMBING,
  ELECTRICIAN,
  CARPENTER,
  PAINTER,
  GARDENER,
  TUTOR,
  MOVING,
  LOCKSMITH,
  PEST_CONTROL,
  HVAC,
  ROOFING,
  LANDSCAPING,
  INTERIOR_DESIGN,
  WEB_DEVELOPMENT,
  GRAPHIC_DESIGN,
  PHOTOGRAPHY,
  PERSONAL_TRAINING,
  DOG_WALKING,
  CLEANING_SERVICES,
  VIRTUAL_ASSISTANCE,
  COOKING,
  CAR_REPAIR,
  OTHER
}
