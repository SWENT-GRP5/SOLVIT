package com.android.solvit.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListProviderViewModel(private val repository: ProviderRepository) : ViewModel() {
  private val _uiState = MutableStateFlow<List<Provider>>(emptyList())
  val providersList: StateFlow<List<Provider>> = _uiState

  private val _selectedService = MutableStateFlow<Services?>(null)
  val selectedService: StateFlow<Services?> = _selectedService

  private val _selectedProvider = MutableStateFlow<Provider?>(null)
  val selectedProvider: StateFlow<Provider?> = _selectedProvider

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListProviderViewModel(
                repository = ProviderRepositoryFirestore(FirebaseFirestore.getInstance()))
                as T
          }
        }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun addProvider(
      provider: Provider,
  ) {
    repository.addProvider(provider, onSuccess = { getProviders() }, onFailure = {})
  }

  fun deleteProvider(
      provider: Provider,
  ) {
    repository.deleteProvider(provider, onSuccess = { getProviders() }, onFailure = {})
  }

  fun updateProvider(
      provider: Provider,
  ) {
    repository.updateProvider(provider, onSuccess = { getProviders() }, onFailure = {})
  }

  fun getProviders() {
    repository.getProviders(
        _selectedService.value,
        onSuccess = { providers -> _uiState.value = providers },
        onFailure = {})
  }

  fun filterProviders(filter: (Provider) -> Boolean) {
    _uiState.value = _uiState.value.filter(filter)
  }

  fun selectService(service: Services?) {
    _selectedService.value = service
  }
}
