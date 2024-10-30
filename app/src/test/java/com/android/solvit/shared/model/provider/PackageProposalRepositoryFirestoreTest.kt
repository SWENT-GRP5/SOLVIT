package com.android.solvit.shared.model.provider

import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PackageProposalRepositoryFirestoreTest {

        @Mock
        private lateinit var mockFirestore: FirebaseFirestore
        @Mock
        private lateinit var mockDocumentReference: DocumentReference
        @Mock
        private lateinit var mockCollectionReference: CollectionReference
        @Mock
        private lateinit var mockQuerySnapshot: QuerySnapshot

        private lateinit var proposalRepositoryFirestore: PackageProposalRepositoryFirestore

        private val proposal =
            PackageProposal(
                "test",
                "test",
                "test",
                0.0,
                emptyList()
            )

        @Before
        fun setUp() {
            MockitoAnnotations.openMocks(this)

            // Initialize Firebase if necessary
            if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
                FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
            }

            proposalRepositoryFirestore = PackageProposalRepositoryFirestore(mockFirestore)

            Mockito.`when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
            Mockito.`when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
            Mockito.`when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
        }

        @Test
        fun getNewUid() {
            Mockito.`when`(mockDocumentReference.id).thenReturn("test uid")
            MatcherAssert.assertThat(proposalRepositoryFirestore.getNewUid(), CoreMatchers.`is`("test uid"))
        }

        @Test
        fun addPackage() {
            Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
            proposalRepositoryFirestore.addPackageProposal(proposal, {}, {})
            verify(mockDocumentReference).set(eq(proposal))
        }

        @Test
        fun deletePackage() {
            Mockito.`when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
            proposalRepositoryFirestore.deletePackageProposal(proposal.uid, {}, {})
            verify(mockDocumentReference).delete()
        }

        @Test
        fun updatePackage() {
            Mockito.`when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))
            proposalRepositoryFirestore.updatePackageProposal(proposal, {}, {})
            verify(mockDocumentReference).set(eq(proposal))
        }

        @Test
        fun getPackages() {
            Mockito.`when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
            Mockito.`when`(mockQuerySnapshot.documents).thenReturn(listOf())
            proposalRepositoryFirestore.getPackageProposal({}, { TestCase.fail("Should not fail") })
            verify(mockCollectionReference).get()
        }
    }

