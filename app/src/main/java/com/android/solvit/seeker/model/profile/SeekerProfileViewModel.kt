package com.android.solvit.seeker.model.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SeekerProfileViewModel(private val repository: UserRepositoryFirestore) : ViewModel() {

  // private val _userProfile = MutableStateFlow<List<UserProfile>>(emptyList())
  // hardcode
  private val _seekerProfile =
      MutableStateFlow(
          listOf(
              SeekerProfile(
                  uid = "12345", // Hardcoded UID
                  name = "John Doe", // Hardcoded Name
                  username = "johndoe", // Hardcoded username
                  email = "john.doe@example.com", // Hardcoded Email
                  phone = "+1234567890", // Hardcoded Phone Number
                  address = "Chemin des Triaudes" // Hardcoded Address
                  )))
  val seekerProfile: StateFlow<List<SeekerProfile>> = _seekerProfile.asStateFlow()

  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean>
    get() = _isLoading

  private val _error = MutableLiveData<String>()
  val error: LiveData<String>
    get() = _error

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun getUserProfile() {
    repository.getUserProfile(onSuccess = { _seekerProfile.value = it }, onFailure = {})
  }

  /*fun updateUserProfile(profile: UserProfile) {
      repository.updateUserProfile(profile= profile , onSuccess = {  getUserProfile() }, onFailure = {})
  }

   */

  fun updateUserProfile(profile: SeekerProfile) {
    _seekerProfile.value = listOf(profile)
  }

  fun deleteUserProfile(id: String) {
    repository.deleteUserProfile(id = id, onSuccess = { getUserProfile() }, onFailure = {})
  }

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
}
