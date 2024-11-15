package com.android.solvit.shared.model.packages

data class PackageProposal(
    val uid: String,
    val title: String,
    val description: String,
    val price: Double,
    val bulletPoints: List<String>
)
