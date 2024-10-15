package com.android.solvit.shared.model.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  private val _email = MutableStateFlow("")
  val email: StateFlow<String> = _email

  private val _password = MutableStateFlow("")
  val password: StateFlow<String> = _password

  private val _role = MutableStateFlow("")
  val role: StateFlow<String> = _role

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
    authRepository.init { _user.value = it }
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

  fun loginWithEmailAndPassword(onSuccess: () -> Unit, onFailure: () -> Unit) {
    authRepository.loginWithEmailAndPassword(
        email.value, password.value, { _user.value = it; onSuccess() }, { _user.value = null; onFailure() })
  }

  fun signInWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: () -> Unit) {
    authRepository.signInWithGoogle(account, { _user.value = it; onSuccess() }, { _user.value = null; onFailure() })
  }

  fun registerWithEmailAndPassword(onSuccess: () -> Unit, onFailure: () -> Unit) {
    authRepository.registerWithEmailAndPassword(
        role.value, email.value, password.value, { _user.value = it; onSuccess() }, { _user.value = null; onFailure() })
  }

  fun registerWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: () -> Unit) {
    authRepository.registerWithGoogle(
        account, role.value, { _user.value = it; onSuccess() }, { _user.value = null; onFailure() })
  }

  fun logout(onSuccess: () -> Unit) {
    authRepository.logout { _user.value = null; onSuccess() }
  }
}
