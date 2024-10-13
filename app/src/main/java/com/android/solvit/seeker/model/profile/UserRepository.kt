package com.android.solvit.seeker.model.profile

interface UserRepository {
  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  fun getUserProfile(onSuccess: (List<SeekerProfile>) -> Unit, onFailure: (Exception) -> Unit)

  fun updateUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getCurrentUserEmail(): String?

  fun deleteUserProfile(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getCurrentUserPhoneNumber(): String?
}
