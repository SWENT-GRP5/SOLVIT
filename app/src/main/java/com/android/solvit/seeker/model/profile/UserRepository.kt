package com.android.solvit.seeker.model.profile

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

  fun addUserProfile(profile: SeekerProfile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getCurrentUserEmail(): String?

  fun deleteUserProfile(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getCurrentUserPhoneNumber(): String?
}
