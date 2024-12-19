package com.android.solvit.seeker.model.profile

import android.net.Uri
import com.android.solvit.shared.model.map.Location

// Interface defining repository functions for managing seeker profiles and preferences.
// This interface abstracts away the data source, allowing flexibility in the actual implementation.
interface UserRepository {
  // Generate a new unique user ID.
  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  // Retrieve all seeker profiles from the data source.
  // Calls `onSuccess` with a list of profiles upon success.
  // Calls `onFailure` with an exception if there's an error.
  fun getUsersProfile(onSuccess: (List<SeekerProfile>) -> Unit, onFailure: (Exception) -> Unit)

  // Retrieve a specific user profile by its unique ID (uid).
  // Calls `onSuccess` with the seeker profile upon success.
  // Calls `onFailure` with an exception if there's an error.
  fun getUserProfile(
      uid: String,
      onSuccess: (SeekerProfile) -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Update a user's profile in the data source.
  // Calls `onSuccess` upon successful update.
  // Calls `onFailure` with an exception if there's an error.
  fun updateUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Add a new user profile to the data source.
  // Accepts an optional image URI for the profile picture.
  // Calls `onSuccess` upon successful addition.
  // Calls `onFailure` with an exception if there's an error.
  fun addUserProfile(
      profile: SeekerProfile,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Delete a user profile by its unique ID.
  // Calls `onSuccess` upon successful deletion.
  // Calls `onFailure` with an exception if there's an error.
  fun deleteUserProfile(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  // Update a user's saved locations in the data source.
  // Accepts a new location and updates the list of cached locations.
  // Calls `onSuccess` with the updated list of locations upon success.
  // Calls `onFailure` with an exception if there's an error.
  fun updateUserLocations(
      userId: String,
      newLocation: Location,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Retrieve the cached locations for a user.
  // Calls `onSuccess` with the list of locations upon success.
  // Calls `onFailure` with an exception if there's an error.
  fun getCachedLocation(
      userId: String,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Add a new preference for a user in the data source.
  // Calls `onSuccess` upon successful addition.
  // Calls `onFailure` with an exception if there's an error.
  fun addUserPreference(
      userId: String,
      preference: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Delete a user's preference in the data source.
  // Calls `onSuccess` upon successful deletion.
  // Calls `onFailure` with an exception if there's an error.
  fun deleteUserPreference(
      userId: String,
      preference: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Retrieve the list of preferences for a user.
  // Calls `onSuccess` with the list of preferences upon success.
  // Calls `onFailure` with an exception if there's an error.
  fun getUserPreferences(
      userId: String,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Retrieve a specific seeker profile by its unique ID (uid).
  // Returns the seeker profile if found, or null if not.
  suspend fun returnSeekerById(uid: String): SeekerProfile?
}
