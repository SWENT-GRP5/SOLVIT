package com.android.solvit.shared.model.provider

import com.google.firebase.firestore.FirebaseFirestore

class TaskRepository(private val db: FirebaseFirestore) {

  fun getTasks(onSuccess: (List<Task>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection("tasks").get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val tasks =
            task.result?.mapNotNull { document -> document.toObject(Task::class.java) }
                ?: emptyList()
        onSuccess(tasks)
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }
}
