package com.android.solvit.seeker.model.profile

import android.net.Uri
import com.android.solvit.shared.model.map.Location

interface UserRepository {
  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  fun getUsersProfile(onSuccess: (List<SeekerProfile>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserProfile(
      uid: String,
      onSuccess: (SeekerProfile) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updateUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun addUserProfile(
      profile: SeekerProfile,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteUserProfile(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateUserLocations(
      userId: String,
      newLocation: Location,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getCachedLocation(
      userId: String,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun addUserPreference(
      userId: String,
      preference: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteUserPreference(
      userId: String,
      preference: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getUserPreferences(
      userId: String,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  suspend fun returnSeekerById(uid: String): SeekerProfile?
}
