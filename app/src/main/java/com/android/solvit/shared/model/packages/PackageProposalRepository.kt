package com.android.solvit.shared.model.packages

/**
 * Interface defining the contract for managing package proposals in the repository. It provides
 * methods to initialize the repository, fetch, add, update, and delete package proposals.
 */
interface PackageProposalRepository {
  /**
   * Generates a new unique identifier for a package proposal.
   *
   * @return A unique identifier string.
   */
  fun getNewUid(): String

  /**
   * Initializes the repository and prepares it for package proposal operations.
   *
   * @param onSuccess Callback invoked when the repository is successfully initialized.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves all package proposals from the repository.
   *
   * @param onSuccess Callback invoked with a list of package proposals upon successful retrieval.
   * @param onFailure Callback invoked with an exception if retrieval fails.
   */
  fun getPackageProposal(onSuccess: (List<PackageProposal>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a new package proposal to the repository.
   *
   * @param proposal The package proposal to be added.
   * @param onSuccess Callback invoked when the proposal is successfully added.
   * @param onFailure Callback invoked with an exception if adding the proposal fails.
   */
  fun addPackageProposal(
      proposal: PackageProposal,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates an existing package proposal in the repository.
   *
   * @param proposal The updated package proposal.
   * @param onSuccess Callback invoked when the proposal is successfully updated.
   * @param onFailure Callback invoked with an exception if updating the proposal fails.
   */
  fun updatePackageProposal(
      proposal: PackageProposal,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a package proposal from the repository based on its unique identifier.
   *
   * @param id The unique identifier of the package proposal to be deleted.
   * @param onSuccess Callback invoked when the proposal is successfully deleted.
   * @param onFailure Callback invoked with an exception if deletion fails.
   */
  fun deletePackageProposal(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
