package com.android.solvit.shared.model.request.analyzer

import android.graphics.Bitmap
import com.android.solvit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// Define the response schema using Schema.obj
private val jsonSchema =
    Schema.obj(
        name = "ImageAnalysisResponse",
        description = "The structured response for images analysis",
        Schema.str(name = "title", description = "The generated title for the analysis"),
        Schema.str(name = "type", description = "The category of the analyzed images"),
        Schema.str(
            name = "description", description = "A detailed description of the analyzed images"))

// Initialize the generative model with a JSON response schema
private val generativeModel =
    GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GOOGLE_AI_API_KEY,
        generationConfig =
            generationConfig {
              responseMimeType = "application/json"
              responseSchema = jsonSchema
            })

// Function to analyze images using the Gemini model
suspend fun analyzeImagesGemini(images: List<Bitmap>): Triple<String, String, String> {
  return withContext(Dispatchers.IO) {
    try {
      // Create the input content using the ContentBuilder
      val inputContent =
          Content.Builder()
              .apply {
                images.forEach { image(it) }
                text(
                    """
                    You are an AI that analyzes images and provides suggestions for service seekers.
                    Your task is to:
                    1. Classify each image into one of the following categories: 
                       PLUMBER, ELECTRICIAN, TUTOR, EVENT_PLANNER, WRITER, CLEANER, CARPENTER, PHOTOGRAPHER, 
                       PERSONAL_TRAINER, HAIR_STYLIST, OTHER.
                    2. Generate a title for the issue, written in the voice of the service seeker.
                    3. Generate a detailed description for the issue in the images, written as if the seeker is describing the problem to potential providers.
                    Please return the results in the following format:
                    - title : Generated Title Here
                    - type : Generated Category Here
                    - description : Generated Description Here
                    """)
              }
              .build()

      // Call the model to generate content
      val response = generativeModel.generateContent(inputContent)

      // Parse the response text
      val jsonObject = JSONObject(response.text)
      val title = jsonObject.getString("title")
      val type = jsonObject.getString("type")
      val description = jsonObject.getString("description")

      Triple(title, type, description)
    } catch (e: Exception) {
      throw Exception("Error analyzing images with Gemini: ${e.message}", e)
    }
  }
}
