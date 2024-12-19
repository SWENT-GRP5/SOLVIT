package com.android.solvit.seeker.model.provider

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.service.Services
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel to manage and filter the list of Providers, apply various filters, and handle CRUD
 * operations on Providers in the repository.
 */
class ListProviderViewModel(private val repository: ProviderRepository) : ViewModel() {

  // StateFlow variables for tracking the provider data and user selections.

  private val _providersList = MutableStateFlow<List<Provider>>(emptyList())
  val providersList: StateFlow<List<Provider>> = _providersList

  private val _selectedService = MutableStateFlow<Services?>(null)
  val selectedService: StateFlow<Services?> = _selectedService

  private val _selectedProvider = MutableStateFlow<Provider?>(null)
  val selectedProvider: StateFlow<Provider?> = _selectedProvider

  private val _providersListFiltered = MutableStateFlow<List<Provider>>(emptyList())
  val providersListFiltered: StateFlow<List<Provider>> = _providersListFiltered

  private val _selectedLanguages = MutableStateFlow<Set<Language>>(emptySet())
  val selectedLanguages: StateFlow<Set<Language>> = _selectedLanguages

  private val _selectedRatings = MutableStateFlow<Set<Double>>(emptySet())
  val selectedRatings: StateFlow<Set<Double>> = _selectedRatings

  private val _minPrice = MutableStateFlow("")
  val minPrice: StateFlow<String?> = _minPrice

  private val _maxPrice = MutableStateFlow("")
  val maxPrice: StateFlow<String?> = _maxPrice

  private var activeFilters = mutableMapOf<String, (Provider) -> Boolean>()

  companion object {
    // Factory for creating an instance of ListProviderViewModel.
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
    // Initialize the repository and listen for provider updates.
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

  /**
   * Filters providers based on a string field (like Language, Rating, Price) by applying the given
   * filter conditions.
   *
   * @param iconPressed A boolean indicating if the filter icon was pressed.
   * @param filterCondition The condition to check before applying the filter.
   * @param filterAction The filter logic to be applied if the condition is true.
   * @param defaultFilterAction The default filter logic to apply when no specific filter is set.
   * @param filterField The field being filtered (e.g., "Language", "Rating").
   */
  fun filterStringFields(
      iconPressed: Boolean,
      filterCondition: Boolean,
      filterAction: (Provider) -> Boolean,
      defaultFilterAction: (Provider) -> Boolean,
      filterField: String
  ) {

    if (iconPressed) {
      filterProviders({ provider -> filterAction(provider) }, filterField)
    } else {
      if (filterCondition) {
        filterProviders({ provider -> filterAction(provider) }, filterField)
      } else {
        filterProviders({ provider -> defaultFilterAction(provider) }, filterField)
      }
    }
  }

  /**
   * Updates the selected languages for filtering providers.
   *
   * @param newLanguages The new set of languages selected by the user.
   * @param languagePressed The language that was pressed by the user.
   */
  fun updateSelectedLanguages(newLanguages: Set<Language>, languagePressed: Language) {
    _selectedLanguages.value = newLanguages
    filterStringFields(
        iconPressed = languagePressed in selectedLanguages.value,
        filterAction = { provider ->
          selectedLanguages.value.intersect(provider.languages.toSet()).isNotEmpty()
        },
        filterCondition = selectedLanguages.value.isNotEmpty(),
        defaultFilterAction = { provider -> provider.languages.isNotEmpty() },
        filterField = "Language")
  }

  /** Clears all the selected filter fields (languages, ratings, price range). */
  fun clearFilterFields() {
    _selectedLanguages.value = emptySet()
    _selectedRatings.value = emptySet()
    _minPrice.value = ""
    _maxPrice.value = ""
  }

  /**
   * Updates the selected ratings for filtering providers.
   *
   * @param newRatings The new set of ratings selected by the user.
   * @param ratingPressed The rating that was pressed by the user.
   */
  fun updateSelectedRatings(newRatings: Set<Double>, ratingPressed: Double) {
    _selectedRatings.value = newRatings

    filterStringFields(
        iconPressed = ratingPressed in selectedRatings.value,
        filterAction = { provider -> selectedRatings.value.contains(provider.rating) },
        defaultFilterAction = { provider -> provider.rating >= 1.0 },
        filterCondition = selectedRatings.value.isNotEmpty(),
        filterField = "Rating")
  }

  /**
   * Updates the minimum price for filtering providers.
   *
   * @param price The minimum price value entered by the user.
   */
  fun updateMinPrice(price: String) {
    _minPrice.value = price
    val minPriceValue = _minPrice.value.toDoubleOrNull()
    val maxPriceValue = _maxPrice.value.toDoubleOrNull()
    if ((maxPriceValue != null && minPriceValue != null && minPriceValue <= maxPriceValue) ||
        (maxPriceValue == null && minPriceValue != null)) {
      filterProviders(filter = { provider -> provider.price >= minPriceValue }, "Price")
    } else {
      filterProviders(filter = { provider -> provider.price >= 0 }, "Price")
    }
  }

  /**
   * Updates the maximum price for filtering providers.
   *
   * @param price The maximum price value entered by the user.
   */
  fun updateMaxPrice(price: String) {
    _maxPrice.value = price
    val maxPriceValue = _maxPrice.value!!.toDoubleOrNull()
    val minPriceValue = _minPrice.value?.toDoubleOrNull()
    if ((maxPriceValue != null && minPriceValue != null && minPriceValue <= maxPriceValue) ||
        (maxPriceValue != null && minPriceValue == null)) {
      filterProviders(filter = { provider -> maxPriceValue >= provider.price }, "Price")
    } else {
      filterProviders(filter = { provider -> provider.price >= 0 }, "Price")
    }
  }

  /**
   * Generates a new unique identifier for a provider.
   *
   * @return A unique ID as a String.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /**
   * Adds a new provider to the repository.
   *
   * @param provider The provider object to be added.
   * @param imageUri The image URI for the provider (optional).
   */
  fun addProvider(provider: Provider, imageUri: Uri?) {
    repository.addProvider(
        provider,
        imageUri,
        onSuccess = { getProviders() },
        onFailure = { Log.e("add Provider", "failed to add Provider") })
  }

  /**
   * Deletes a provider from the repository.
   *
   * @param uid The unique identifier of the provider to be deleted.
   */
  fun deleteProvider(
      uid: String,
  ) {
    repository.deleteProvider(uid, onSuccess = { getProviders() }, onFailure = {})
  }

  /**
   * Updates an existing provider in the repository.
   *
   * @param provider The provider object with updated details.
   */
  fun updateProvider(
      provider: Provider,
  ) {
    repository.updateProvider(provider, onSuccess = { getProviders() }, onFailure = {})
  }

  /** Fetches the list of providers from the repository, applying any filters that may be active. */
  fun getProviders() {
    repository.getProviders(
        _selectedService.value,
        onSuccess = { providers ->
          _providersList.value = providers
          _providersListFiltered.value = providers
        },
        onFailure = { Log.e("getProviders", "failed to get Providers") })
  }

  /**
   * Fetches a provider by their unique identifier (UID).
   *
   * @param uid The UID of the provider to fetch.
   * @return The Provider object, or null if not found.
   */
  suspend fun fetchProviderById(uid: String): Provider? {
    return repository.returnProvider(uid)
  }

  /**
   * Filters the provider list based on a given filter function and filter field.
   *
   * @param filter The filtering logic to apply.
   * @param filterField The field used for the filter (e.g., "Language", "Price").
   */
  fun filterProviders(filter: (Provider) -> Boolean, filterField: String) {

    activeFilters[filterField] = filter
    repository.filterProviders {
      _providersListFiltered.value =
          _providersList.value.filter { provider ->
            activeFilters.values.all { filterA -> filterA(provider) }
          }
    }
  }

  /**
   * Selects a service from the list of available services and updates the selected service.
   *
   * @param service The service to select. Can be null if the selection is cleared.
   */
  fun selectService(service: Services?) {
    _selectedService.value = service
  }

  /**
   * Selects a provider from the list of available providers and updates the selected provider.
   *
   * @param provider The provider to select. Can be null if the selection is cleared.
   */
  fun selectProvider(provider: Provider?) {
    _selectedProvider.value = provider
  }

  fun applyFilters() {
    _providersList.value = _providersListFiltered.value
  }

  /**
   * Refreshes the filters by fetching the updated list of providers from the repository and
   * clearing any active filters.
   */
  fun refreshFilters() {
    getProviders()
    activeFilters.clear()
  }

  /** Clears the selected service, setting the selected service to null. */
  fun clearSelectedService() {
    _selectedService.value = null
  }

  /** Sort the list of providers given three Fields (Top Rates, Top Prices, Time) */
  fun sortProviders(field: String, isSelected: Boolean) {
    Log.e("sortProviders", "field : $field isSelected : $isSelected")
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
              if (isSelected) _providersListFiltered.value.sortedByDescending { it.nbrOfJobs }
              else _providersListFiltered.value.sortedBy { it.nbrOfJobs }
    }
  }

  /**
   * Counts the number of providers who offer a specific service.
   *
   * @param service The service to filter providers by.
   * @return The number of providers offering the given service.
   */
  fun countProvidersByService(service: Services): Int {
    return _providersList.value.count { it.service == service }
  }
}
