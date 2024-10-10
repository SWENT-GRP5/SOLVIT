package com.android.solvit.model.provider

import android.util.Log
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

  private val _providersListFiltered = MutableStateFlow<List<Provider>>(emptyList())
  val providersListFiltered: StateFlow<List<Provider>> = _providersListFiltered

  private val filters = listOf("Price","Languages","Rating")
  private val activeFilters = mutableMapOf<String,(Provider)->Boolean>()

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

  init {
    repository.init { getProviders() }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun addProvider(
      provider: Provider,
  ) {
    repository.addProvider(
        provider,
        onSuccess = { getProviders() },
        onFailure = { Log.e("add Provider", "failed to add Provider") })
  }

  fun deleteProvider(
      uid : String,
  ) {
    repository.deleteProvider(uid, onSuccess = { getProviders() }, onFailure = {})
  }

  fun updateProvider(
      provider: Provider,
  ) {
    repository.updateProvider(provider, onSuccess = { getProviders() }, onFailure = {})
  }

  fun getProviders() {
    repository.getProviders(
        _selectedService.value,
        onSuccess = { providers ->
          _uiState.value = providers
          _providersListFiltered.value = providers
        },
        onFailure = { Log.e("getProviders", "failed to get Providers") })
  }

  fun filterProviders(filter: (Provider) -> Boolean,filterField : String) {

      activeFilters[filterField] = filter
    repository.filterProviders {
        _providersListFiltered.value = _uiState.value.filter{
            provider -> activeFilters.values.all { filterA -> filterA(provider) }
        }

    }

  }

  fun selectService(service: Services?) {
        _selectedService.value = service
  }

  fun applyFilters() {
    _uiState.value = _providersListFiltered.value
  }
    fun refreshFilters(){
        getProviders()
    }
}
