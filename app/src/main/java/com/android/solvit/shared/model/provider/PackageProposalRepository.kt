package com.android.solvit.shared.model.provider

interface PackageProposalRepository {
  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  fun getPackageProposal(onSuccess: (List<PackageProposal>) -> Unit, onFailure: (Exception) -> Unit)

  fun addPackageProposal(
      proposal: PackageProposal,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updatePackageProposal(
      proposal: PackageProposal,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deletePackageProposal(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
