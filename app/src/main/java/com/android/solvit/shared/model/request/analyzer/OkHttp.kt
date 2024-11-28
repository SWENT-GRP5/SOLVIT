package com.android.solvit.shared.model.request.analyzer

import android.util.Log
import com.android.solvit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

suspend fun analyzeImagesWithOkHttp(imageUrls: List<String>): Triple<String, String, String> {
  val client = OkHttpClient()

  val predefinedUrls =
      listOf(
          "https://www.houseopedia.com/wp-content/uploads/2023/01/How-to-Fix-Four-Common-Toilet-Problems-e1674502258761.jpeg")

  val systemMessage =
      """
    You are an AI that analyzes images and provides suggestions for service seekers.
    Your task is to:
    1. Classify each image into one of the following categories: 
       PLUMBER, ELECTRICIAN, TUTOR, EVENT_PLANNER, WRITER, CLEANER, CARPENTER, PHOTOGRAPHER, 
       PERSONAL_TRAINER, HAIR_STYLIST, OTHER. 
    2. Generate a **Title** for the issue, written in the voice of the service seeker.
    3. Generate a **Description** for the issue, written as if the seeker is describing the problem to potential providers hence analyze the photos joined and descripe the issue in the photos. 
       
       
    Please return the results in the following format:
    - **Title**: Generated Title Here
    - **Type**: Generated Category Here
    - **Description**: Generated Description Here
    """
          .trimIndent()

  // Generate a user message for each image URL
  val userMessage =
      predefinedUrls.joinToString("\n") { url -> "Analyze the image available at: $url" }

  val jsonPayload =
      JSONObject().apply {
        put("model", "gpt-4o-mini")
        put(
            "messages",
            JSONArray().apply {
              put(
                  JSONObject().apply {
                    put("role", "system")
                    put("content", systemMessage)
                  })
              put(
                  JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                  })
            })
        put("max_tokens", 500)
      }

  val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull())

  val request =
      Request.Builder()
          .url("https://api.openai.com/v1/chat/completions")
          .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
          .post(requestBody)
          .build()

  return withContext(Dispatchers.IO) {
    try {
      val response = client.newCall(request).execute()

      if (response.isSuccessful) {
        val responseBody = response.body?.string() ?: throw Exception("Empty response body")
        Log.d("analyzeImagesWithOkHttp", "Response: $responseBody")

        // Parse response JSON
        val jsonResponse = JSONObject(responseBody)
        val choices =
            jsonResponse.optJSONArray("choices") ?: throw Exception("No choices in response")
        val message =
            choices.getJSONObject(0).optJSONObject("message")
                ?: throw Exception("No message in choices")
        val content = message.optString("content") ?: throw Exception("No content in message")

        // Extract details from the AI response
        val title =
            content.substringAfter("- **Title**:").substringBefore("\n").trim() ?: "Generated Title"
        val type = content.substringAfter("- **Type**:").substringBefore("\n").trim() ?: "OTHER"
        val description =
            content.substringAfter("- **Description**:").trim() ?: "No description provided."

        Triple(title, type, description)
      } else {
        throw Exception("API call failed: ${response.code} - ${response.message}")
      }
    } catch (e: Exception) {
      Log.e("analyzeImagesWithOkHttp", "Error during AI analysis: ${e.message}", e)
      throw Exception("Error during AI analysis: ${e.message}")
    }
  }
}
