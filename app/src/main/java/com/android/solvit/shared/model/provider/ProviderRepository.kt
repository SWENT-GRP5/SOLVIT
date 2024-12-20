package com.android.solvit.shared.model.provider

import android.net.Uri
import com.android.solvit.shared.model.service.Services

/** Interface defining repository operations related to service providers. */
interface ProviderRepository {

  /**
   * Initializes the provider repository.
   *
   * @param onSuccess Callback invoked when initialization succeeds.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Adds a listener for monitoring changes in the list of providers.
   *
   * @param onSuccess Callback invoked with the list of providers on success.
   * @param onFailure Callback invoked with an exception on failure.
   */
  fun addListenerOnProviders(onSuccess: (List<Provider>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Generates a new unique identifier for a provider.
   *
   * @return A new unique provider ID.
   */
  fun getNewUid(): String

  /**
   * Adds a new provider to the repository.
   *
   * @param provider The provider data to be added.
   * @param imageUri Optional URI of the provider's profile image.
   * @param onSuccess Callback invoked on successful addition.
   * @param onFailure Callback invoked with an exception on failure.
   */
  fun addProvider(
      provider: Provider,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a provider from the repository.
   *
   * @param uid Unique identifier of the provider to delete.
   * @param onSuccess Callback invoked on successful deletion.
   * @param onFailure Callback invoked with an exception on failure.
   */
  fun deleteProvider(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates an existing provider's details.
   *
   * @param provider The provider with updated data.
   * @param onSuccess Callback invoked on successful update.
   * @param onFailure Callback invoked with an exception on failure.
   */
  fun updateProvider(provider: Provider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a list of providers offering a specific service.
   *
   * @param service Optional filter by service type.
   * @param onSuccess Callback invoked with the list of providers on success.
   * @param onFailure Callback invoked with an exception on failure.
   */
  fun getProviders(
      service: Services?,
      onSuccess: (List<Provider>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Filters the list of providers based on specified criteria.
   *
   * @param filter A lambda defining the filtering logic.
   */
  fun filterProviders(filter: () -> Unit)

  /**
   * Retrieves a specific provider by user ID.
   *
   * @param userId Unique identifier of the provider's user account.
   * @param onSuccess Callback invoked with the provider's details on success.
   * @param onFailure Callback invoked with an exception on failure.
   */
  fun getProvider(userId: String, onSuccess: (Provider?) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a provider asynchronously by unique identifier.
   *
   * @param uid The unique identifier of the provider.
   * @return The provider's details, or `null` if not found.
   */
  suspend fun returnProvider(uid: String): Provider?
}
