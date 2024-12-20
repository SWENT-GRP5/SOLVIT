package com.android.solvit.shared.model.map

import java.net.URLEncoder
import okhttp3.OkHttpClient

/**
 * Implementation of `LocationRepository` that fetches location data using the Nominatim API. This
 * repository performs HTTP requests through OkHttp and parses JSON responses.
 *
 * @property client The OkHttp client used for making network requests.
 */
class NominatimLocationRepository(private val client: OkHttpClient) : LocationRepository {

  /**
   * Performs a location search using the Nominatim API based on the provided query.
   *
   * @param query The search query string used to filter locations.
   * @param onSuccess Callback invoked with a list of matching locations upon successful search.
   * @param onFailure Callback invoked with an exception if the search operation fails.
   */
  override fun search(
      query: String,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val request =
        okhttp3.Request.Builder()
            .addHeader("User-Agent", "SolvitAppEPFL")
            .url("https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery")
            .build()

    client
        .newCall(request)
        .enqueue(
            object : okhttp3.Callback {

              /**
               * Handles request failure due to network or server issues.
               *
               * @param call The failed OkHttp call instance.
               * @param e The exception that caused the failure.
               */
              override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                e.printStackTrace()
                onFailure(e)
              }

              /**
               * Processes the server response when the request succeeds.
               *
               * @param call The successful OkHttp call instance.
               * @param response The HTTP response from the server.
               */
              override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                  if (!response.isSuccessful) {
                    onFailure(java.io.IOException("Unexpected code $response"))
                    return
                  }

                  val responseBody = response.body?.string()
                  if (responseBody != null) {
                    try {
                      val locations = parseLocations(responseBody)
                      onSuccess(locations)
                    } catch (e: Exception) {
                      e.printStackTrace()
                      onFailure(e)
                    }
                  } else {
                    onFailure(java.io.IOException("Response body is null"))
                  }
                }
              }
            })
  }
}

/**
 * Parses the JSON response from the Nominatim API into a list of `Location` objects.
 *
 * @param responseBody The raw JSON string returned from the API.
 * @return A list of `Location` objects extracted from the JSON data.
 * @throws Exception If JSON parsing fails or data is malformed.
 */
fun parseLocations(responseBody: String): List<Location> {
  val jsonArray = org.json.JSONArray(responseBody)
  return (0 until jsonArray.length()).map { i ->
    jsonArray.getJSONObject(i).let { jsonObject ->
      Location(
          jsonObject.getDouble("lat"),
          jsonObject.getDouble("lon"),
          jsonObject.getString("display_name"))
    }
  }
}
