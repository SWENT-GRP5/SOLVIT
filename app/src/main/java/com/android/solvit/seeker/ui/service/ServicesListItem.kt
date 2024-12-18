package com.android.solvit.seeker.ui.service

import androidx.compose.ui.graphics.Color
import com.android.solvit.R
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.theme.Carpenter
import com.android.solvit.shared.ui.theme.Cleaner
import com.android.solvit.shared.ui.theme.Electrician
import com.android.solvit.shared.ui.theme.EventPlanner
import com.android.solvit.shared.ui.theme.HairStylist
import com.android.solvit.shared.ui.theme.Other
import com.android.solvit.shared.ui.theme.PersonalTrainer
import com.android.solvit.shared.ui.theme.Photographer
import com.android.solvit.shared.ui.theme.Plumber
import com.android.solvit.shared.ui.theme.Tutor
import com.android.solvit.shared.ui.theme.Writer

data class ServicesListItem(val service: Services, val icon: Int, val color: Color)

val SERVICES_LIST =
    listOf(
        ServicesListItem(Services.PLUMBER, R.drawable.ic_plumber, Plumber),
        ServicesListItem(Services.ELECTRICIAN, R.drawable.ic_electrician, Electrician),
        ServicesListItem(Services.TUTOR, R.drawable.ic_tutor, Tutor),
        ServicesListItem(Services.EVENT_PLANNER, R.drawable.ic_event_planner, EventPlanner),
        ServicesListItem(Services.WRITER, R.drawable.ic_writer, Writer),
        ServicesListItem(Services.CLEANER, R.drawable.ic_cleaner, Cleaner),
        ServicesListItem(Services.CARPENTER, R.drawable.ic_carpenter, Carpenter),
        ServicesListItem(Services.PHOTOGRAPHER, R.drawable.ic_photographer, Photographer),
        ServicesListItem(
            Services.PERSONAL_TRAINER, R.drawable.ic_personal_trainer, PersonalTrainer),
        ServicesListItem(Services.HAIR_STYLIST, R.drawable.ic_hair_stylist, HairStylist),
        ServicesListItem(Services.OTHER, R.drawable.ic_other, Other))
