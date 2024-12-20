package com.android.solvit.shared.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient

/**
 * ViewModel responsible for managing location search and suggestions. It maintains the search query
 * and a list of suggested locations.
 *
 * @property repository The repository that performs location search operations.
 */
class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

  // Holds the list of suggested locations based on the search query
  private val _locationSuggestions = MutableStateFlow<List<Location>>(emptyList())
  val locationSuggestions: StateFlow<List<Location>> = _locationSuggestions

  // Holds the current search query
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  /**
   * Sets the search query and fetches location suggestions from the repository.
   *
   * @param query The search query string.
   */
  fun setQuery(query: String) {
    _query.value = query
    repository.search(
        query,
        onSuccess = { locations -> _locationSuggestions.value = locations },
        onFailure = { exception -> println("Error fetching locations: $exception") })
  }

  /** Clears the current search query and resets location suggestions. */
  fun clear() {
    _query.value = ""
    _locationSuggestions.value = emptyList()
  }

  /**
   * Factory object for creating instances of `LocationViewModel`. Uses a default implementation of
   * `LocationRepository` for initialization.
   */
  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val repository = NominatimLocationRepository(OkHttpClient())
        LocationViewModel(repository)
      }
    }
  }
}
