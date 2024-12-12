package com.android.solvit.shared.model.packages

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class PackageProposalRepositoryFirestore(private val db: FirebaseFirestore) :
    PackageProposalRepository {

  private val collectionPath = "packages"

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getPackageProposal(
      onSuccess: (List<PackageProposal>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("PackageProposalRepositoryFirestore", "getPackageProposal")
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val proposal =
            task.result?.mapNotNull { document -> documentToPackageProposal(document) }
                ?: emptyList()
        onSuccess(proposal)
      } else {
        task.exception?.let { e ->
          Log.e("PackageProposalRepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun addPackageProposal(
      proposal: PackageProposal,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(proposal.uid).set(proposal), onSuccess, onFailure)
  }

  override fun updatePackageProposal(
      proposal: PackageProposal,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(proposal.uid).set(proposal), onSuccess, onFailure)
  }

  override fun deletePackageProposal(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("PackageProposalRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  fun documentToPackageProposal(document: DocumentSnapshot): PackageProposal? {
    return try {
      val uid = document.id
      val title = document.getString("title") ?: return null
      val packageNumber = document.getDouble("packageNumber") ?: return null
      val providerId = document.getString("providerId") ?: return null
      val description = document.getString("description") ?: return null
      val price = document.getDouble("price") ?: return null
      val bulletPoints = document.get("bulletPoints") as? List<String> ?: emptyList()

      PackageProposal(
          uid = uid,
          title = title,
          packageNumber = packageNumber,
          providerId = providerId,
          description = description,
          price = price,
          bulletPoints = bulletPoints)
    } catch (e: Exception) {
      Log.e("PackageProposalRepositoryFirestore", "Error converting document to PackageProposal", e)
      null
    }
  }
}
