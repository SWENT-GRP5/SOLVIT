package com.android.solvit.seeker.model.profile

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class SeekerProfileViewModelTest {

  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var authViewModel: AuthViewModel
  private lateinit var authRepository: AuthRepository
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
    authRepository = mock(AuthRepository::class.java)
    authViewModel = AuthViewModel(authRepository)
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
  fun getUsersProfileCallsRepository() {

    seekerProfileViewModel.getUsersProfile()

    verify(firebaseRepository).getUsersProfile(any(), any())
  }

  @Test
  fun getUserProfileCallsRepository() {
    seekerProfileViewModel.getUserProfile("1234")
    verify(firebaseRepository).getUserProfile(any(), any(), any())
  }

  @Test
  fun addUserProfielCallsRepository() {
    seekerProfileViewModel.addUserProfile(testProfile)
    verify(firebaseRepository).addUserProfile(any(), any(), any())
  }

  /*
  @Test
  fun updateUserProfileUpdatesLocalProfile() {

    seekerProfileViewModel.updateUserProfile(testProfile)
    val updatedProfile = seekerProfileViewModel.seekerProfile.value
    assertThat(updatedProfile, `is`(testProfile))
  }*/

  @Test
  fun deleteUserProfileCallsRepository() {

    doNothing().`when`(firebaseRepository).deleteUserProfile(eq(testProfile.uid), any(), any())
    seekerProfileViewModel.deleteUserProfile(testProfile.uid)
    verify(firebaseRepository).deleteUserProfile(eq(testProfile.uid), any(), any())
  }
}
