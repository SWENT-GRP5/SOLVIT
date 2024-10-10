package com.github.se.bootcamp.model.map

import com.android.solvit.model.map.Location
import com.android.solvit.model.map.NominatimLocationRepository
import com.android.solvit.model.map.parseLocations
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify

class NominatimLocationRepositoryTest {
  private lateinit var repository: NominatimLocationRepository
  private lateinit var mockClient: OkHttpClient
  private lateinit var mockCall: Call
  private lateinit var mockResponse: Response
  private lateinit var responseBody: ResponseBody

  @Before
  fun setUp() {
    mockClient = mock(OkHttpClient::class.java)
    mockCall = mock(Call::class.java)
    mockResponse = mock(Response::class.java)
    responseBody = mock(ResponseBody::class.java)
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockResponse.body).thenReturn(responseBody)
    repository = NominatimLocationRepository(mockClient)
  }

  @Test
  fun testSearchCallback() {
    val query = "EPFL"
    val responseBodyContent =
        """
            [
                {
                    "lat": 46.519653,
                    "lon": 6.6322734,
                    "display_name": "EPFL, Route Cantonale, Ecublens, District de l'Ouest lausannois, Vaud, 1015, Switzerland"
                }
            ]
        """
            .trimIndent()

    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body?.string()).thenReturn(responseBodyContent)

    doAnswer { invocation ->
          val callback = invocation.getArgument<Callback>(0)
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    val onSuccess = mock<(List<Location>) -> Unit>()
    repository.search(query, onSuccess, onFailure = { assert(false) })

    verify(onSuccess).invoke(parseLocations(responseBodyContent))
  }

  @Test
  fun testNetworkFailure() {
    val query = "EPFL"

    // Simulate a network failure
    doAnswer { invocation ->
          val callback = invocation.getArgument<Callback>(0)
          callback.onFailure(mockCall, IOException("Network failure"))
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    val onFailure = mock<(Exception) -> Unit>()
    repository.search(query, onSuccess = { assert(false) }, onFailure = onFailure)

    // Verify that onFailure is called
    verify(onFailure).invoke(any())
  }

  @Test
  fun testInvalidJsonResponse() {
    val query = "EPFL"
    val invalidResponseBody = "{ invalid json ]"

    // Mocking invalid JSON response
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body?.string()).thenReturn(invalidResponseBody)

    doAnswer { invocation ->
          val callback = invocation.getArgument<Callback>(0)
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    val onFailure = mock<(Exception) -> Unit>()
    repository.search(query, onSuccess = { assert(false) }, onFailure = onFailure)

    // Verify that onFailure is called due to invalid JSON
    verify(onFailure).invoke(any())
  }

  @Test
  fun testUnsuccessfulHttpResponse() {
    val query = "EPFL"

    // Simulate an unsuccessful HTTP response (e.g., 404)
    `when`(mockResponse.isSuccessful).thenReturn(false)

    doAnswer { invocation ->
          val callback = invocation.getArgument<Callback>(0)
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    val onFailure = mock<(Exception) -> Unit>()
    repository.search(query, onSuccess = { assert(false) }, onFailure = onFailure)

    // Verify that onFailure is called for an unsuccessful HTTP response
    verify(onFailure).invoke(any())
  }

  @Test
  fun testEmptyQuery() {
    val query = ""

    // Empty query should still invoke the search, mock a successful response with no data
    `when`(mockResponse.isSuccessful).thenReturn(true)
    `when`(mockResponse.body?.string()).thenReturn("[]")

    doAnswer { invocation ->
          val callback = invocation.getArgument<Callback>(0)
          callback.onResponse(mockCall, mockResponse)
          null
        }
        .`when`(mockCall)
        .enqueue(any())

    val onSuccess = mock<(List<Location>) -> Unit>()
    repository.search(query, onSuccess, onFailure = { assert(false) })

    // Verify that the onSuccess callback is invoked with an empty list
    verify(onSuccess).invoke(emptyList())
  }
}
