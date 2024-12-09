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
import kotlin.random.Random
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

  private var activeFilters = mutableMapOf<String, (Provider) -> Boolean>()

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
        onSuccess = {
          _providersList.value = it
          _providersListFiltered.value = it
        },
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
    activeFilters.clear()
  }

  fun clearSelectedService() {
    _selectedService.value = null
  }

  /** Sort the list of providers given three Fields (Top Rates, Top Prices, Time) */
  fun sortProviders(field: String, isSelected: Boolean) {
    Log.e("sortProviders", "field : $field isSelected : $isSelected")
    Log.e("listSorted", "${_providersListFiltered.value}")
    when (field) {
      "Top Rates" ->
          _providersListFiltered.value =
              if (isSelected) _providersListFiltered.value.sortedByDescending { it.rating }
              else
                  _providersListFiltered.value.sortedByDescending {
                    it.rating + Random.nextDouble(1.0, 5.0)
                  } // We use here hash code to randomize if field is unselected
      "Top Prices" ->
          _providersListFiltered.value =
              if (isSelected) _providersListFiltered.value.sortedBy { it.price }
              else
                  _providersListFiltered.value.sortedBy {
                    it.price + Random.nextDouble(1.0, 1000.0)
                  }
      "Highest Activity" ->
          _providersListFiltered.value =
              if (isSelected) _providersListFiltered.value.sortedBy { it.nbrOfJobs }
              else _providersListFiltered.value.sortedBy { it.nbrOfJobs }
    }
  }
}
