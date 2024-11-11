package com.android.solvit.shared.model.jobs

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class JobDashboardRepositoryFirestore(private val firestore: FirebaseFirestore) :
    JobDashboardRepository {

  override fun getCurrentJobs(): Flow<List<Job>> = getJobsByStatus("CURRENT")

  override fun getPendingJobs(): Flow<List<Job>> = getJobsByStatus("PENDING")

  override fun getHistoryJobs(): Flow<List<Job>> = getJobsByStatus("HISTORY")

  private fun getJobsByStatus(status: String): Flow<List<Job>> = callbackFlow {
    val listener =
        firestore
            .collection("jobs")
            .whereEqualTo("status", status)
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
              if (e != null) {
                close(e) // Close the flow if there’s an error
                return@addSnapshotListener
              }

              if (snapshot != null) {
                val jobs =
                    snapshot.documents.mapNotNull { document ->
                      document.toObject(Job::class.java)?.apply { id = document.id }
                    }
                trySend(jobs) // Emit the list of jobs to the flow
              }
            }
    awaitClose { listener.remove() } // Remove the listener when the flow is closed
  }
}
