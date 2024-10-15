package com.android.solvit.seeker.model.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SeekerProfileViewModel(private val repository: UserRepositoryFirestore) : ViewModel() {

  private val _seekerProfile = MutableStateFlow<List<SeekerProfile>>(emptyList())

  val seekerProfile: StateFlow<List<SeekerProfile>> = _seekerProfile.asStateFlow()

  init {
    repository.init { getUserProfile() }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun getUserProfile() {
    repository.getUserProfile(
        onSuccess = { _seekerProfile.value = it },
        onFailure = { Log.e("get UserProfile", "failed to get UserProfile") })
  }

  fun addUserProfile(profile: SeekerProfile) {
    repository.addUserProfile(
        profile = profile,
        onSuccess = { getUserProfile() },
        onFailure = { Log.e("add User", "failed to add User") })
  }

  fun updateUserProfile(profile: SeekerProfile) {
    repository.updateUserProfile(
        profile = profile,
        onSuccess = { getUserProfile() },
        onFailure = { Log.e("update User", "failed to update User") })
  }

  fun deleteUserProfile(id: String) {
    repository.deleteUserProfile(
        id = id,
        onSuccess = { getUserProfile() },
        onFailure = { Log.e("delete User", "failed to delete User") })
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
