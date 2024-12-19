package com.android.solvit.seeker.model.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.solvit.seeker.ui.service.SERVICES_LIST
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchServicesViewModel : ViewModel() {

  // MutableStateFlow to track whether a search operation is in progress.
  private val _isSearching = MutableStateFlow(false)
  val isSearching = _isSearching.asStateFlow()

  // MutableStateFlow to track the search text input by the user.
  private val _searchText = MutableStateFlow("")
  val searchText = _searchText.asStateFlow()

  // MutableStateFlow to hold the full list of services.
  private val _servicesList = MutableStateFlow(SERVICES_LIST)
  val servicesList =
      searchText
          .combine(_servicesList) { text, services ->
            if (text.isBlank()) {
              services
            }
            services.filter { service ->
              service.service.toString().contains(text, ignoreCase = true)
            }
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(),
              initialValue = _servicesList.value)

  /**
   * Updates the search text with the user's input.
   *
   * @param text The search text input by the user.
   */
  fun onSearchTextChange(text: String) {
    _searchText.value = text
  }

  /**
   * Toggles the search state (whether the search is active or not). If the search is being closed,
   * clears the search text.
   */
  fun onToggleSearch() {
    _isSearching.value = !_isSearching.value
    if (!_isSearching.value) {
      _searchText.value = ""
    }
  }
}
