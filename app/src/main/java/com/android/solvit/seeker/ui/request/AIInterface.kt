import android.util.Log
import com.android.solvit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AIAnalysisService {
  @POST("chat/completions") //  API endpoint
  @Headers("Content-Type: application/json")
  fun analyzeImages(
      @Body requestBody: RequestBody // JSON payload with model, messages, etc.
  ): Call<AIAnalysisResponse>
}

// Response class for API
data class AIAnalysisResponse(val type: String, val title: String, val description: String)

// A Retrofit Instance
fun createAIAnalysisService(): AIAnalysisService {

  val apiKey = BuildConfig.OPENAI_API_KEY

  // Add Authorization header to each request
  val client =
      OkHttpClient.Builder()
          .addInterceptor { chain ->
            val request =
                chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey") // API Key
                    .build()
            chain.proceed(request)
          }
          .build()

  return Retrofit.Builder()
      .baseUrl("https://api.openai.com/v1/") // Replace with your API's base URL
      .client(client) // Attach OkHttp client with API key
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(AIAnalysisService::class.java)
}

fun prepareRequestBody(imageUrls: List<String>): RequestBody {
  val systemMessage =
      """
        You are an AI that analyzes images and classifies them into one of the following 
        categories: PLUMBER, ELECTRICIAN, TUTOR, EVENT_PLANNER, WRITER, CLEANER, CARPENTER, 
        PHOTOGRAPHER, PERSONAL_TRAINER, HAIR_STYLIST, OTHER. 
        Then, describe the issue depicted in the images and provide a suitable title and type.
    """
          .trimIndent()

  val userMessage = imageUrls.joinToString("\n") { "Image URL: $it" }

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
                    put(
                        "content",
                        """
                    Analyze the following images:
                    $userMessage
                    """
                            .trimIndent())
                  })
            })
        put("max_tokens", 500)
        put("temperature", 0.7)
      }

  return RequestBody.create("application/json".toMediaTypeOrNull(), jsonPayload.toString())
}

suspend fun analyzeImagesWithOpenAI(imageUrls: List<String>): Triple<String, String, String> {
  val service = createAIAnalysisService()

  // Create the prompt as a RequestBody
  val requestBody = prepareRequestBody(imageUrls) // Prepare the JSON payload

  return withContext(Dispatchers.IO) {
    try {
      val response = service.analyzeImages(requestBody).execute() // API call

      if (response.isSuccessful) {
        val body = response.body()

        // Log the raw response
        Log.d("analyzeImagesWithOpenAI", "Response: ${response.raw()}")
        Log.d("analyzeImagesWithOpenAI", "Parsed Body: $body")

        Triple(
            body?.type ?: "OTHER",
            body?.title ?: "Generated Title",
            body?.description ?: "No description provided.")
      } else {
        throw Exception("API call failed: ${response.errorBody()?.string()}")
      }
    } catch (e: Exception) {
      throw Exception("Error during AI analysis: ${e.message}")
    }
  }
}
