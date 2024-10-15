package com.android.solvit.shared.model.authentication

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {
    private val collectionPath = "users"

    fun init(onSuccess: (user: User?) -> Unit) {
        if (auth.currentUser != null) {
            val userId = auth.currentUser?.uid ?: return onSuccess(null)
            db.collection(collectionPath).document(userId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(docToUser(doc))
                } else {
                    Log.w("AuthRepository", "User not found in database")
                    onSuccess(null)
                }
            }
        } else {
            onSuccess(null)
        }
    }

    fun loginWithEmailAndPassword(email: String, password: String, onSuccess: (user: User) -> Unit, onFailure: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AuthRepository", "Login successful")
                    it.result?.user?.uid?.let { userId -> fetchUserDocument(userId, onSuccess, onFailure) }
                } else {
                    Log.w("AuthRepository", "Login failed", it.exception)
                    onFailure(it.exception!!)
                }
            }
    }

    fun signInWithGoogle(account: GoogleSignInAccount, onSuccess: (user: User) -> Unit, onFailure: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AuthRepository", "Google sign-in successful")
                    it.result?.user?.uid?.let { userId -> fetchUserDocument(userId, onSuccess, onFailure) }
                } else {
                    Log.w("AuthRepository", "Google sign-in failed", it.exception)
                    onFailure(it.exception!!)
                }
            }
    }

    fun registerWithEmailAndPassword(role: String, email: String, password: String, onSuccess: (user: User) -> Unit, onFailure: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AuthRepository", "Registration successful")
                    val user = User(it.result!!.user!!.uid, role, email)
                    createUserDocument(user, { onSuccess(user) }, onFailure)
                } else {
                    Log.w("AuthRepository", "Registration failed", it.exception)
                    onFailure(it.exception!!)
                }
            }
    }

    fun registerWithGoogle(account: GoogleSignInAccount, role: String, onSuccess: (user: User) -> Unit, onFailure: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("AuthRepository", "Google sign-in successful")
                    val user = User(it.result!!.user!!.uid, role, it.result!!.user!!.email!!)
                    createUserDocument(user, { onSuccess(user) }, onFailure)
                } else {
                    Log.w("AuthRepository", "Google sign-in failed", it.exception)
                    onFailure(it.exception!!)
                }
            }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }

    private fun fetchUserDocument(userId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath).document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val user = docToUser(doc)
                if (user != null) {
                    onSuccess(user)
                } else {
                    Log.w("AuthRepository", "Failed to convert user document")
                    onFailure(Exception("Failed to convert user document"))
                }
            } else {
                Log.w("AuthRepository", "User not found in database")
                onFailure(Exception("User not found in database"))
            }
        }.addOnFailureListener { e ->
            Log.w("AuthRepository", "Failed to fetch user document", e)
            onFailure(e)
        }
    }

    private fun createUserDocument(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath).document(user.uid).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.w("AuthRepository", "Failed to add user to database", e)
                onFailure(e)
            }
    }

    private fun docToUser(doc: DocumentSnapshot): User? {
        val uid = doc.getString("uid") ?: return null
        val role = doc.getString("role") ?: return null
        val email = doc.getString("email") ?: return null
        val profileData = doc.get("profileData") as? Map<String, Any> ?: emptyMap()
        return User(uid, role, email, profileData)
    }
}