package com.android.solvit.shared.model.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PackageProposalViewModel(private val repository: PackageProposalRepository) : ViewModel() {

  private val _proposal = MutableStateFlow<List<PackageProposal>>(emptyList())
  val proposal: StateFlow<List<PackageProposal>> = _proposal.asStateFlow()

  private val _selectedPackage = MutableStateFlow<PackageProposal?>(null)
  val selectedPackage: StateFlow<PackageProposal?> = _selectedPackage.asStateFlow()

  init {
    repository.init { getPackageProposal() }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PackageProposalViewModel(PackageProposalRepositoryFirestore(Firebase.firestore))
                as T
          }
        }
  }

  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** Gets all Packages documents. */
  fun getPackageProposal() {
    repository.getPackageProposal(onSuccess = { _proposal.value = it }, onFailure = {})
  }

  /**
   * Adds a PackageProposal document.
   *
   * @param proposal The PackageProposal document to be added.
   */
  fun addPackageProposal(proposal: PackageProposal) {
    repository.addPackageProposal(
        proposal = proposal, onSuccess = { getPackageProposal() }, onFailure = {})
  }

  /**
   * Updates a PackageProposal document.
   *
   * @param proposal The PackageProposal document to be updated.
   */
  fun updatePackageProposal(proposal: PackageProposal) {
    repository.updatePackageProposal(
        proposal = proposal, onSuccess = { getPackageProposal() }, onFailure = {})
  }

  /**
   * Deletes a PackageProposal document by its ID.
   *
   * @param id The ID of the a PackageProposal document to be deleted.
   */
  fun deletePackageProposalById(id: String) {
    repository.deletePackageProposal(id = id, onSuccess = { getPackageProposal() }, onFailure = {})
  }

  /**
   * Selects a PackageProposal document.
   *
   * @param proposal The PackageProposal document to be selected.
   */
  fun selectPackage(proposal: PackageProposal) {
    _selectedPackage.value = proposal
  }
}
