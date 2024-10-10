
package com.android.solvit.repository

import android.util.Log
import com.android.solvit.ui.screens.profile.UserProfile
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.google.firebase.firestore.DocumentSnapshot

class FirebaseRepositoryImp(private val db: FirebaseFirestore):FirebaseRepository{
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val collectionPath = "user"


    override fun init(onSuccess: () -> Unit) {
        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                onSuccess()
            }
        }}

    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }


    override fun getUserProfile(onSuccess: (List<UserProfile>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("RepositoryFirestore", "getUserProfile")
        db.collection(collectionPath).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.mapNotNull { document -> documentToUser(document) } ?: emptyList()
                onSuccess(user)
            } else {
                task.exception?.let { e ->
                    Log.e("RepositoryFirestore", "Error getting documents", e)
                    onFailure(e)
                }
            }
        }
    }

    override fun updateUserProfile(profile: UserProfile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        performFirestoreOperation(
            db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
    }





    override fun deleteUserProfile(onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .delete()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override fun getCurrentUserPhoneNumber(): String? {
        return auth.currentUser?.phoneNumber
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
                    Log.e("RepositoryFirestore", "Error performing Firestore operation", e)
                    onFailure(e)
                }
            }
        }
    }

    private fun documentToUser(document: DocumentSnapshot): UserProfile? {
        return try {
            val uid = document.id
            val name = document.getString("name") ?: return null
            val email= document.getString("email") ?: return null
            val phone= document.getString("phone") ?: return null


            UserProfile(
                uid = uid,
                name = name,
               email=email,
                phone=phone)
        } catch (e: Exception) {
            Log.e("TodosRepositoryFirestore", "Error converting document to ToDo", e)
            null
        }
    }


}

