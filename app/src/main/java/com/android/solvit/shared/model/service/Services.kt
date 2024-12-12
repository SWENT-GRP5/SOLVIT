package com.android.solvit.shared.model.service

import androidx.compose.ui.graphics.Color
import com.android.solvit.R
import com.android.solvit.shared.ui.theme.Carpenter
import com.android.solvit.shared.ui.theme.Cleaner
import com.android.solvit.shared.ui.theme.Electrician
import com.android.solvit.shared.ui.theme.EventPlanner
import com.android.solvit.shared.ui.theme.HairStylist
import com.android.solvit.shared.ui.theme.PersonalTrainer
import com.android.solvit.shared.ui.theme.Photographer
import com.android.solvit.shared.ui.theme.Plumber
import com.android.solvit.shared.ui.theme.Tutor
import com.android.solvit.shared.ui.theme.Writer

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

    fun getIcon(service: Services): Int {
      return when (service) {
        PLUMBER -> R.drawable.ic_plumber
        ELECTRICIAN -> R.drawable.ic_electrician
        TUTOR -> R.drawable.ic_tutor
        EVENT_PLANNER -> R.drawable.ic_event_planner
        WRITER -> R.drawable.ic_writer
        CLEANER -> R.drawable.ic_cleaner
        CARPENTER -> R.drawable.ic_carpenter
        PHOTOGRAPHER -> R.drawable.ic_photographer
        PERSONAL_TRAINER -> R.drawable.ic_personal_trainer
        HAIR_STYLIST -> R.drawable.ic_hair_stylist
        OTHER -> R.drawable.ic_tutor
      }
    }

    fun getColor(service: Services): Color {
      return when (service) {
        PLUMBER -> Plumber
        ELECTRICIAN -> Electrician
        TUTOR -> Tutor
        EVENT_PLANNER -> EventPlanner
        WRITER -> Writer
        CLEANER -> Cleaner
        CARPENTER -> Carpenter
        PHOTOGRAPHER -> Photographer
        PERSONAL_TRAINER -> PersonalTrainer
        HAIR_STYLIST -> HairStylist
        OTHER -> Tutor
      }
    }

    fun getProfileImage(service: Services): Int {
      return when (service) {
        PLUMBER -> R.drawable.pr_plumber
        ELECTRICIAN -> R.drawable.pr_electrician
        TUTOR -> R.drawable.pr_tutor
        EVENT_PLANNER -> R.drawable.pr_event_planner
        WRITER -> R.drawable.pr_writer
        CLEANER -> R.drawable.pr_cleaner
        CARPENTER -> R.drawable.pr_carpenter
        PHOTOGRAPHER -> R.drawable.pr_photographer
        PERSONAL_TRAINER -> R.drawable.pr_personal_trainer
        HAIR_STYLIST -> R.drawable.pr_hair_stylist
        OTHER -> R.drawable.pr_tutor
      }
    }
  }
}
