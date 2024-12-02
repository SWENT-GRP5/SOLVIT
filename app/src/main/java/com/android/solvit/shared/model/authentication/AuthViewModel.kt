package com.android.solvit.shared.model.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.notifications.FcmTokenManager
import com.android.solvit.shared.notifications.NotificationService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val authRepository: AuthRep,
    private val fcmTokenManager: FcmTokenManager? = null
) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  private val _email = MutableStateFlow("")
  val email: StateFlow<String> = _email

  private val _password = MutableStateFlow("")
  val password: StateFlow<String> = _password

  private val _role = MutableStateFlow("")
  val role: StateFlow<String> = _role

  private val _googleAccount = MutableStateFlow<GoogleSignInAccount?>(null)
  val googleAccount: StateFlow<GoogleSignInAccount?> = _googleAccount

  private val _userRegistered = MutableStateFlow(false)
  val userRegistered: StateFlow<Boolean> = _userRegistered

  private val maxLocationsSize = 5

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(
                AuthRepository(Firebase.auth, Firebase.firestore), FcmTokenManager.getInstance())
                as T
          }
        }
  }

  init {
    authRepository.init { user ->
      _user.value = user
      if (user != null) {
        _userRegistered.value = true
        fcmTokenManager?.let { updateFcmToken() }
      }
    }
  }

  private fun updateFcmToken() {
    fcmTokenManager?.let { manager ->
      viewModelScope.launch {
        try {
          val userId = authRepository.getUserId()
          if (userId.isNotEmpty()) {
            val token = FirebaseMessaging.getInstance().token.await()
            manager.updateUserFcmToken(userId, token)
          }
        } catch (e: Exception) {
          Log.e("AuthViewModel", "Error updating FCM token", e)
        }
      }
    }
  }

  private suspend fun onAuthenticationSuccess() {
    // Store FCM token after successful authentication
    fcmTokenManager?.let { NotificationService.storeCurrentToken() }
  }

  fun setRole(role: String) {
    _role.value = role
  }

  fun setEmail(email: String) {
    _email.value = email
  }

  fun setPassword(password: String) {
    _password.value = password
  }

  fun setGoogleAccount(account: GoogleSignInAccount) {
    _googleAccount.value = account
  }

  fun registered() {
    _userRegistered.value = true
  }

  fun loginWithEmailAndPassword(onSuccess: () -> Unit, onFailure: () -> Unit) {
    authRepository.loginWithEmailAndPassword(
        email.value,
        password.value,
        {
          _user.value = it
          viewModelScope.launch { onAuthenticationSuccess() }
          onSuccess()
        },
        {
          _user.value = null
          onFailure()
        })
  }

  fun signInWithGoogle(onSuccess: () -> Unit, onFailure: () -> Unit) {
    if (googleAccount.value == null) {
      onFailure()
    }
    authRepository.signInWithGoogle(
        googleAccount.value!!,
        {
          _user.value = it
          _email.value = it.email
          viewModelScope.launch { onAuthenticationSuccess() }
          onSuccess()
        },
        {
          _user.value = null
          onFailure()
        })
  }

  fun registerWithEmailAndPassword(onSuccess: () -> Unit, onFailure: () -> Unit) {
    authRepository.registerWithEmailAndPassword(
        role.value,
        email.value,
        password.value,
        {
          _user.value = it
          viewModelScope.launch { onAuthenticationSuccess() }
          onSuccess()
        },
        {
          _user.value = null
          onFailure()
        })
  }

  fun registerWithGoogle(onSuccess: () -> Unit, onFailure: () -> Unit) {
    if (googleAccount.value == null) {
      onFailure()
    }
    authRepository.registerWithGoogle(
        googleAccount.value!!,
        role.value,
        {
          _user.value = it
          _email.value = it.email
          viewModelScope.launch { onAuthenticationSuccess() }
          onSuccess()
        },
        {
          _user.value = null
          onFailure()
        })
  }

  fun logout(onSuccess: () -> Unit) {
    authRepository.logout {
      _userRegistered.value = false
      _user.value = null
      _role.value = ""
      _email.value = ""
      _password.value = ""
      _googleAccount.value = null
      onSuccess()
    }
  }

  fun addUserLocation(location: Location, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val userLocations = user.value?.locations ?: emptyList()
    if (userLocations.contains(location)) {
      Log.d("AuthViewModel", "User already has this location")
      onSuccess()
      return
    }
    val updatedLocations = (listOf(location) + userLocations).take(maxLocationsSize)
    authRepository.updateUserLocations(
        updatedLocations,
        {
          _user.value = user.value?.copy(locations = updatedLocations)
          Log.d("AuthViewModel", "User locations updated")
          onSuccess()
        },
        {
          Log.e("AuthViewModel", "Error updating user locations", it)
          onFailure(it)
        })
  }

  fun removeUserLocation(
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val userLocations = user.value?.locations ?: emptyList()
    if (!userLocations.contains(location)) {
      Log.d("AuthViewModel", "User does not have this location")
      onSuccess()
      return
    }
    val updatedLocations = userLocations.filter { it != location }
    authRepository.updateUserLocations(
        updatedLocations,
        {
          _user.value = user.value?.copy(locations = updatedLocations)
          Log.d("AuthViewModel", "User locations updated")
          onSuccess()
        },
        {
          Log.e("AuthViewModel", "Error updating user locations", it)
          onFailure(it)
        })
  }
}
