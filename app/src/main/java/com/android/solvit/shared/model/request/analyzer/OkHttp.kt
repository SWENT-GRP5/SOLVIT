package com.android.solvit.shared.model.request.analyzer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import com.android.solvit.BuildConfig


suspend fun analyzeImagesWithOkHttp(imageUrls: List<String>): Triple<String, String, String> {
    val client = OkHttpClient()

    val systemMessage = """
        You are an AI that analyzes images and classifies them into one of the following 
        categories: PLUMBER, ELECTRICIAN, TUTOR, EVENT_PLANNER, WRITER, CLEANER, CARPENTER, 
        PHOTOGRAPHER, PERSONAL_TRAINER, HAIR_STYLIST, OTHER. 
        Then, describe the issue depicted in the images and provide a suitable title and type.
    """.trimIndent()

    // Generate a user message for each image URL
    val userMessage = imageUrls.joinToString("\n") { url ->
        "Analyze the image available at: $url"
    }

    val jsonPayload = JSONObject().apply {
        put("model", "gpt-4o-mini")
        put(
            "messages",
            JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", systemMessage)
                    }
                )
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    }
                )
            }
        )
        put("max_tokens", 500)
    }

    val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
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
                val choices = jsonResponse.optJSONArray("choices") ?: throw Exception("No choices in response")
                val message = choices.getJSONObject(0).optJSONObject("message") ?: throw Exception("No message in choices")
                val content = message.optString("content") ?: throw Exception("No content in message")

                // Extract details from the AI response
                val lines = content.lines()
                val type = lines.getOrNull(0)?.substringAfter("Type: ") ?: "OTHER"
                val title = lines.getOrNull(1)?.substringAfter("Title: ") ?: "Generated Title"
                val description = lines.getOrNull(2)?.substringAfter("Description: ") ?: "No description provided."

                Triple(type, title, description)
            } else {
                throw Exception("API call failed: ${response.code} - ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("analyzeImagesWithOkHttp", "Error during AI analysis: ${e.message}", e)
            throw Exception("Error during AI analysis: ${e.message}")
        }
    }
}
