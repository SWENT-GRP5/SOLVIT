package com.android.solvit.seeker.model.profile

import android.net.Uri
import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.utils.uploadImageToStorage
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * UserRepositoryFirestore handles all Firebase Firestore and Firebase Storage operations for
 * managing SeekerProfile data. It implements the UserRepository interface, providing functionality
 * for adding, retrieving, updating, and deleting user profiles, as well as managing user
 * preferences and locations.
 *
 * @param db The FirebaseFirestore instance for database operations.
 * @param storage The FirebaseStorage instance for image uploads.
 */
open class UserRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

  private val collectionPath = "user" // Path for the user collection in Firestore.
  private val usersImagesPath =
      "usersImages/" // Path for storing user profile images in Firebase Storage.

  /**
   * Initializes the repository. It currently triggers the [onSuccess] callback.
   *
   * @param onSuccess Callback invoked when initialization is complete.
   */
  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  /**
   * Generates a new unique ID (UID) for a new user profile.
   *
   * @return A new Firestore document ID.
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Adds a SeekerProfile to Firestore. If [imageUri] is provided, it uploads the image to Firebase
   * Storage before saving the profile with the image URL. Otherwise, it saves the profile without
   * an image.
   *
   * @param profile The SeekerProfile to add.
   * @param imageUri The Uri of the profile image to upload (optional).
   * @param onSuccess Callback invoked when the profile is successfully added.
   * @param onFailure Callback invoked with the exception when adding the profile fails.
   */
  override fun addUserProfile(
      profile: SeekerProfile,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (imageUri != null) {
      uploadImageToStorage(
          storage,
          usersImagesPath,
          imageUri,
          onSuccess = { imageUrl ->
            performFirestoreOperation(
                db.collection(collectionPath)
                    .document(profile.uid)
                    .set(profile.copy(imageUrl = imageUrl)),
                onSuccess,
                onFailure)
          },
          onFailure = { Log.e("Add Seeker", "Failed to add seeker $it") })
    } else {
      performFirestoreOperation(
          db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
    }
  }

  /**
   * Fetches the user profile from Firestore based on the given [uid].
   *
   * @param uid The user ID of the profile to fetch.
   * @param onSuccess Callback invoked with the SeekerProfile when the profile is successfully
   *   retrieved.
   * @param onFailure Callback invoked with the exception when fetching the profile fails.
   */
  override fun getUserProfile(
      uid: String,
      onSuccess: (SeekerProfile) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUsersProfile")
    db.collection(collectionPath).document(uid).get().addOnCompleteListener { document ->
      if (document.isSuccessful) {

        Log.e("Repo", "${document.result}")
        val user = documentToUser(document.result)
        if (user != null) {
          onSuccess(user)
        } else {
          Log.e("RepositoryFirestore", "Error getting user")
        }
      } else {
        document.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Suspended function to fetch the user profile using the user ID [uid]. Uses coroutines to make
   * the function asynchronous.
   *
   * @param uid The user ID of the profile to fetch.
   * @return The SeekerProfile if found, or null otherwise.
   */
  override suspend fun returnSeekerById(uid: String): SeekerProfile? {
    return try {
      val document = db.collection(collectionPath).document(uid).get().await()
      Log.e("UserRepositoryFirestore", "$document")
      val user = documentToUser(document)
      if (user != null) {
        Log.d("RepositoryFirestore", "User fetched successfully: $user")
        user
      } else {
        Log.e("RepositoryFirestore", "User not found for uid: $uid")
        null
      }
    } catch (e: Exception) {
      Log.e("RepositoryFirestore", "Error fetching user by Id : $e")
      null
    }
  }

  /**
   * Fetches all user profiles from Firestore and returns them as a list of SeekerProfile.
   *
   * @param onSuccess Callback invoked with the list of SeekerProfiles.
   * @param onFailure Callback invoked with the exception if the operation fails.
   */
  override fun getUsersProfile(
      onSuccess: (List<SeekerProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUsersProfile")
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val user = task.result?.mapNotNull { document -> documentToUser(document) } ?: emptyList()
        onSuccess(user)
      } else {
        task.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Updates an existing SeekerProfile in Firestore.
   *
   * @param profile The SeekerProfile to update.
   * @param onSuccess Callback invoked when the update is successful.
   * @param onFailure Callback invoked with the exception when the update fails.
   */
  override fun updateUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  /**
   * Deletes a SeekerProfile from Firestore using the user ID [id].
   *
   * @param id The user ID of the profile to delete.
   * @param onSuccess Callback invoked when the profile is successfully deleted.
   * @param onFailure Callback invoked with the exception when the delete operation fails.
   */
  override fun deleteUserProfile(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  fun getLocations(doc: DocumentSnapshot): MutableList<Location> {
    val locations =
        (doc.get("cachedLocations") as? List<*>)?.mapNotNull { locationMap ->
          (locationMap as? Map<String, Any>)?.let {
            Location(
                latitude = it["latitude"] as? Double ?: 0.0,
                longitude = it["longitude"] as? Double ?: 0.0,
                name = it["name"] as? String ?: "Unknown")
          }
        } ?: emptyList()
    return locations.toMutableList()
  }

  /**
   * Updates the cached locations for the user with ID [userId]. It adds [newLocation] to the
   * existing locations and limits the list to a maximum of 2 locations.
   *
   * @param userId The ID of the user whose locations are being updated.
   * @param newLocation The new location to add.
   * @param onSuccess Callback invoked with the updated list of locations.
   * @param onFailure Callback invoked with the exception when the update fails.
   */
  override fun updateUserLocations(
      userId: String,
      newLocation: Location,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        var locations = getLocations(doc)
        locations.add(0, newLocation)
        if (locations.size > 2) {
          locations = locations.take(2).toMutableList()
        }
        db.collection(collectionPath).document(userId).update("cachedLocations", locations.toList())
        onSuccess(locations.toList())
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }

  /**
   * Fetches the cached locations for a user with ID [userId].
   *
   * @param userId The ID of the user whose locations are being retrieved.
   * @param onSuccess Callback invoked with the list of locations.
   * @param onFailure Callback invoked with the exception when the operation fails.
   */
  override fun getCachedLocation(
      userId: String,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        val locations = getLocations(doc)
        onSuccess(locations)
      } else {

        task.exception?.let { onFailure(it) }
      }
    }
  }

  /**
   * Adds a new preference to the user's preference list in Firestore. It first retrieves the
   * current list of preferences, adds the new preference if it's not already present, and then
   * updates the user's document with the updated preferences list.
   *
   * @param userId The user ID to add the preference to.
   * @param preference The new preference to add.
   * @param onSuccess Callback invoked when the operation is successful.
   * @param onFailure Callback invoked with the exception when the operation fails.
   */
  override fun addUserPreference(
      userId: String,
      preference: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        // Check if the document exists, else create a new one
        val currentPreferences = doc?.get("preferences") as? MutableList<String> ?: mutableListOf()

        // Add the new preference to the list
        if (!currentPreferences.contains(preference)) {
          currentPreferences.add(preference)
        }

        // Update or create the document with the updated preferences list
        db.collection(collectionPath)
            .document(userId)
            .set(mapOf("preferences" to currentPreferences))
            .addOnCompleteListener {
              if (it.isSuccessful) onSuccess() else it.exception?.let(onFailure)
            }
      } else {
        task.exception?.let(onFailure)
      }
    }
  }

  /**
   * Deletes a specific preference from the user's preferences list in Firestore. It retrieves the
   * user's current preferences, removes the specified preference, and updates the document.
   *
   * @param userId The user ID whose preference should be removed.
   * @param preference The preference to remove.
   * @param onSuccess Callback invoked when the operation is successful.
   * @param onFailure Callback invoked with the exception when the operation fails.
   */
  override fun deleteUserPreference(
      userId: String,
      preference: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        val currentPreferences = doc?.get("preferences") as? MutableList<String> ?: mutableListOf()

        // Remove the preference from the list
        currentPreferences.remove(preference)

        // Update the document with the new preferences list
        db.collection(collectionPath)
            .document(userId)
            .update("preferences", currentPreferences)
            .addOnCompleteListener {
              if (it.isSuccessful) onSuccess() else it.exception?.let(onFailure)
            }
      } else {
        task.exception?.let(onFailure)
      }
    }
  }

  /**
   * Retrieves the list of preferences for a specific user from Firestore.
   *
   * @param userId The user ID whose preferences are to be retrieved.
   * @param onSuccess Callback invoked with the list of preferences when the operation is
   *   successful.
   * @param onFailure Callback invoked with the exception when the operation fails.
   */
  override fun getUserPreferences(
      userId: String,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        val preferences = doc?.get("preferences") as? List<String> ?: emptyList()
        onSuccess(preferences)
      } else {
        task.exception?.let(onFailure)
      }
    }
  }

  /**
   * Executes a Firestore operation (e.g., add, update, delete) and handles the result with success
   * and failure callbacks.
   *
   * @param task The Firestore task to be executed (e.g., document creation or update).
   * @param onSuccess Callback invoked when the operation is successful.
   * @param onFailure Callback invoked with the exception when the operation fails.
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Converts a Firestore document into a SeekerProfile object. This method extracts all relevant
   * fields such as user ID, name, username, image URL, email, phone, address (including latitude
   * and longitude), and preferences to create a SeekerProfile.
   *
   * @param document The Firestore DocumentSnapshot to convert.
   * @return A SeekerProfile object, or null if the document is missing required fields.
   */
  fun documentToUser(document: DocumentSnapshot): SeekerProfile? {
    return try {
      Log.e("DocumentToUser", "")
      val uid = document.id
      val name = document.getString("name") ?: return null
      val username = document.getString("username") ?: return null
      val imageUrl = document.getString("imageUrl") ?: ""
      val email = document.getString("email") ?: return null
      val phone = document.getString("phone") ?: return null
      val address = document.get("address") as? Map<*, *>
      val latitude = address?.get("latitude") as? Double ?: return null
      val longitude = address["longitude"] as? Double ?: return null
      val nameLoc = address["name"] as? String ?: return null
      val preferences = document.get("preferences") as? List<String> ?: emptyList()
      Log.e(
          "DocumentToUser",
          "${
                    SeekerProfile(
                        uid = uid,
                        name = name,
                        username = username,
                        imageUrl = imageUrl,
                        email = email,
                        phone = phone,
                        address = Location(latitude, longitude, nameLoc),
                        preferences = preferences
                    )
                }")
      SeekerProfile(
          uid = uid,
          name = name,
          username = username,
          imageUrl = imageUrl,
          email = email,
          phone = phone,
          address = Location(latitude, longitude, nameLoc),
          preferences = preferences)
    } catch (e: Exception) {
      Log.e("RepositoryFirestore", "Error converting document to UserProfile", e)
      null
    }
  }
}
