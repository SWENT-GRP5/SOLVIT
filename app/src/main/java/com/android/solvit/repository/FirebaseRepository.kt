package com.android.solvit.repository

import com.android.solvit.ui.screens.profile.UserProfile

interface FirebaseRepository {
    fun getNewUid(): String

    fun init(onSuccess: () -> Unit)

   fun getUserProfile(onSuccess: (List<UserProfile>) -> Unit, onFailure: (Exception) -> Unit)

    fun updateUserProfile(profile: UserProfile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun getCurrentUserEmail(): String?

    fun deleteUserProfile(onComplete: (Boolean) -> Unit)

    fun getCurrentUserPhoneNumber(): String?

}