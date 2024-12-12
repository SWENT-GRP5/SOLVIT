package com.android.solvit.shared.model.chat

import kotlinx.serialization.Serializable

// Data class for AI response structure
@Serializable data class AiSolverResponse(val response: String, val shouldCreateRequest: Boolean)
