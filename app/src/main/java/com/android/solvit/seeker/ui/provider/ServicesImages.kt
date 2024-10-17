package com.android.solvit.seeker.ui.provider

import com.android.solvit.R
import com.android.solvit.shared.model.service.Services

class ServicesImages {
  val serviceMap: Map<Services, Int> =
      mapOf(
          Services.PLUMBER to R.drawable.plumbier,
          Services.ELECTRICIAN to R.drawable.electrician_image,
          Services.WRITER to R.drawable.writer_image,
          Services.TUTOR to R.drawable.tutor_image,
          Services.CLEANER to R.drawable.cleaner_image,
          Services.CARPENTER to R.drawable.carpenter_image,
          Services.EVENT_PLANNER to R.drawable.eventplanner_image,
          Services.HAIR_STYLIST to R.drawable.hairstylist_image,
          Services.PERSONAL_TRAINER to R.drawable.trainer_image,
          Services.PHOTOGRAPHER to R.drawable.photographer_image)
}
