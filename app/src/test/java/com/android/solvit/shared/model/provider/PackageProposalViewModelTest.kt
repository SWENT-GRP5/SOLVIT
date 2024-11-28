package com.android.solvit.shared.model.provider

import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalRepository
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class PackageProposalViewModelTest {
  private lateinit var proposalRepository: PackageProposalRepository
  private lateinit var proposalViewModel: PackageProposalViewModel
  private lateinit var proposal: PackageProposal

  @Before
  fun setup() {
    proposalRepository = Mockito.mock(PackageProposalRepository::class.java)
    proposalViewModel = PackageProposalViewModel(proposalRepository)
    proposal =
        PackageProposal(
            "test", packageNumber = 0.0, providerId = "1", "test", "test", 0.0, emptyList())
  }

  @Test
  fun getNewUid() {
    Mockito.`when`(proposalRepository.getNewUid()).thenReturn("test uid")
    MatcherAssert.assertThat(proposalViewModel.getNewUid(), CoreMatchers.`is`("test uid"))
  }

  @Test
  fun addPackage() {
    proposalViewModel.addPackageProposal(proposal)
    verify(proposalRepository).addPackageProposal(eq(proposal), any(), any())
  }

  @Test
  fun deletePackage() {
    proposalViewModel.deletePackageProposalById(proposal.uid)
    verify(proposalRepository).deletePackageProposal(eq(proposal.uid), any(), any())
  }

  @Test
  fun updatePackage() {
    proposalViewModel.updatePackageProposal(proposal)
    verify(proposalRepository).updatePackageProposal(eq(proposal), any(), any())
  }

  @Test
  fun getPackages() {
    proposalViewModel.getPackageProposal()
    verify(proposalRepository).getPackageProposal(any(), any())
  }

  @Test
  fun selectPackage() {
    proposalViewModel.selectPackage(proposal)
    MatcherAssert.assertThat(proposalViewModel.selectedPackage.value, CoreMatchers.`is`(proposal))
  }
}
