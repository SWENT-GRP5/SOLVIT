package com.android.solvit.seeker.model.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.map.Location
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SeekerProfileViewModel(
    private val repository: UserRepository,
) : ViewModel() {

  // private val _userProfile = MutableStateFlow<List<UserProfile>>(emptyList())
  // hardcode
  private val _seekerProfile =
      MutableStateFlow<SeekerProfile>(
          SeekerProfile(
              uid = "", // Hardcoded UID
              name = "", // Hardcoded Name
              username = "", // Hardcoded username
              email = "", // Hardcoded Email
              phone = "", // Hardcoded Phone Number
              address = "" // Hardcoded Address
              ))
  val seekerProfile: StateFlow<SeekerProfile> = _seekerProfile

  private val _seekerProfileList = MutableStateFlow<List<SeekerProfile>>(emptyList())
  val seekerProfileList: StateFlow<List<SeekerProfile>> = _seekerProfileList

  private val _cachedLocations = MutableStateFlow<List<Location>>(emptyList())
  val cachedLocations: StateFlow<List<Location>> = _cachedLocations

  private val _locationSearched = MutableStateFlow<Location>(Location(0.0, 0.0, "Unknown"))
  val locationSearched: StateFlow<Location> = _locationSearched

  private val _userPreferences = MutableStateFlow<List<String>>(emptyList())
  val userPreferences: StateFlow<List<String>> = _userPreferences

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return SeekerProfileViewModel(UserRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean>
    get() = _isLoading

  private val _error = MutableLiveData<String>()
  val error: LiveData<String>
    get() = _error

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  init {
    repository.init { getUsersProfile() }
  }

  fun getUserProfile(uid: String) {

    repository.getUserProfile(
        uid,
        onSuccess = { _seekerProfile.value = it },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to get user profile") })
  }

  fun getUsersProfile() {
    repository.getUsersProfile(onSuccess = { _seekerProfileList.value = it }, onFailure = {})
  }

  fun addUserProfile(profile: SeekerProfile) {
    repository.addUserProfile(profile, onSuccess = { getUsersProfile() }, onFailure = {})
  }

  fun updateUserProfile(profile: SeekerProfile) {
    repository.updateUserProfile(profile, onSuccess = { getUsersProfile() }, onFailure = {})
  }

  fun deleteUserProfile(id: String) {
    repository.deleteUserProfile(id = id, onSuccess = { getUsersProfile() }, onFailure = {})
  }

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
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to add preference")
            })
  }

  fun deleteUserPreference(userId: String, preference: String) {

      // Optimistically remove the preference from the local state
      val updatedPreferences = _userPreferences.value.toMutableList()
      updatedPreferences.remove(preference)
      _userPreferences.value = updatedPreferences
      // Perform the deletion from Firestore
      repository.deleteUserPreference(
        userId = userId,
        preference = preference,
        onSuccess = {  Log.d("SeekerProfileViewModel", "Preference deleted successfully") },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to delete preference")
           })
  }

  fun getUserPreferences(userId: String) {
    repository.getUserPreferences(
        userId = userId,
        onSuccess = { _userPreferences.value = it },
        onFailure = { Log.e("SeekerProfileViewModel", "Failed to get preferences") })
  }

  fun updateCachedLocations(userId: String, newLocation: Location) {
    repository.updateUserLocations(
        userId, newLocation, onSuccess = { _cachedLocations.value = it }, onFailure = {})
  }

  fun getCachedLocations(userId: String) {
    repository.getCachedLocation(
        userId, onSuccess = { _cachedLocations.value = it }, onFailure = {})
  }

  fun setLocationSearched(location: Location) {
    _locationSearched.value = location
  }
}
