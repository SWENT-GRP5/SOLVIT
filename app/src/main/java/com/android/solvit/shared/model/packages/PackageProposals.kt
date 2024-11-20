package com.android.solvit.shared.model.packages

data class PackageProposal(
    val uid: String,
    val packageNumber: Double = 0.0,
    val providerId: String? = null,
    val title: String,
    val description: String,
    val price: Double? = null,
    val bulletPoints: List<String>
)
