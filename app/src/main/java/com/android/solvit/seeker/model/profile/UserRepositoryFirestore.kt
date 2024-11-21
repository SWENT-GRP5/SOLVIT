package com.android.solvit.seeker.model.profile

import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

open class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  private val collectionPath = "user"

  override fun init(onSuccess: () -> Unit) {
    FirebaseAuth.getInstance().addAuthStateListener { onSuccess() }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun addUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  override fun getUserProfile(
      uid: String,
      onSuccess: (SeekerProfile) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUsersProfile")
    db.collection(collectionPath).document(uid).get().addOnCompleteListener { document ->
      if (document.isSuccessful) {

        val user = documentToUser(document.result)
        if (user != null) {
          onSuccess(user)
        } else {
          Log.e("RepositoryFirestore", "Error getting user")
        }
      } else {
        document.exception?.let { e ->
          Log.e("RepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun getUsersProfile(
      onSuccess: (List<SeekerProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("RepositoryFirestore", "getUsersProfile")
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

  override fun updateUserProfile(
      profile: SeekerProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  override fun deleteUserProfile(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  fun getLocations(doc: DocumentSnapshot): MutableList<Location> {
    val locations =
        (doc.get("cachedLocations") as? List<*>)?.mapNotNull { locationMap ->
          (locationMap as? Map<String, Any>)?.let {
            Location(
                latitude = it["latitude"] as? Double ?: 0.0,
                longitude = it["longitude"] as? Double ?: 0.0,
                name = it["name"] as? String ?: "Unknown")
          }
        } ?: emptyList()
    return locations.toMutableList()
  }

  override fun updateUserLocations(
      userId: String,
      newLocation: Location,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.e("AVANT", userId)
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      Log.e("Apres", "soy aqui $userId")
      if (task.isSuccessful) {
        Log.e("updateUserLocation", "yes task is succesfull")
        val doc = task.result
        var locations = getLocations(doc)
        locations.add(0, newLocation)
        if (locations.size > 2) {
          locations = locations.take(2).toMutableList()
        }
        Log.e("CACHED LOCAATIONS", "$locations")
        db.collection(collectionPath).document(userId).update("cachedLocations", locations.toList())
        onSuccess(locations.toList())
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }

  override fun getCachedLocation(
      userId: String,
      onSuccess: (List<Location>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(userId).get().addOnCompleteListener() { task ->
      if (task.isSuccessful) {
        val doc = task.result
        val locations = getLocations(doc)
        onSuccess(locations)
      } else {

        task.exception?.let { onFailure(it) }
      }
    }
  }

    override fun addUserPreference(
        userId: String,
        preference: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc = task.result
                // Check if the document exists, else create a new one
                val currentPreferences = doc?.get("preferences") as? MutableList<String> ?: mutableListOf()

                // Add the new preference to the list
                if (!currentPreferences.contains(preference)) {
                    currentPreferences.add(preference)
                }

                // Update or create the document with the updated preferences list
                db.collection(collectionPath).document(userId).set(mapOf("preferences" to currentPreferences))
                    .addOnCompleteListener {
                        if (it.isSuccessful) onSuccess()
                        else it.exception?.let(onFailure)
                    }
            } else {
                task.exception?.let(onFailure)
            }
        }
    }


    // Delete a preference from the user's preferences
    override fun deleteUserPreference(
        userId: String,
        preference: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc = task.result
                val currentPreferences = doc?.get("preferences") as? MutableList<String> ?: mutableListOf()

                // Remove the preference from the list
                currentPreferences.remove(preference)

                // Update the document with the new preferences list
                db.collection(collectionPath).document(userId).update("preferences", currentPreferences)
                    .addOnCompleteListener {
                        if (it.isSuccessful) onSuccess()
                        else it.exception?.let(onFailure)
                    }
            } else {
                task.exception?.let(onFailure)
            }
        }
    }


    // Get the list of preferences for the user
    override fun getUserPreferences(
        userId: String,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc = task.result
                val preferences = doc?.get("preferences") as? List<String> ?: emptyList()
                onSuccess(preferences)
            } else {
                task.exception?.let(onFailure)
            }
        }
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

  fun documentToUser(document: DocumentSnapshot): SeekerProfile? {
    return try {
      val uid = document.id
      val name = document.getString("name") ?: return null
      val username = document.getString("username") ?: return null
      val email = document.getString("email") ?: return null
      val phone = document.getString("phone") ?: return null
      val address = document.getString("address") ?: return null
        val preferences = document.get("preferences") as? List<String> ?: emptyList()

      SeekerProfile(
          uid = uid,
          name = name,
          username = username,
          email = email,
          phone = phone,
          address = address,
          preferences= preferences)
    } catch (e: Exception) {
      Log.e("RepositoryFirestore", "Error converting document to UserProfile", e)
      null
    }
  }
}
