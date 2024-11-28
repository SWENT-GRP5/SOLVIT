package com.android.solvit.shared.model.authentication

import com.android.solvit.shared.model.map.Location
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AuthRep {

  fun init(onSuccess: (user: User?) -> Unit)

  fun getUserId(): String

  fun loginWithEmailAndPassword(
      email: String,
      password: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun signInWithGoogle(
      account: GoogleSignInAccount,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun registerWithEmailAndPassword(
      role: String,
      email: String,
      password: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun registerWithGoogle(
      account: GoogleSignInAccount,
      role: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun logout(onSuccess: () -> Unit)

  fun updateUserLocations(
      locations: List<Location>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
