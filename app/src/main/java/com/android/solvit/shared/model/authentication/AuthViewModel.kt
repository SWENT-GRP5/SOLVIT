package com.android.solvit.shared.model.authentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.WebSocket

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _email = MutableStateFlow<String>("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow<String>("")
    val password: StateFlow<String> = _password

    private val _role = MutableStateFlow<String>("")
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

    fun loginWithEmailAndPassword() {
        authRepository.loginWithEmailAndPassword(
            email.value,
            password.value,
            { _user.value = it },
            { _user.value = null }
        )
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        authRepository.signInWithGoogle(
            account,
            { _user.value = it },
            { _user.value = null }
        )
    }

    fun registerWithEmailAndPassword() {
        authRepository.registerWithEmailAndPassword(
            role.value,
            email.value,
            password.value,
            { _user.value = it },
            { _user.value = null }
        )
    }

    fun registerWithGoogle(account: GoogleSignInAccount) {
        authRepository.registerWithGoogle(
            account,
            role.value,
            { _user.value = it },
            { _user.value = null }
        )
    }

    fun logout() {
        authRepository.logout { _user.value = null }
    }
}