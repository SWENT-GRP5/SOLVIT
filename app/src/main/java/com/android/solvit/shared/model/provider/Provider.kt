package com.android.solvit.shared.model.provider

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp

data class Provider(
    val uid: String = "",
    val name: String = "",
    val service: Services = Services.TUTOR,
    val imageUrl: String = "",
    val companyName: String = "",
    val phone: String = "",
    val location: Location = Location(0.0, 0.0, ""),
    val description: String = "",
    val popular: Boolean = false,
    val rating: Double = 0.0,
    val price: Double = 0.0,
    val deliveryTime: Timestamp = Timestamp.now(),
    val languages: List<Language> = emptyList(),
    val earnings: Double = 0.0,
    val pendingTasks: Int = 0,
    val tasksCompleted: Long = 0
)
