package com.android.solvit.shared.model.authentication

import com.android.solvit.shared.model.map.Location
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Interface defining authentication-related operations for managing user accounts, login,
 * registration, and profile updates. It supports multiple authentication methods including
 * email/password and Google sign-in.
 */
interface AuthRep {

  /**
   * Initializes the authentication process and checks if the user is already logged in.
   *
   * @param onSuccess Callback triggered with the currently logged-in user if found.
   */
  fun init(onSuccess: (user: User?) -> Unit)

  /**
   * Retrieves the unique identifier of the currently authenticated user.
   *
   * @return The user ID as a `String`.
   */
  fun getUserId(): String

  /**
   * Logs in a user using email and password credentials.
   *
   * @param email The user's email address.
   * @param password The user's password.
   * @param onSuccess Callback triggered upon successful login with the authenticated user.
   * @param onFailure Callback triggered with an exception if login fails.
   */
  fun loginWithEmailAndPassword(
      email: String,
      password: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Logs in a user using a Google account.
   *
   * @param account The `GoogleSignInAccount` object containing the user's Google account
   *   information.
   * @param onSuccess Callback triggered upon successful login with the authenticated user.
   * @param onFailure Callback triggered with an exception if login fails.
   */
  fun signInWithGoogle(
      account: GoogleSignInAccount,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Registers a new user using email and password credentials.
   *
   * @param role The user's role in the application (e.g., provider, client).
   * @param email The user's email address.
   * @param password The user's chosen password.
   * @param onSuccess Callback triggered upon successful registration with the new user details.
   * @param onFailure Callback triggered with an exception if registration fails.
   */
  fun registerWithEmailAndPassword(
      role: String,
      email: String,
      password: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Registers a new user using a Google account.
   *
   * @param account The `GoogleSignInAccount` object containing the user's Google account
   *   information.
   * @param role The user's role in the application.
   * @param onSuccess Callback triggered upon successful registration with the new user details.
   * @param onFailure Callback triggered with an exception if registration fails.
   */
  fun registerWithGoogle(
      account: GoogleSignInAccount,
      role: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Sets the user's display name.
   *
   * @param userName The new display name for the user.
   * @param userId The unique identifier of the user.
   * @param onSuccess Callback triggered when the name is successfully updated.
   * @param onFailure Callback triggered with an exception if the update fails.
   */
  fun setUserName(
      userName: String,
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Logs out the currently authenticated user.
   *
   * @param onSuccess Callback triggered upon successful logout.
   */
  fun logout(onSuccess: () -> Unit)

  /**
   * Updates the user's saved locations in the database.
   *
   * @param userId The unique identifier of the user.
   * @param locations A list of locations associated with the user.
   * @param onSuccess Callback triggered upon successful location update.
   * @param onFailure Callback triggered with an exception if the update fails.
   */
  fun updateUserLocations(
      userId: String,
      locations: List<Location>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Completes the user's registration process after filling in additional required information.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess Callback triggered upon successful completion of registration.
   * @param onFailure Callback triggered with an exception if registration completion fails.
   */
  fun completeRegistration(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
