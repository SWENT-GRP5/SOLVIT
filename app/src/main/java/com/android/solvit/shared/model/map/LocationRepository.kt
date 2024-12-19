package com.android.solvit.shared.model.map

/**
 * Interface for handling location-based search operations. It provides a method for searching
 * locations based on a query string.
 */
interface LocationRepository {
  /**
   * Searches for locations that match the specified query string.
   *
   * @param query The search query string used to filter locations.
   * @param onSuccess Callback invoked with a list of matching locations upon a successful search.
   * @param onFailure Callback invoked with an exception if the search operation fails.
   */
  fun search(query: String, onSuccess: (List<Location>) -> Unit, onFailure: (Exception) -> Unit)
}
