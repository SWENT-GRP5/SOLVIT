package com.android.solvit.provider.model.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.haversineDistance
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for managing provider-related operations, including retrieving and updating
 * provider data, calculating booking routes, and optimizing travel paths. It uses a backtracking
 * algorithm to solve the Traveling Salesman Problem (TSP) for route optimization.
 *
 * @constructor Initializes the ViewModel with a given provider repository.
 * @property repository Repository for provider data operations, including fetching and updating
 *   provider details.
 */
class ProviderViewModel(private val repository: ProviderRepository) : ViewModel() {

  /** Holds the current provider's details as a state flow. */
  private val _userProvider = MutableStateFlow<Provider?>(null)

  /** Exposes the current provider's data as an immutable state flow. */
  val userProvider: StateFlow<Provider?> = _userProvider

  /**
   * Stores the minimum cost of traveling through all booking locations. Used during route
   * optimization.
   */
  var minCost = Double.MAX_VALUE

  /** Holds the best route order after solving the TSP problem. */
  val bestRoute = mutableListOf<Int>()

  /** Factory for creating instances of the ViewModel with default repository implementations. */
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return ProviderViewModel(
                ProviderRepositoryFirestore(Firebase.firestore, Firebase.storage))
                as T
          }
        }
  }

  /**
   * Retrieves provider information from the repository by its unique ID.
   *
   * @param uid The unique ID of the provider to fetch.
   */
  fun getProvider(uid: String) {
    repository.getProvider(
        uid,
        onSuccess = { _userProvider.value = it },
        onFailure = { Log.e("ProviderViewModel", it.toString()) })
  }

  /**
   * Updates the provider's data in the repository and fetches the updated provider details.
   *
   * @param provider The updated provider information to be saved.
   */
  fun updateProvider(provider: Provider) {
    repository.updateProvider(provider, onSuccess = { getProvider(provider.uid) }, onFailure = {})
  }

  /**
   * Calculates the Haversine distance between two geographical points.
   *
   * @param startLoc The starting location.
   * @param destLoc The destination location.
   * @return The calculated distance in kilometers.
   */
  fun distanceTo(startLoc: Location, destLoc: Location): Double {
    return haversineDistance(
        startLoc.latitude, startLoc.longitude, destLoc.latitude, destLoc.longitude)
  }

  /**
   * Constructs a distance matrix for bookings, including the provider's current location.
   *
   * @param bookings A list of booking locations.
   * @return A 2D array where distances[i][j] represents the distance from location i to j.
   */
  fun distanceMatrix(bookings: List<Location>): Array<DoubleArray> {
    val startPoint = _userProvider.value?.location

    val distances = Array(bookings.size + 1) { DoubleArray(bookings.size + 1) }
    for (i in bookings.indices) {
      distances[0][i + 1] = startPoint?.let { distanceTo(it, bookings[i]) }!!
      distances[i + 1][0] = distances[0][i + 1]
      for (j in bookings.indices) {
        distances[i + 1][j + 1] = if (i == j) 0.0 else distanceTo(bookings[i], bookings[j])
      }
    }
    return distances
  }

  /**
   * Solves the Traveling Salesman Problem (TSP) using backtracking to find the optimal route.
   *
   * @param distances A precomputed distance matrix.
   * @param currentPosition The current location in the route.
   * @param visited A Boolean array tracking visited locations.
   * @param currentCost The accumulated cost of the current route.
   * @param route The current route being evaluated.
   */
  fun tspBackTracking(
      distances: Array<DoubleArray>,
      currentPosition: Int,
      visited: BooleanArray,
      currentCost: Double,
      route: MutableList<Int>
  ) {
    // Check if all locations have been visited
    if (route.size == visited.size) {
      val totalCost = currentCost + distances[currentPosition][0]
      if (totalCost < minCost) {
        minCost = totalCost
        bestRoute.clear()
        bestRoute.addAll(route)
        bestRoute.add(0)
      }
      return
    }

    // Recursively visit every unvisited location
    for (i in 1 until visited.size) {
      if (!visited[i]) {
        visited[i] = true
        route.add(i)
        tspBackTracking(distances, i, visited, currentCost + distances[currentPosition][i], route)
        visited[i] = false
        route.removeAt(route.lastIndex)
      }
    }
  }

  /**
   * Optimizes the booking route by computing the shortest path using TSP backtracking.
   *
   * @param location A list of booking locations to optimize.
   * @return The optimized list of locations based on the shortest route.
   */
  fun optimizeRouteBooking(location: List<Location>): List<Location> {
    val distances = distanceMatrix(location)
    val visited = BooleanArray(location.size + 1) { false }
    visited[0] = true
    tspBackTracking(distances, 0, visited, 0.0, mutableListOf(0))
    val newRoute: MutableList<Location> = mutableListOf()
    for (i in location.indices) {
      newRoute.add(i, location[bestRoute[i]])
    }
    return newRoute
  }
}
