package com.android.solvit.shared.model.authentication

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.notifications.FcmTokenManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthViewModelTest {
  @MockK private lateinit var authRepository: AuthRepository
  @MockK private lateinit var fcmTokenManager: FcmTokenManager
  @MockK private lateinit var mockGoogleSignInAccount: GoogleSignInAccount

  private lateinit var authViewModel: AuthViewModel
  private val testUserId = "test-user-id"
  private val testFcmToken = "test-fcm-token"
  private val testUser = User(testUserId, "role", "name", "email", emptyList())
  private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    // Mock GoogleSignInAccount
    every { mockGoogleSignInAccount.id } returns "test-google-id"
    every { mockGoogleSignInAccount.email } returns "test@example.com"
    every { mockGoogleSignInAccount.displayName } returns "Test User"

    // Mock AuthRepository
    every { authRepository.getUserId() } returns testUserId
    every { authRepository.init(any()) } answers { firstArg<(User?) -> Unit>().invoke(null) }
    coJustRun { fcmTokenManager.updateUserFcmToken(any(), any()) }

    authViewModel = AuthViewModel(authRepository, fcmTokenManager)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun setRole() =
      testScope.runTest {
        authViewModel.setRole("role")
        assert(authViewModel.role.value == "role")
      }

  @Test
  fun setEmail() =
      testScope.runTest {
        authViewModel.setEmail("email")
        assert(authViewModel.email.value == "email")
      }

  @Test
  fun setPassword() =
      testScope.runTest {
        authViewModel.setPassword("password")
        assert(authViewModel.password.value == "password")
      }

  @Test
  fun setGoogleAccount() =
      testScope.runTest {
        authViewModel.setGoogleAccount(mockGoogleSignInAccount)
        assert(authViewModel.googleAccount.value == mockGoogleSignInAccount)
      }

  @Test
  fun registered() =
      testScope.runTest {
        authViewModel.registered()
        assert(authViewModel.userRegistered.value)
      }

  @Test
  fun loginWithEmailAndPassword() =
      testScope.runTest {
        // Arrange
        coEvery { authRepository.loginWithEmailAndPassword(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<(User) -> Unit>().invoke(testUser)
            }

        // Act
        authViewModel.loginWithEmailAndPassword({}, {})

        // Assert
        coVerify { authRepository.loginWithEmailAndPassword(any(), any(), any(), any()) }
      }

  @Test
  fun registerWithEmailAndPassword() =
      testScope.runTest {
        // Arrange
        coEvery {
          authRepository.registerWithEmailAndPassword(any(), any(), any(), any(), any())
        } coAnswers { arg<(User) -> Unit>(3).invoke(testUser) }

        // Act
        authViewModel.registerWithEmailAndPassword({}, {})

        // Assert
        coVerify { authRepository.registerWithEmailAndPassword(any(), any(), any(), any(), any()) }
      }

  @Test
  fun signInWithGoogle() =
      testScope.runTest {
        // Arrange
        authViewModel.setGoogleAccount(mockGoogleSignInAccount)
        coEvery { authRepository.signInWithGoogle(any(), any(), any()) } coAnswers
            {
              secondArg<(User) -> Unit>().invoke(testUser)
            }

        // Act
        authViewModel.signInWithGoogle({}, {})

        // Assert
        coVerify { authRepository.signInWithGoogle(any(), any(), any()) }
      }

  @Test
  fun signInWithGoogleFailsWithoutAccount() =
      testScope.runTest {
        // Arrange
        var failureCalled = false

        // Act
        authViewModel.signInWithGoogle({}, { failureCalled = true })

        // Assert
        assert(failureCalled)
        coVerify(exactly = 0) { authRepository.signInWithGoogle(any(), any(), any()) }
      }

  @Test
  fun registerWithGoogle() =
      testScope.runTest {
        // Arrange
        authViewModel.setGoogleAccount(mockGoogleSignInAccount)
        coEvery { authRepository.registerWithGoogle(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<(User) -> Unit>().invoke(testUser)
            }

        // Act
        authViewModel.registerWithGoogle({}, {})

        // Assert
        coVerify { authRepository.registerWithGoogle(any(), any(), any(), any()) }
      }

  @Test
  fun registerWithGoogleFailsWithoutAccount() =
      testScope.runTest {
        // Arrange
        var failureCalled = false

        // Act
        authViewModel.registerWithGoogle({}, { failureCalled = true })

        // Assert
        assert(failureCalled)
        coVerify(exactly = 0) { authRepository.registerWithGoogle(any(), any(), any(), any()) }
      }

  @Test
  fun logout() =
      testScope.runTest {
        // Arrange
        coEvery { authRepository.logout(any()) } coAnswers { firstArg<() -> Unit>().invoke() }

        // Act
        authViewModel.logout {}

        // Assert
        coVerify { authRepository.logout(any()) }
        assert(authViewModel.user.value == null)
        assert(authViewModel.email.value == "")
        assert(authViewModel.password.value == "")
        assert(authViewModel.googleAccount.value == null)
        assert(!authViewModel.userRegistered.value)
        assert(authViewModel.role.value == "")
      }

  @Test
  fun `setUserName updates user name successfully`() =
      testScope.runTest {
        // Arrange
        val testUserName = "Test User"
        every { authRepository.init(any()) } answers
            {
              firstArg<(User?) -> Unit>().invoke(testUser)
            }
        authViewModel = AuthViewModel(authRepository, fcmTokenManager)

        coEvery { authRepository.setUserName(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<() -> Unit>().invoke()
            }

        // Act
        authViewModel.setUserName(testUserName)

        // Assert
        coVerify { authRepository.setUserName(eq(testUserName), eq(testUser.uid), any(), any()) }
      }

  @Test
  fun `setUserName handles null user`() =
      testScope.runTest {
        // Arrange
        val testUserName = "Test User"

        // Act
        authViewModel.setUserName(testUserName)

        // Assert
        coVerify(exactly = 0) { authRepository.setUserName(any(), any(), any(), any()) }
      }

  @Test
  fun `addUserLocation handles duplicate location`() =
      testScope.runTest {
        // Arrange
        val location = Location(0.0, 0.0, "Test Location")
        val testUserWithLocation = testUser.copy(locations = listOf(location))
        every { authRepository.init(any()) } answers
            {
              firstArg<(User?) -> Unit>().invoke(testUserWithLocation)
            }
        authViewModel = AuthViewModel(authRepository, fcmTokenManager)

        var successCalled = false

        coEvery { authRepository.updateUserLocations(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<() -> Unit>().invoke()
            }

        // Act
        authViewModel.addUserLocation(location, { successCalled = true }, { _: Exception -> })

        // Assert
        assert(successCalled)
        coVerify(exactly = 0) { authRepository.updateUserLocations(any(), any(), any(), any()) }
      }

  @Test
  fun `addUserLocation respects maximum locations limit`() =
      testScope.runTest {
        // Arrange
        val locations = List(5) { i -> Location(i.toDouble(), i.toDouble(), "Location $i") }
        val newLocation = Location(10.0, 10.0, "New Location")
        val testUserWithLocations = testUser.copy(locations = locations)
        every { authRepository.init(any()) } answers
            {
              firstArg<(User?) -> Unit>().invoke(testUserWithLocations)
            }
        authViewModel = AuthViewModel(authRepository, fcmTokenManager)

        coEvery { authRepository.updateUserLocations(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<() -> Unit>().invoke()
            }

        // Act
        authViewModel.addUserLocation(newLocation, {}, { _: Exception -> })

        // Assert
        coVerify {
          authRepository.updateUserLocations(
              eq(testUserId),
              withArg { updatedLocations ->
                assert(updatedLocations.count() == 5 && updatedLocations[0] == newLocation)
                true
              },
              any(),
              any())
        }
      }

  @Test
  fun `removeUserLocation handles non-existent location`() =
      testScope.runTest {
        // Arrange
        val location = Location(0.0, 0.0, "Test Location")
        every { authRepository.init(any()) } answers
            {
              firstArg<(User?) -> Unit>().invoke(testUser)
            }
        authViewModel = AuthViewModel(authRepository, fcmTokenManager)

        var successCalled = false

        coEvery { authRepository.updateUserLocations(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<() -> Unit>().invoke()
            }

        // Act
        authViewModel.removeUserLocation(location, { successCalled = true }, { _: Exception -> })

        // Assert
        assert(successCalled)
        coVerify(exactly = 0) { authRepository.updateUserLocations(any(), any(), any(), any()) }
      }

  @Test
  fun `updateFcmToken succeeds with valid user and token`() =
      testScope.runTest {
        // Arrange
        every { authRepository.getUserId() } returns testUserId
        coEvery { authRepository.loginWithEmailAndPassword(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<(User) -> Unit>().invoke(testUser)
            }
        coJustRun { fcmTokenManager.updateUserFcmToken(any(), any()) }

        // Mock Firebase Messaging token
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns
            mockk { every { token } returns Tasks.forResult(testFcmToken) }

        // Act
        authViewModel.loginWithEmailAndPassword({}, {})

        // Assert
        coVerify { fcmTokenManager.updateUserFcmToken(eq(testUserId), eq(testFcmToken)) }
      }

  @Test
  fun `updateFcmToken handles empty user ID`() =
      testScope.runTest {
        // Arrange
        every { authRepository.getUserId() } returns ""
        coEvery { authRepository.loginWithEmailAndPassword(any(), any(), any(), any()) } coAnswers
            {
              thirdArg<(User) -> Unit>().invoke(testUser)
            }

        // Act
        authViewModel.loginWithEmailAndPassword({}, {})

        // Assert
        coVerify(exactly = 0) { fcmTokenManager.updateUserFcmToken(any(), any()) }
      }
}
