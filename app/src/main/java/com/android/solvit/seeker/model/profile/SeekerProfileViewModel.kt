package com.android.solvit.seeker.model.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.map.Location
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing the Seeker's profile. It provides data related to the Seeker's profile,
 * preferences, and locations, and interacts with the repository to perform CRUD operations.
 *
 * @property repository The repository used for fetching, adding, updating, and deleting Seeker
 *   data.
 */
class SeekerProfileViewModel(
    private val repository: UserRepository,
) : ViewModel() {

  // Seeker profile data that is currently being managed.
  private val _seekerProfile =
      MutableStateFlow(
          SeekerProfile(
              uid = "", // Hardcoded UID
              name = "", // Hardcoded Name
              username = "", // Hardcoded username
              email = "", // Hardcoded Email
              phone = "", // Hardcoded Phone Number
              address = Location(0.0, 0.0, "") // Hardcoded Address
              ))
  val seekerProfile: StateFlow<SeekerProfile> = _seekerProfile

  // List of Seeker profiles, which can be used to manage multiple profiles.
  private val _seekerProfileList = MutableStateFlow<List<SeekerProfile>>(emptyList())
  val seekerProfileList: StateFlow<List<SeekerProfile>> = _seekerProfileList

  // List of cached locations for the current Seeker.
  private val _cachedLocations = MutableStateFlow<List<Location>>(emptyList())
  val cachedLocations: StateFlow<List<Location>> = _cachedLocations

  // Location being searched for by the Seeker.
  private val _locationSearched = MutableStateFlow<Location?>(null)
  val locationSearched: StateFlow<Location?> = _locationSearched

  // List of preferences selected by the Seeker.
  private val _userPreferences = MutableStateFlow<List<String>>(emptyList())
  val userPreferences: StateFlow<List<String>> = _userPreferences

  /** Factory for creating instances of [SeekerProfileViewModel]. */
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return SeekerProfileViewModel(
                UserRepositoryFirestore(Firebase.firestore, Firebase.storage))
                as T
          }
        }
  }

  // LiveData for tracking loading state in UI.
  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean>
    get() = _isLoading

  // LiveData for error messages.
  private val _error = MutableLiveData<String>()
  val error: LiveData<String>
    get() = _error

  /**
   * Retrieves a new UID from the repository.
   *
   * @return A new UID as a [String].
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  init {
    // Initialize the repository and fetch the list of user profiles when the ViewModel is created.
    repository.init { getUsersProfile() }
  }

  /**
   * Fetches a Seeker profile by the given [uid] and updates the [seekerProfile] state.
   *
   * @param uid The unique identifier for the Seeker profile to retrieve.
   */
  fun getUserProfile(uid: String) {

    repository.getUserProfile(
        uid,
        onSuccess = { _seekerProfile.value = it },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to get user profile") })
  }

  /**
   * Suspends and fetches a Seeker profile by [uid].
   *
   * @param uid The unique identifier of the Seeker.
   * @return The Seeker profile, or null if not found.
   */
  suspend fun fetchUserById(uid: String): SeekerProfile? {
    return repository.returnSeekerById(uid)
  }

  /** Fetches all Seeker profiles from the repository and updates [seekerProfileList]. */
  fun getUsersProfile() {
    repository.getUsersProfile(onSuccess = { _seekerProfileList.value = it }, onFailure = {})
  }

  /**
   * Adds a new Seeker profile and an optional image to the repository.
   *
   * @param profile The Seeker profile to add.
   * @param imageUri The URI of the profile image (optional).
   */
  fun addUserProfile(profile: SeekerProfile, imageUri: Uri?) {
    repository.addUserProfile(profile, imageUri, onSuccess = { getUsersProfile() }, onFailure = {})
  }

  /**
   * Updates an existing Seeker profile in the repository.
   *
   * @param profile The updated Seeker profile.
   */
  fun updateUserProfile(profile: SeekerProfile) {
    repository.updateUserProfile(profile, onSuccess = { getUsersProfile() }, onFailure = {})
  }

  /**
   * Deletes a Seeker profile by [id].
   *
   * @param id The unique identifier of the Seeker profile to delete.
   */
  fun deleteUserProfile(id: String) {
    repository.deleteUserProfile(id = id, onSuccess = { getUsersProfile() }, onFailure = {})
  }

  /**
   * Adds a new user preference to the current Seeker profile and updates the state optimistically.
   *
   * @param userId The Seeker's unique identifier.
   * @param preference The preference to add.
   */
  fun addUserPreference(userId: String, preference: String) {
    val updatedPreferences = _userPreferences.value.toMutableList()
    if (!updatedPreferences.contains(preference)) {
      updatedPreferences.add(preference)
      _userPreferences.value = updatedPreferences // Update the state optimistically
    }
    // Perform the addition from Firestore
    repository.addUserPreference(
        userId = userId,
        preference = preference,
        onSuccess = { Log.d("SeekerProfileViewModel", "Preference added successfully") },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to add preference") })
  }

  /**
   * Deletes a user preference from the current Seeker profile and updates the state optimistically.
   *
   * @param userId The Seeker's unique identifier.
   * @param preference The preference to delete.
   */
  fun deleteUserPreference(userId: String, preference: String) {
    // Optimistically remove the preference from the local state
    val updatedPreferences = _userPreferences.value.toMutableList()
    updatedPreferences.remove(preference)
    _userPreferences.value = updatedPreferences
    // Perform the deletion from Firestore
    repository.deleteUserPreference(
        userId = userId,
        preference = preference,
        onSuccess = { Log.d("SeekerProfileViewModel", "Preference deleted successfully") },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to delete preference") })
  }

  /**
   * Fetches all user preferences for the given [userId].
   *
   * @param userId The Seeker's unique identifier.
   */
  fun getUserPreferences(userId: String) {
    repository.getUserPreferences(
        userId = userId,
        onSuccess = { _userPreferences.value = it },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to get preferences") })
  }

  /**
   * Updates the cached locations of a user with [newLocation].
   *
   * @param userId The Seeker's unique identifier.
   * @param newLocation The new location to update.
   */
  fun updateCachedLocations(userId: String, newLocation: Location) {
    repository.updateUserLocations(
        userId, newLocation, onSuccess = { _cachedLocations.value = it }, onFailure = {})
  }

  /**
   * Fetches cached locations for the given [userId].
   *
   * @param userId The Seeker's unique identifier.
   */
  fun getCachedLocations(userId: String) {
    repository.getCachedLocation(
        userId, onSuccess = { _cachedLocations.value = it }, onFailure = {})
  }

  /**
   * Sets the currently searched location.
   *
   * @param location The location being searched.
   */
  fun setLocationSearched(location: Location) {
    _locationSearched.value = location
  }

  /** Clears the currently searched location. */
  fun clearLocation() {
    _locationSearched.value = null
  }
}
