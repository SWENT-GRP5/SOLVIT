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

class ProviderViewModel(private val repository: ProviderRepository) : ViewModel() {
  private val _userProvider = MutableStateFlow<Provider?>(null)
  val userProvider: StateFlow<Provider?> = _userProvider

  // represent cost of route of provider to go to bookings
  var minCost = Double.MAX_VALUE

  // represent the best route given location of different bookings
  val bestRoute = mutableListOf<Int>()

  // create factory
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

  fun getProvider(uid: String) {
    repository.getProvider(
        uid,
        onSuccess = { _userProvider.value = it },
        onFailure = { Log.e("ProviderViewModel", it.toString()) })
  }

  fun updateProvider(provider: Provider) {
    repository.updateProvider(provider, onSuccess = { getProvider(provider.uid) }, onFailure = {})
  }

  fun distanceTo(startLoc: Location, destLoc: Location): Double {
    return haversineDistance(
        startLoc.latitude, startLoc.longitude, destLoc.latitude, destLoc.longitude)
  }

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
