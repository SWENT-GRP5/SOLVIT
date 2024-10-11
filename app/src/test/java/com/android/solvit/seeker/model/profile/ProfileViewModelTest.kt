package com.android.solvit.seeker.model.profile

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class ProfileViewModelTest {

  private lateinit var profileViewModel: ProfileViewModel
  private lateinit var firebaseRepository: FirebaseRepositoryImp

  val testProfile =
      UserProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = "Chemin des Triaudes")

  @Before
  fun setUp() {
    firebaseRepository = mock(FirebaseRepositoryImp::class.java)
    profileViewModel = ProfileViewModel(firebaseRepository)
  }

  @Test
  fun getNewUidCallsRepository() {

    `when`(firebaseRepository.getNewUid()).thenReturn("12345")
    val newUid = profileViewModel.getNewUid()
    assertThat(newUid, `is`("12345"))
    verify(firebaseRepository).getNewUid()
  }

  @Test
  fun getUserProfileCallsRepository() {

    profileViewModel.getUserProfile()
    verify(firebaseRepository).getUserProfile(any(), any())
  }

  @Test
  fun updateUserProfileUpdatesLocalProfile() {

    profileViewModel.updateUserProfile(testProfile)
    val updatedProfile = profileViewModel.userProfile.value
    assertThat(updatedProfile[0], `is`(testProfile))
  }

  @Test
  fun deleteUserProfileCallsRepository() {

    doNothing().`when`(firebaseRepository).deleteUserProfile(eq(testProfile.uid), any(), any())
    profileViewModel.deleteUserProfile(testProfile.uid)
    verify(firebaseRepository).deleteUserProfile(eq(testProfile.uid), any(), any())
  }
}
