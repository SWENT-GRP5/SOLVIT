package com.android.solvit.shared.model.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.notifications.FcmTokenManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val authRepository: AuthRep) : ViewModel() {
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

  private val _userRegistered = MutableStateFlow<Boolean>(false)
  val userRegistered: StateFlow<Boolean> = _userRegistered

  private val maxLocationsSize = 5

  private val fcmTokenManager = FcmTokenManager.getInstance()
  private val scope = CoroutineScope(Dispatchers.IO)

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(AuthRepository(Firebase.auth, Firebase.firestore)) as T
          }
        }
  }

  init {
    viewModelScope.launch {
      try {
        val token = FirebaseMessaging.getInstance().token.await()
        println("FCM Token: $token") // Debug log
        Firebase.auth.currentUser?.uid?.let { userId ->
          fcmTokenManager.updateUserFcmToken(userId, token)
          println("FCM Token stored for user: $userId") // Debug log
        }
      } catch (e: Exception) {
        println("FCM Token Error: ${e.message}") // Debug log
        e.printStackTrace()
      }
    }
    authRepository.init {
      _user.value = it
      if (it != null) {
        _userRegistered.value = true
        updateFcmToken()
      }
    }
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
          updateFcmToken()
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
          updateFcmToken()
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

  private fun updateFcmToken() {
    viewModelScope.launch {
      try {
        val token = FirebaseMessaging.getInstance().token.await()
        Firebase.auth.currentUser?.uid?.let { userId ->
          fcmTokenManager.updateUserFcmToken(userId, token)
        }
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Error updating FCM token", e)
      }
    }
  }
}
