package com.android.solvit.shared.model.provider

data class PackageProposal(
    val uid: String,
    val title: String,
    val description: String,
    val price: Double,
    val bulletPoints: List<String>
)
