package com.android.solvit.seeker.model.profile

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class SeekerProfileViewModelTest {

  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var firebaseRepository: UserRepositoryFirestore

  val testProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = "Chemin des Triaudes")

  @Before
  fun setUp() {
    firebaseRepository = mock(UserRepositoryFirestore::class.java)
    seekerProfileViewModel = SeekerProfileViewModel(firebaseRepository)
  }

  @Test
  fun getNewUidCallsRepository() {

    `when`(firebaseRepository.getNewUid()).thenReturn("12345")
    val newUid = seekerProfileViewModel.getNewUid()
    assertThat(newUid, `is`("12345"))
    verify(firebaseRepository).getNewUid()
  }

  @Test
  fun getUserProfileCallsRepository() {
    seekerProfileViewModel.getUserProfile()
    verify(firebaseRepository).getUserProfile(any(), any())
  }

  @Test
  fun updateUserProfileUpdatesLocalProfile() {
    seekerProfileViewModel.updateUserProfile(testProfile)
    verify(firebaseRepository).updateUserProfile(eq(testProfile), any(), any())
  }

  @Test
  fun deleteUserProfileCallsRepository() {

    doNothing().`when`(firebaseRepository).deleteUserProfile(eq(testProfile.uid), any(), any())
    seekerProfileViewModel.deleteUserProfile(testProfile.uid)
    verify(firebaseRepository).deleteUserProfile(eq(testProfile.uid), any(), any())
  }

  @Test
  fun addUserProfileCallsRepository() {
    seekerProfileViewModel.addUserProfile(testProfile)
    verify(firebaseRepository).addUserProfile(eq(testProfile), any(), any())
  }
}
