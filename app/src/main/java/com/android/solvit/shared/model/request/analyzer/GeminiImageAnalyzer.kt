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

/**
 * Class responsible for analyzing images using the Gemini AI model.
 *
 * @property generativeModel The generative AI model used for content generation.
 */
class GeminiImageAnalyzer(private val generativeModel: GenerativeModel = defaultGenerativeModel) {
  companion object {
    /**
     * Defines the JSON schema expected in the image analysis response.
     *
     * Response Fields:
     * - title: The generated title for the issue.
     * - type: The generated category of the service request.
     * - description: A detailed explanation of the identified problem.
     */
    private val jsonSchema =
        Schema.obj(
            name = "ImageAnalysisResponse",
            description = "The structured response for images analysis",
            Schema.str(name = "title", description = "The generated title for the analysis"),
            Schema.str(name = "type", description = "The generated category for the analysis"),
            Schema.str(
                name = "description",
                description = "A detailed description of the analyzed images"))

    /**
     * Default generative model for performing AI-based image analysis.
     *
     * Configurations:
     * - Model name: gemini-1.5-flash
     * - API key: Uses Google AI API Key from build configuration.
     * - Response MIME type: application/json
     * - Response schema: Predefined using jsonSchema
     */
    val defaultGenerativeModel =
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GOOGLE_AI_API_KEY,
            generationConfig =
                generationConfig {
                  responseMimeType = "application/json"
                  responseSchema = jsonSchema
                })
  }

  /**
   * Analyzes a list of images using the Gemini AI model.
   *
   * The AI model:
   * - Classifies each image into a relevant category.
   * - Generates a title reflecting the nature of the problem.
   * - Creates a meaningful description as if the service seeker is describing the issue.
   *
   * @param images The list of bitmaps representing the uploaded images.
   * @return A triple containing the generated title, type (category), and description.
   * @throws Exception If any error occurs during the image analysis process.
   */
  suspend fun analyzeImages(images: List<Bitmap>): Triple<String, String, String> {
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
                    3. Generate a meaningful description for the issue in the images, written as if the seeker is describing the problem to potential providers.                  
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
}
