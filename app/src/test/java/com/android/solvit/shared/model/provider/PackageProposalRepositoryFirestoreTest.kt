package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalRepositoryFirestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PackageProposalRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var proposalRepositoryFirestore: PackageProposalRepositoryFirestore

  private val proposal =
      PackageProposal(
          "test", packageNumber = 0.0, providerId = "1", "test", "test", 0.0, emptyList())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    proposalRepositoryFirestore = PackageProposalRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun init_doesNotCallOnSuccessWhenUserIsNotAuthenticated() {
    `when`(mockAuth.currentUser).thenReturn(null)

    var onSuccessCalled = false
    proposalRepositoryFirestore.init { onSuccessCalled = true }

    Assert.assertFalse(onSuccessCalled)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("test uid")
    MatcherAssert.assertThat(proposalRepositoryFirestore.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun addPackage() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    proposalRepositoryFirestore.addPackageProposal(proposal, {}, {})
    verify(mockDocumentReference).set(eq(proposal))
  }

  @Test
  fun deletePackage() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
    proposalRepositoryFirestore.deletePackageProposal(proposal.uid, {}, {})
    verify(mockDocumentReference).delete()
  }

  @Test
  fun updatePackage() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
    proposalRepositoryFirestore.updatePackageProposal(proposal, {}, {})
    verify(mockDocumentReference).set(eq(proposal))
  }

  @Test
  fun getPackages() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())
    proposalRepositoryFirestore.getPackageProposal({}, { TestCase.fail("Should not fail") })
    verify(mockCollectionReference).get()
  }

  @Test
  fun documentToPackageProposal() {
    val document = mock(DocumentSnapshot::class.java)
    `when`(document.id).thenReturn("1")
    `when`(document.getDouble("packageNumber")).thenReturn(1.0)
    `when`(document.getString("providerId")).thenReturn("1234")
    `when`(document.getString("title")).thenReturn("title")
    `when`(document.getString("description")).thenReturn("description")
    `when`(document.getDouble("price")).thenReturn(0.0)
    `when`(document.get("bulletPoints")).thenReturn(emptyList<String>())
    val proposal = proposalRepositoryFirestore.documentToPackageProposal(document)
    assertEquals(1.0, proposal?.packageNumber)
    assertEquals("1234", proposal?.providerId)
    assertEquals("1", proposal?.uid)
    assertEquals("title", proposal?.title)
    assertEquals("description", proposal?.description)
    assertEquals(0.0, proposal?.price)
    // assertEquals(emptyList(), proposal?.bulletPoints)
  }
}
