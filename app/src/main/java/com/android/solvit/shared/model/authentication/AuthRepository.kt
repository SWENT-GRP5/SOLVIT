package com.android.solvit.shared.model.authentication

import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Implementation of the `AuthRep` interface that manages user authentication, registration, and
 * profile updates using Firebase Authentication and Firestore. This repository handles operations
 * like login, registration, user data retrieval, and updates.
 *
 * @param auth Firebase Authentication instance for managing authentication tasks.
 * @param db Firestore database instance for user data storage and retrieval.
 */
class AuthRepository(private val auth: FirebaseAuth, private val db: FirebaseFirestore) : AuthRep {
  private val collectionPath = "users"

  /**
   * Initializes the repository by checking if a user is already authenticated. If authenticated,
   * retrieves the user's data from Firestore.
   *
   * @param onSuccess Callback triggered with the authenticated user or `null` if not found.
   */
  override fun init(onSuccess: (user: User?) -> Unit) {
    if (auth.currentUser != null) {
      val userId = auth.currentUser?.uid ?: return onSuccess(null)
      db.collection(collectionPath).document(userId).get().addOnSuccessListener { doc ->
        if (doc.exists()) {
          onSuccess(docToUser(doc))
        } else {
          Log.w("AuthRepository", "User not found in database")
          onSuccess(null)
        }
      }
    } else {
      onSuccess(null)
    }
  }

  /**
   * Retrieves the currently authenticated user's ID.
   *
   * @return The user's unique ID as a `String`, or an empty string if not authenticated.
   */
  override fun getUserId(): String {
    return auth.currentUser?.uid ?: ""
  }

  /**
   * Logs in the user using email and password credentials.
   *
   * @param email The email address of the user.
   * @param password The password of the user.
   * @param onSuccess Callback triggered upon successful login with the authenticated user.
   * @param onFailure Callback triggered with an exception if login fails.
   */
  override fun loginWithEmailAndPassword(
      email: String,
      password: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (email.isEmpty() || password.isEmpty()) {
      onFailure(Exception("Email and password cannot be empty"))
      return
    }
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
      if (it.isSuccessful) {
        Log.d("AuthRepository", "Login successful")
        it.result?.user?.uid?.let { userId -> fetchUserDocument(userId, onSuccess, onFailure) }
      } else {
        Log.w("AuthRepository", "Login failed", it.exception)
        onFailure(it.exception!!)
      }
    }
  }

  /**
   * Logs in the user using a Google account.
   *
   * @param account The user's `GoogleSignInAccount`.
   * @param onSuccess Callback triggered upon successful login with the authenticated user.
   * @param onFailure Callback triggered with an exception if login fails.
   */
  override fun signInWithGoogle(
      account: GoogleSignInAccount,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    auth.signInWithCredential(credential).addOnCompleteListener {
      if (it.isSuccessful) {
        Log.d("AuthRepository", "Google sign-in successful")
        it.result?.user?.uid?.let { userId -> fetchUserDocument(userId, onSuccess, onFailure) }
      } else {
        Log.w("AuthRepository", "Google sign-in failed", it.exception)
        onFailure(it.exception!!)
      }
    }
  }

  /**
   * Registers a new user with email and password credentials.
   *
   * @param role The role assigned to the user (e.g., provider, client).
   * @param email The user's email address.
   * @param password The user's password.
   * @param onSuccess Callback triggered upon successful registration with the new user details.
   * @param onFailure Callback triggered with an exception if registration fails.
   */
  override fun registerWithEmailAndPassword(
      role: String,
      email: String,
      password: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (email.isEmpty() || password.isEmpty()) {
      onFailure(Exception("Email and password cannot be empty"))
      return
    }
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
      if (it.isSuccessful) {
        Log.d("AuthRepository", "Registration successful")
        val user =
            User(
                uid = it.result!!.user!!.uid,
                role = role,
                email = email,
                registrationCompleted = false)
        createUserDocument(user, { onSuccess(user) }, onFailure)
      } else {
        Log.w("AuthRepository", "Registration failed", it.exception)
        onFailure(it.exception!!)
      }
    }
  }

  /**
   * Registers a new user using a Google account.
   *
   * @param account The `GoogleSignInAccount` representing the user.
   * @param role The role assigned to the user.
   * @param onSuccess Callback triggered upon successful registration with the new user details.
   * @param onFailure Callback triggered with an exception if registration fails.
   */
  override fun registerWithGoogle(
      account: GoogleSignInAccount,
      role: String,
      onSuccess: (user: User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    auth.signInWithCredential(credential).addOnCompleteListener {
      if (it.isSuccessful) {
        Log.d("AuthRepository", "Google sign-in successful")
        val user =
            User(
                it.result!!.user!!.uid,
                role,
                email = it.result!!.user!!.email!!,
                registrationCompleted = false)
        createUserDocument(user, { onSuccess(user) }, onFailure)
      } else {
        Log.w("AuthRepository", "Google sign-in failed", it.exception)
        onFailure(it.exception!!)
      }
    }
  }

  /**
   * Logs out the currently authenticated user.
   *
   * @param onSuccess Callback triggered upon successful logout.
   */
  override fun logout(onSuccess: () -> Unit) {
    auth.signOut()
    onSuccess()
  }

  /**
   * Updates the user's stored locations in the Firestore database.
   *
   * @param userId The unique identifier of the user.
   * @param locations A list of user locations to be updated.
   * @param onSuccess Callback triggered upon successful update.
   * @param onFailure Callback triggered with an exception if the update fails.
   */
  override fun updateUserLocations(
      userId: String,
      locations: List<Location>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .update(
            "locations",
            locations.map {
              mapOf("latitude" to it.latitude, "longitude" to it.longitude, "name" to it.name)
            })
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
          Log.w("AuthRepository", "Failed to update user locations", e)
          onFailure(e)
        }
  }

  /**
   * Completes the user's registration process by updating the `registrationCompleted` field in
   * Firestore.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess Callback triggered upon successful completion.
   * @param onFailure Callback triggered with an exception if the update fails.
   */
  override fun completeRegistration(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .update("registrationCompleted", true)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
          Log.w("AuthRepository", "Failed to complete registration", e)
          onFailure(e)
        }
  }

  private fun fetchUserDocument(
      userId: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .get()
        .addOnSuccessListener { doc ->
          if (doc.exists()) {
            val user = docToUser(doc)
            if (user != null) {
              onSuccess(user)
            } else {
              Log.w("AuthRepository", "Failed to convert user document")
              onFailure(Exception("Failed to convert user document"))
            }
          } else {
            Log.w("AuthRepository", "User not found in database")
            onFailure(Exception("User not found in database"))
          }
        }
        .addOnFailureListener { e ->
          Log.w("AuthRepository", "Failed to fetch user document", e)
          onFailure(e)
        }
  }

  /**
   * Sets the user's display name in Firestore.
   *
   * @param userName The new display name for the user.
   * @param userId The unique identifier of the user.
   * @param onSuccess Callback triggered upon successful update.
   * @param onFailure Callback triggered with an exception if the update fails.
   */
  override fun setUserName(
      userName: String,
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .update("userName", userName)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
          Log.w("AuthRepository", "Failed to update user name", e)
          onFailure(e)
        }
  }

  /**
   * Creates a new user document in Firestore after registration.
   *
   * @param user The `User` object containing the user's data.
   * @param onSuccess Callback triggered upon successful creation.
   * @param onFailure Callback triggered with an exception if creation fails.
   */
  private fun createUserDocument(
      user: User,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(user.uid)
        .set(user)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e ->
          Log.w("AuthRepository", "Failed to add user to database", e)
          onFailure(e)
        }
  }

  /**
   * Converts a Firestore document to a `User` object.
   *
   * @param doc The `DocumentSnapshot` containing the user's data.
   * @return The converted `User` object or `null` if conversion fails.
   */
  private fun docToUser(doc: DocumentSnapshot): User? {
    val uid = doc.getString("uid") ?: return null
    val role = doc.getString("role") ?: return null
    val username = doc.getString("userName") ?: ""
    val email = doc.getString("email") ?: return null
    val locations =
        (doc.get("locations") as? List<Map<String, Any>> ?: emptyList()).map {
          Location(
              latitude = it["latitude"] as? Double ?: 0.0,
              longitude = it["longitude"] as? Double ?: 0.0,
              name = it["name"] as? String ?: "Unknown")
        }
    val registrationCompleted = doc.getBoolean("registrationCompleted") ?: true
    return User(uid, role, username, email, locations, registrationCompleted)
  }
}
