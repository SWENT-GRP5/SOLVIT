package com.android.solvit.shared.model.packages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.BuildConfig
import com.android.solvit.shared.model.service.Services
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PackagesAssistantViewModel : ViewModel() {
  private val _packageProposals = MutableStateFlow<List<PackageProposal>>(emptyList())
  val packageProposals: StateFlow<List<PackageProposal>> = _packageProposals

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  // Schema for the package proposals structured response
  private val schema =
      Schema(
          name = "PackageProposals",
          description = "A list of package proposals",
          type = FunctionType.ARRAY,
          items =
              Schema(
                  name = "PackageProposal",
                  type = FunctionType.OBJECT,
                  description = "A package proposal",
                  required =
                      listOf(
                          "uid",
                          "packageNumber",
                          "providerId",
                          "title",
                          "description",
                          "price",
                          "bulletPoints"),
                  properties =
                      mapOf(
                          "uid" to
                              Schema(
                                  name = "uid",
                                  type = FunctionType.STRING,
                                  description = "The unique identifier of the package proposal"),
                          "packageNumber" to
                              Schema(
                                  name = "packageNumber",
                                  type = FunctionType.NUMBER,
                                  description =
                                      "The index of the package number amongst all the packages of a provider"),
                          "providerId" to
                              Schema(
                                  name = "providerId",
                                  type = FunctionType.STRING,
                                  description =
                                      "The unique identifier associated with the provider creating the packages"),
                          "title" to
                              Schema(
                                  name = "title",
                                  type = FunctionType.STRING,
                                  description = "The title of the package proposal"),
                          "description" to
                              Schema(
                                  name = "description",
                                  type = FunctionType.STRING,
                                  description = "The description of the package proposal"),
                          "price" to
                              Schema(
                                  name = "price",
                                  type = FunctionType.NUMBER,
                                  description = "The price of the package proposal"),
                          "bulletPoints" to
                              Schema(
                                  name = "bulletPoints",
                                  type = FunctionType.ARRAY,
                                  description = "The bullet points of the package proposal",
                                  items =
                                      Schema(
                                          name = "bulletPoint",
                                          type = FunctionType.STRING,
                                          description = "A bullet point of the package proposal"),
                              ))))

  // Generative model for generating package proposals
  private val model =
      GenerativeModel(
          modelName = "gemini-1.5-flash",
          apiKey = BuildConfig.GOOGLE_AI_API_KEY,
          generationConfig =
              generationConfig {
                temperature = 0.15f
                topK = 32
                topP = 1f
                maxOutputTokens = 4096
                responseMimeType = "application/json"
                responseSchema = schema
              },
          safetySettings =
              listOf(
                  SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                  SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                  SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                  SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)))

  // Fetch package proposals for a given service type
  fun fetchPackageProposals(
      type: Services,
      numberOfPackages: Int,
      providerId: String,
      viewModel: PackageProposalViewModel,
      providerQuery: String
  ) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        val response = model.generateContent(generatePrompt(type, numberOfPackages, providerQuery))
        val jsonResponse = response.text // Assuming the response contains JSON text
        Log.d("PackagesAssistantViewModel", "jsonResponse: $jsonResponse")
        val updatedProposals =
            jsonResponse?.let { jsonToPackageProposals(it, viewModel, providerId) }
        _packageProposals.value = updatedProposals ?: emptyList()
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PackagesAssistantViewModel() as T
          }
        }
  }

  // Convert the JSON response to a list of package proposals
  private fun jsonToPackageProposals(
      response: String,
      viewModel: PackageProposalViewModel,
      providerId: String
  ): List<PackageProposal> {
    val json = Json { ignoreUnknownKeys = true }
    val proposals: List<PackageProposal> = json.decodeFromString<List<PackageProposal>>(response)
    return proposals.mapIndexed { index, proposal ->
      proposal.copy(
          uid = viewModel.getNewUid(), packageNumber = index.toDouble(), providerId = providerId)
    }
  }

  /**
   * Generate a prompt for the package proposals to ensure the model generates the correct content.
   * The prompt can be refined further based on the specific requirements of the service type.
   *
   * @param type The type of service for which the package proposals are being generated.
   * @param numberOfPackages The number of package proposals to generate.
   */
  private fun generatePrompt(type: Services, numberOfPackages: Int, providerQuery: String): String {
    return """
            Generate a list of $numberOfPackages package proposals for this type of service ($type)
             using the provided schema.
             The Provider making this request can provide additional information and requests here:
             $providerQuery
             If the provider has any specific requirements for the package proposals, please include
             them in the response.
             THE MOST IMPORTANT INFORMATION IS STILL THE NUMBER OF PACKAGES TO BE GENERATED AND THE SERVICE TYPE.
             YOU NEED TO RESPECT THEM AND RESPECT THE SCHEMA PROVIDED.
        """
        .trimIndent()
  }
}
