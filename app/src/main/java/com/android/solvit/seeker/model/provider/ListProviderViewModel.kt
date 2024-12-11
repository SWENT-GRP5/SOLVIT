package com.android.solvit.seeker.model.provider

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.service.Services
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListProviderViewModel(private val repository: ProviderRepository) : ViewModel() {
  private val _providersList = MutableStateFlow<List<Provider>>(emptyList())
  val providersList: StateFlow<List<Provider>> = _providersList

  private val _selectedService = MutableStateFlow<Services?>(null)
  val selectedService: StateFlow<Services?> = _selectedService

  private val _selectedProvider = MutableStateFlow<Provider?>(null)
  val selectedProvider: StateFlow<Provider?> = _selectedProvider

  private val _providersListFiltered = MutableStateFlow<List<Provider>>(emptyList())
  val providersListFiltered: StateFlow<List<Provider>> = _providersListFiltered

  private val filters = listOf("Price", "Languages", "Rating")
  private val activeFilters = mutableMapOf<String, (Provider) -> Boolean>()

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListProviderViewModel(
                repository =
                    ProviderRepositoryFirestore(
                        FirebaseFirestore.getInstance(), FirebaseStorage.getInstance()))
                as T
          }
        }
  }

  init {
    repository.init { getProviders() }
    repository.addListenerOnProviders(
        onSuccess = { _providersList.value = it },
        onFailure = { exception ->
          Log.e("ListProviderViewModel", "Error listening List of Providers", exception)
        })
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun addProvider(provider: Provider, imageUri: Uri?) {
    repository.addProvider(
        provider,
        imageUri,
        onSuccess = { getProviders() },
        onFailure = { Log.e("add Provider", "failed to add Provider") })
  }

  fun deleteProvider(
      uid: String,
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
          _providersList.value = providers
          _providersListFiltered.value = providers
        },
        onFailure = { Log.e("getProviders", "failed to get Providers") })
  }

  suspend fun fetchProviderById(uid: String): Provider? {
    return repository.returnProvider(uid)
  }

  fun filterProviders(filter: (Provider) -> Boolean, filterField: String) {

    activeFilters[filterField] = filter
    repository.filterProviders {
      _providersListFiltered.value =
          _providersList.value.filter { provider ->
            activeFilters.values.all { filterA -> filterA(provider) }
          }
    }
  }

  fun selectService(service: Services?) {
    _selectedService.value = service
  }

  fun selectProvider(provider: Provider?) {
    _selectedProvider.value = provider
  }

  fun applyFilters() {
    _providersList.value = _providersListFiltered.value
  }

  fun refreshFilters() {
    getProviders()
  }

  fun countProvidersByService(service: Services): Int {
      return _providersList.value.count { it.service == service }
  }
}
