package com.android.solvit.shared.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {
  private val _locationSuggestions = MutableStateFlow<List<Location>>(emptyList())
  val locationSuggestions: StateFlow<List<Location>> = _locationSuggestions
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  fun setQuery(query: String) {
    _query.value = query
    repository.search(
        query,
        onSuccess = { locations -> _locationSuggestions.value = locations },
        onFailure = { exception -> println("Error fetching locations: $exception") })
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val repository = NominatimLocationRepository(OkHttpClient())
        LocationViewModel(repository)
      }
    }
  }
}
