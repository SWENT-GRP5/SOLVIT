package com.android.solvit.seeker.model.profile

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
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

  private val testUserId = "12345"
  private val testPreference = "🔧 Plumbing"
  private val userPreferences = listOf("⚡ Electrical Work", "📚 Tutoring")

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

    @Test
    fun `addUserPreference calls repository and refreshes preferences`() {
        // Mocking addUserPreference to immediately call onSuccess
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess.invoke() // Simulate success callback for adding the preference
            null
        }
            .`when`(firebaseRepository)
            .addUserPreference(eq(testUserId), eq(testPreference), any(), any())

        // Mocking getUserPreferences to return a list of preferences via onSuccess callback
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<String>) -> Unit>(1)
            onSuccess.invoke(userPreferences) // Simulate success callback with preferences
            null
        }
            .`when`(firebaseRepository)
            .getUserPreferences(eq(testUserId), any(), any())

        // Call the function in the ViewModel
        seekerProfileViewModel.addUserPreference(testUserId, testPreference)

        // Verify addUserPreference was called
        verify(firebaseRepository).addUserPreference(eq(testUserId), eq(testPreference), any(), any())


    }


    @Test
  fun `deleteUserPreference calls repository and refreshes preferences`() {
    // Mocking deleteUserPreference to immediately call onSuccess
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess.invoke() // Simulate success callback for deletion
          null
        }
        .`when`(firebaseRepository)
        .deleteUserPreference(eq(testUserId), eq(testPreference), any(), any())

    // Mocking getUserPreferences to return a list of preferences via onSuccess callback
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<String>) -> Unit>(1)
          onSuccess.invoke(userPreferences) // Simulate success callback with preferences
          null
        }
        .`when`(firebaseRepository)
        .getUserPreferences(eq(testUserId), any(), any())

    // Call the function in the ViewModel to delete the preference
    seekerProfileViewModel.deleteUserPreference(testUserId, testPreference)

    // Verify deleteUserPreference was called
    verify(firebaseRepository)
        .deleteUserPreference(eq(testUserId), eq(testPreference), any(), any())


  }

  @Test
  fun `getUserPreferences calls repository`() {
    // Mock successful repository call
    doNothing().`when`(firebaseRepository).getUserPreferences(eq(testUserId), any(), any())

    // Call the function in the ViewModel
    seekerProfileViewModel.getUserPreferences(testUserId)

    // Verify repository interaction
    verify(firebaseRepository).getUserPreferences(eq(testUserId), any(), any())
  }
}
