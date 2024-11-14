package com.android.solvit.shared.model.map

import okhttp3.OkHttpClient
import java.net.URLEncoder

class NominatimLocationRepository(private val client: OkHttpClient) : LocationRepository {
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
              override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                e.printStackTrace()
                onFailure(e)
              }

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
