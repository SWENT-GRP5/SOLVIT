package com.android.solvit.model.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.solvit.ui.services.SERVICES_LIST
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchServicesViewModel: ViewModel() {
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _servicesList = MutableStateFlow(SERVICES_LIST)
    val servicesList = searchText.combine(_servicesList) { text, services ->
        if (text.isBlank()) {
            services
        }
        services.filter { service ->
            service.service.toString().contains(text, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = _servicesList.value
    )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onToggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchText.value = ""
        }
    }
}