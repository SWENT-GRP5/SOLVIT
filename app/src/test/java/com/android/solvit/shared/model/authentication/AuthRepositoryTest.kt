package com.android.solvit.shared.model.authentication

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class AuthRepositoryTest {

  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollection: CollectionReference
  @Mock private lateinit var mockUser: FirebaseUser
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockGoogleSignInAccount: GoogleSignInAccount
  @Mock private lateinit var mockTaskAuth: Task<AuthResult>
  @Mock private lateinit var mockAuthResult: AuthResult
  @Mock private lateinit var mockTaskDoc: Task<DocumentSnapshot>
  @Mock private lateinit var mockTask: Task<Void>

  private lateinit var authRepository: AuthRepository
  private val uid = "testUid"
  private val collectionPath = "users"
  private val user = User("testUid", "testRole", "test@test.com")
  private val exception = Exception("test exception")
  private val idToken = "testIdToken"

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    authRepository = AuthRepository(mockAuth, mockFirestore)

    `when`(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTaskAuth)
    `when`(mockAuth.signInWithCredential(any())).thenReturn(mockTaskAuth)
    `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
        .thenReturn(mockTaskAuth)
    `when`(mockTaskAuth.result).thenReturn(mockAuthResult)
    `when`(mockAuthResult.user).thenReturn(mockUser)
    `when`(mockGoogleSignInAccount.idToken).thenReturn(idToken)
    `when`(mockUser.email).thenReturn("test@test.com")

    `when`(mockFirestore.collection(collectionPath)).thenReturn(mockCollection)
    `when`(mockCollection.document(uid)).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(mockTaskDoc)
    `when`(mockDocumentReference.set(any())).thenReturn(mockTask)
    `when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("testUid")
    `when`(mockDocumentSnapshot.getString("role")).thenReturn("testRole")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("test@test.com")
  }

  @Test
  fun testInitWithNoUser() {
    `when`(mockAuth.currentUser).thenReturn(null)

    var result: User? = user
    authRepository.init { user -> result = user }

    assert(result == null)
  }

  @Test
  fun testInitWithUser() {
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn(uid)
    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)

    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      null
    }

    var result: User? = null
    authRepository.init { user -> result = user }

    assert(result == user)
    verify(mockDocumentReference).get()
    verify(mockTaskDoc).addOnSuccessListener(any())
  }

  @Test
  fun testInitWithUserNotFound() {
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn(uid)
    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      null
    }

    var result: User? = user
    authRepository.init { user -> result = user }

    assert(result == null)
    verify(mockDocumentReference).get()
    verify(mockTaskDoc).addOnSuccessListener(any())
  }

  @Test
  fun testLoginWithEmailAndPasswordSuccess() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(true)
    `when`(mockUser.uid).thenReturn(uid)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTaskDoc
    }
    `when`(mockTaskDoc.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Document not found"))
      null
    }

    var result: User? = null
    val onFailure: (Exception) -> Unit = mock()
    authRepository.loginWithEmailAndPassword(
        "test@example.com", "password", { result = it }, onFailure)

    assert(result == user)
    verify(mockAuth).signInWithEmailAndPassword("test@example.com", "password")
    verify(mockDocumentReference).get()
    verify(mockTaskDoc).addOnSuccessListener(any())
    verify(mockTaskDoc).addOnFailureListener(any())
  }

  @Test
  fun testLoginWithEmailAndPasswordFailure() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(false)
    `when`(mockTaskAuth.exception).thenReturn(exception)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    var result: User? = null
    val onFailure: (Exception) -> Unit = { assert(it.message!! == "test exception") }
    authRepository.loginWithEmailAndPassword(
        "test@example.com", "password", { result = it }, onFailure)

    assert(result == null)
    verify(mockAuth).signInWithEmailAndPassword("test@example.com", "password")
  }

  @Test
  fun testSignInWithGoogleSuccess() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(true)
    `when`(mockUser.uid).thenReturn(uid)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTaskDoc
    }
    `when`(mockTaskDoc.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Document not found"))
      null
    }

    var result: User? = null
    val onFailure: (Exception) -> Unit = mock()
    authRepository.signInWithGoogle(mockGoogleSignInAccount, { result = it }, onFailure)

    assert(result == user)
    verify(mockAuth).signInWithCredential(any())
    verify(mockDocumentReference).get()
    verify(mockTaskDoc).addOnSuccessListener(any())
    verify(mockTaskDoc).addOnFailureListener(any())
  }

  @Test
  fun testSignInWithGoogleFailure() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(false)
    `when`(mockTaskAuth.exception).thenReturn(exception)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    var result: User? = null
    val onFailure: (Exception) -> Unit = { assert(it.message!! == "test exception") }
    authRepository.signInWithGoogle(mockGoogleSignInAccount, { result = it }, onFailure)

    assert(result == null)
    verify(mockAuth).signInWithCredential(any())
  }

  @Test
  fun testRegisterWithEmailAndPasswordSuccess() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(true)
    `when`(mockUser.uid).thenReturn(uid)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Document not found"))
      null
    }

    var result: User? = null
    val onFailure: (Exception) -> Unit = mock()
    authRepository.registerWithEmailAndPassword(
        "testRole", "test@example.com", "password", { result = it }, onFailure)

    verify(mockAuth).createUserWithEmailAndPassword("test@example.com", "password")
    verify(mockDocumentReference).set(any())
    verify(mockTask).addOnSuccessListener(any())
    verify(mockTask).addOnFailureListener(any())
  }

  @Test
  fun testRegisterWithEmailAndPasswordFailure() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(false)
    `when`(mockTaskAuth.exception).thenReturn(exception)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    var result: User? = null
    val onFailure: (Exception) -> Unit = { assert(it.message!! == "test exception") }
    authRepository.registerWithEmailAndPassword(
        "testRole", "test@example.com", "password", { result = it }, onFailure)

    assert(result == null)
    verify(mockAuth).createUserWithEmailAndPassword("test@example.com", "password")
  }

  @Test
  fun testRegisterWithGoogleSuccess() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(true)
    `when`(mockUser.uid).thenReturn(uid)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    `when`(mockTask.isSuccessful).thenReturn(true)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Document not found"))
      null
    }

    var result: User? = null
    val onFailure: (Exception) -> Unit = mock()
    authRepository.registerWithGoogle(
        mockGoogleSignInAccount, "testRole", { result = it }, onFailure)

    verify(mockAuth).signInWithCredential(any())
    verify(mockDocumentReference).set(any())
    verify(mockTask).addOnSuccessListener(any())
    verify(mockTask).addOnFailureListener(any())
  }

  @Test
  fun testRegisterWithGoogleFailure() {
    `when`(mockTaskAuth.isSuccessful).thenReturn(false)
    `when`(mockTaskAuth.exception).thenReturn(exception)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
          listener.onComplete(mockTaskAuth)
          null
        }
        .`when`(mockTaskAuth)
        .addOnCompleteListener(any())

    var result: User? = null
    val onFailure: (Exception) -> Unit = { assert(it.message!! == "test exception") }
    authRepository.registerWithGoogle(
        mockGoogleSignInAccount, "testRole", { result = it }, onFailure)

    assert(result == null)
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
    val method =
        AuthRepository::class.java.getDeclaredMethod("docToUser", DocumentSnapshot::class.java)
    method.isAccessible = true

    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("testUid")
    `when`(mockDocumentSnapshot.getString("role")).thenReturn("testRole")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("test@example.com")

    val user = method.invoke(authRepository, mockDocumentSnapshot) as User?
    assertNotNull(user)
    assertEquals("testUid", user?.uid)
    assertEquals("testRole", user?.role)
    assertEquals("test@example.com", user?.email)
  }
}
