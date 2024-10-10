package com.android.solvit.ui.screens.profile


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.repository.FirebaseRepository
import com.android.solvit.repository.FirebaseRepositoryImp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(private val repository: FirebaseRepositoryImp) : ViewModel() {

   // private val _userProfile = MutableStateFlow<List<UserProfile>>(emptyList())
    // hardcode
   private val _userProfile = MutableStateFlow(
       listOf(
           UserProfile(
               uid = "12345",    // Hardcoded UID
               name = "John Doe", // Hardcoded Name
               email = "john.doe@example.com", // Hardcoded Email
               phone = "+1234567890" // Hardcoded Phone Number
           )
       )
   )
    var userProfile: StateFlow<List<UserProfile>> = _userProfile.asStateFlow()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    fun getNewUid(): String {
        return repository.getNewUid()
    }


    fun getUserProfile() {
        repository.getUserProfile(onSuccess = { _userProfile.value = it }, onFailure = {})
    }


    fun loadUserProfile() {
        _isLoading.value = true
        repository.getUserProfile(
            onSuccess = {
                _userProfile.value = it
                _isLoading.value = false
            },
            onFailure = {
                _error.value = it.message
                _isLoading.value = false
            }
        )
    }




    /*fun updateUserProfile(profile: UserProfile) {
        repository.updateUserProfile(profile= profile , onSuccess = {  getUserProfile() }, onFailure = {})
    }

     */

    fun updateUserProfile(profile: UserProfile) {
    _userProfile.value = listOf(profile)
    }

    fun deleteUserProfile() {
        _isLoading.value = true
        repository.deleteUserProfile { isSuccess ->
            if (!isSuccess) {
                _error.value = "Failed to delete profile"
            }
            _isLoading.value = false
        }
    }


    // create factory
    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(FirebaseRepositoryImp(Firebase.firestore)) as T
                }
            }
    }

}
