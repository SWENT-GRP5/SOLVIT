package com.android.solvit.shared.model.authentication

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class AuthRepositoryTest {

    @Mock private lateinit var mockAuth: FirebaseAuth
    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockUser: FirebaseUser
    @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
    @Mock private lateinit var mockGoogleSignInAccount: GoogleSignInAccount
    @Mock private lateinit var mockTaskAuthResult: AuthResult

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(mockAuth, mockFirestore)
    }

    @Test
    fun testInitWithNoUser() {
        `when`(mockAuth.currentUser).thenReturn(null)

        val onSuccess: (User?) -> Unit = mock()
        authRepository.init(onSuccess)

        verify(onSuccess).invoke(null)
    }

    @Test
    fun testLoginWithEmailAndPasswordSuccess() {
        `when`(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forResult(mockTaskAuthResult))
        `when`(mockTaskAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("testUid")

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.loginWithEmailAndPassword("test@example.com", "password", onSuccess, onFailure)

        verify(mockAuth).signInWithEmailAndPassword("test@example.com", "password")
    }

    @Test
    fun testLoginWithEmailAndPasswordFailure() {
        val exception = Exception("Login failed")
        `when`(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forException(exception))

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.loginWithEmailAndPassword("test@example.com", "password", onSuccess, onFailure)

        verify(mockAuth).signInWithEmailAndPassword("test@example.com", "password")
    }

    @Test
    fun testSignInWithGoogleSuccess() {
        `when`(mockGoogleSignInAccount.idToken).thenReturn("mockIdToken")
        `when`(mockAuth.signInWithCredential(any()))
            .thenReturn(Tasks.forResult(mockTaskAuthResult))
        `when`(mockTaskAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("testUid")

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.signInWithGoogle(mockGoogleSignInAccount, onSuccess, onFailure)

        verify(mockGoogleSignInAccount).idToken
        verify(mockAuth).signInWithCredential(any())
    }

    @Test
    fun testSignInWithGoogleFailure() {
        val exception = Exception("Google sign-in failed")
        `when`(mockGoogleSignInAccount.idToken).thenReturn("mockIdToken")
        `when`(mockAuth.signInWithCredential(any()))
            .thenReturn(Tasks.forException(exception))

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.signInWithGoogle(mockGoogleSignInAccount, onSuccess, onFailure)

        verify(mockGoogleSignInAccount).idToken
        verify(mockAuth).signInWithCredential(any())
    }

    @Test
    fun testRegisterWithEmailAndPasswordSuccess() {
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forResult(mockTaskAuthResult))
        `when`(mockTaskAuthResult.user).thenReturn(mockUser)

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.registerWithEmailAndPassword("testRole", "test@example.com", "password", onSuccess, onFailure)

        verify(mockAuth).createUserWithEmailAndPassword("test@example.com", "password")
    }

    @Test
    fun testRegisterWithEmailAndPasswordFailure() {
        val exception = Exception("Registration failed")
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forException(exception))

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.registerWithEmailAndPassword("testRole", "test@example.com", "password", onSuccess, onFailure)

        verify(mockAuth).createUserWithEmailAndPassword("test@example.com", "password")
    }

    @Test
    fun testRegisterWithGoogleSuccess() {
        `when`(mockGoogleSignInAccount.idToken).thenReturn("mockIdToken")
        `when`(mockAuth.signInWithCredential(any()))
            .thenReturn(Tasks.forResult(mockTaskAuthResult))
        `when`(mockTaskAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("testUid")

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.registerWithGoogle(mockGoogleSignInAccount, "testRole", onSuccess, onFailure)

        verify(mockGoogleSignInAccount).idToken
        verify(mockAuth).signInWithCredential(any())
    }

    @Test
    fun testRegisterWithGoogleFailure() {
        val exception = Exception("Google sign-in failed")
        `when`(mockGoogleSignInAccount.idToken).thenReturn("mockIdToken")
        `when`(mockAuth.signInWithCredential(any()))
            .thenReturn(Tasks.forException(exception))

        val onSuccess: (User) -> Unit = mock()
        val onFailure: (Exception) -> Unit = mock()
        authRepository.registerWithGoogle(mockGoogleSignInAccount, "testRole", onSuccess, onFailure)

        verify(mockGoogleSignInAccount).idToken
        verify(mockAuth).signInWithCredential(any())
    }

    @Test
    fun testLogout() {
        val onSuccess: () -> Unit = mock()
        authRepository.logout(onSuccess)

        verify(onSuccess).invoke()
    }

    @Test
    fun testPrivateDocToUser() {
        val method = AuthRepository::class.java.getDeclaredMethod("docToUser", DocumentSnapshot::class.java)
        method.isAccessible = true

        `when`(mockDocumentSnapshot.getString("uid")).thenReturn("testUid")
        `when`(mockDocumentSnapshot.getString("role")).thenReturn("testRole")
        `when`(mockDocumentSnapshot.getString("email")).thenReturn("test@example.com")

        val user = method.invoke(authRepository, mockDocumentSnapshot) as User?
        TestCase.assertNotNull(user)
        TestCase.assertEquals("testUid", user?.uid)
        TestCase.assertEquals("testRole", user?.role)
        TestCase.assertEquals("test@example.com", user?.email)
    }
}