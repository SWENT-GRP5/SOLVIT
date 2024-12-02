package com.android.solvit.shared.model.request.analyzer

import android.graphics.Bitmap
import com.android.solvit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Initialize the generative model
private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GOOGLE_AI_API_KEY
)


// Function to analyze images using the Gemini model
suspend fun analyzeImagesGemini(
    images: List<Bitmap>
): Triple<String, String, String> {
    return withContext(Dispatchers.IO) {
        try {
            // Create the input content using the ContentBuilder
            val inputContent = Content.Builder().apply {
                images.forEach { image(it) }
                text(
                    """
                    You are an AI that analyzes images and provides suggestions for service seekers.
                    Your task is to:
                    1. Classify each image into one of the following categories: 
                       PLUMBER, ELECTRICIAN, TUTOR, EVENT_PLANNER, WRITER, CLEANER, CARPENTER, PHOTOGRAPHER, 
                       PERSONAL_TRAINER, HAIR_STYLIST, OTHER.
                    2. Generate a **Title** for the issue, written in the voice of the service seeker.
                    3. Generate a **Description** for the issue, written as if the seeker is describing the problem to potential providers.
                    Please return the results in the following format:
                    - **Title**: Generated Title Here
                    - **Type**: Generated Category Here
                    - **Description**: Generated Description Here
                    """
                )
            }.build()

            // Call the model to generate content
            val response = generativeModel.generateContent(inputContent)

            // Parse the response text
            val content = response.text
            val title = content?.substringAfter("- **Title**:")?.substringBefore("\n")?.trim() ?: "Generated Title"
            val type = content?.substringAfter("- **Type**:")?.substringBefore("\n")?.trim()  ?: "Generated Type"
            val description = content?.substringAfter("- **Description**:")?.trim() ?: "Generated description"

            Triple(title, type, description)
        } catch (e: Exception) {
            throw Exception("Error analyzing images with Gemini: ${e.message}", e)
        }
    }
}
