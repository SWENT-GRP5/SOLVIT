package com.android.solvit.ui.services

import com.android.solvit.R
import com.android.solvit.model.Services

data class ServicesListItem(val service: Services, val image: Int)

val SERVICES_LIST =
    listOf(
        ServicesListItem(Services.PLUMBER, R.drawable.plumber),
        ServicesListItem(Services.ELECTRICIAN, R.drawable.electrician),
        ServicesListItem(Services.TUTOR, R.drawable.tutor),
        ServicesListItem(Services.EVENT_PLANNER, R.drawable.event_planner),
        ServicesListItem(Services.WRITER, R.drawable.writer),
        ServicesListItem(Services.CLEANER, R.drawable.cleaner),
        ServicesListItem(Services.CARPENTER, R.drawable.carpenter),
        ServicesListItem(Services.PHOTOGRAPHER, R.drawable.photographer),
        ServicesListItem(Services.PERSONAL_TRAINER, R.drawable.personal_trainer),
        ServicesListItem(Services.HAIR_STYLIST, R.drawable.hair_stylist))
