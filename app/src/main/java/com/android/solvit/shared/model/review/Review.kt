package com.android.solvit.shared.model.review

data class Review(
    val uid: String,
    val authorId: String,
    val serviceRequestId: String,
    val providerId: String,
    val rating: Int,
    val comment: String
)
