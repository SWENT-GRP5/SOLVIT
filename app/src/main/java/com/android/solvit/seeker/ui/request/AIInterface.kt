import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.android.solvit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Headers
import java.io.File
import java.io.FileOutputStream

interface AIAnalysisService {
    @Multipart
    @POST("chat/completions") //  API endpoint
    @Headers("Content-Type: application/json")
    fun analyzeImages(
        @Body requestBody: RequestBody // JSON payload with model, messages, etc.
    ): Call<AIAnalysisResponse>
}

// Response class for API
data class AIAnalysisResponse(
    val type: String,
    val title: String,
    val description: String
)

// A Retrofit Instance
fun createAIAnalysisService(): AIAnalysisService {

    val apiKey = BuildConfig.OPENAI_API_KEY

    // Add Authorization header to each request
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
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
    val promptText = """
        Analyze the following images:
        ${imageUrls.joinToString("\n") { "Image: $it" }}
        Classify the issue into one of the following categories: PLUMBER, ELECTRICIAN, TUTOR, EVENT_PLANNER, WRITER, CLEANER, CARPENTER, PHOTOGRAPHER, PERSONAL_TRAINER, HAIR_STYLIST, OTHER.
        Additionally, provide a descriptive title and detailed description for the issue.
    """.trimIndent()

    val jsonPayload = JSONObject().apply {
        put("model", "gpt-4o-mini")
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You are an AI assistant analyzing issues.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", promptText)
            })
        })
        put("max_tokens", 500)
    }

    return RequestBody.create("application/json".toMediaTypeOrNull(), jsonPayload.toString())
}


suspend fun analyzeImagesWithOpenAI(
    imageUrls: List<String>
): Triple<String, String, String> {
    val service = createAIAnalysisService()

    // Create the prompt as a RequestBody
    val requestBody = prepareRequestBody(imageUrls) // Prepare the JSON payload

    return withContext(Dispatchers.IO) {
        try {
            val response = service.analyzeImages(requestBody).execute() // API call

            if (response.isSuccessful) {
                val body = response.body()
                Triple(
                    body?.type ?: "OTHER",
                    body?.title ?: "Generated Title",
                    body?.description ?: "No description provided."
                )
            } else {
                throw Exception("API call failed: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            throw Exception("Error during AI analysis: ${e.message}")
        }
    }
}
