package com.android.solvit.shared.model.service

enum class Services {
  PLUMBER,
  ELECTRICIAN,
  TUTOR,
  EVENT_PLANNER,
  WRITER,
  CLEANER,
  CARPENTER,
  PHOTOGRAPHER,
  PERSONAL_TRAINER,
  HAIR_STYLIST,
  OTHER;

  companion object {
    fun format(service: Services): String {
      return service.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
  }
}
