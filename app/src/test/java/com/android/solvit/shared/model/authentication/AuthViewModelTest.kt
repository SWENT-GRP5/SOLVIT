package com.android.solvit.shared.model.authentication

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any

class AuthViewModelTest {
  private lateinit var authRepository: AuthRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var mockGoogleSignInAccount: GoogleSignInAccount

  @Before
  fun setUp() {
    authRepository = mock(AuthRepository::class.java)
    authViewModel = AuthViewModel(authRepository)
    mockGoogleSignInAccount = mock(GoogleSignInAccount::class.java)
  }

  @Test
  fun setRole() {
    authViewModel.setRole("role")
    assert(authViewModel.role.value == "role")
  }

  @Test
  fun setEmail() {
    authViewModel.setEmail("email")
    assert(authViewModel.email.value == "email")
  }

  @Test
  fun setPassword() {
    authViewModel.setPassword("password")
    assert(authViewModel.password.value == "password")
  }

  @Test
  fun loginWithEmailAndPassword() {
    authViewModel.loginWithEmailAndPassword()
    verify(authRepository).loginWithEmailAndPassword(any(), any(), any(), any())
  }

  @Test
  fun registerWithEmailAndPassword() {
    authViewModel.registerWithEmailAndPassword()
    verify(authRepository).registerWithEmailAndPassword(any(), any(), any(), any(), any())
  }

  @Test
  fun signInWithGoogle() {
    authViewModel.signInWithGoogle(mockGoogleSignInAccount)
    verify(authRepository).signInWithGoogle(any(), any(), any())
  }

  @Test
  fun registerWithGoogle() {
    authViewModel.registerWithGoogle(mockGoogleSignInAccount)
    verify(authRepository).registerWithGoogle(any(), any(), any(), any())
  }

  @Test
  fun logout() {
    authViewModel.logout()
    verify(authRepository).logout(any())
  }
}
