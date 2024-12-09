package com.android.solvit.shared.model.provider

import android.net.Uri
import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.uploadImageToStorage
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProviderRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProviderRepository {

  private val collectionPath = "providers"
  private val providersImagesPath = "providersImages/"

  private fun convertDoc(doc: DocumentSnapshot): Provider? {
    try {
      val name = doc.getString("name") ?: return null
      val serviceString = doc.getString("service") ?: return null
      val service = Services.valueOf(serviceString)
      val imageUrl = doc.getString("imageUrl") ?: return null
      val description = (doc.getString("description")) ?: return null
      val locationDoc = doc.get("location") as? Map<*, *>
      val latitude = locationDoc?.get("latitude") as? Double ?: return null
      val longitude = locationDoc?.get("longitude") as? Double ?: return null
      val nameLoc = locationDoc?.get("name") as? String ?: return null
      val location = Location(latitude, longitude, nameLoc)
      val rating = doc.getDouble("rating") ?: return null
      val popular = doc.getBoolean("popular") ?: return null
      val price = doc.getDouble("price") ?: return null
      val nbrOfJobs = doc.getDouble("nbrOfJobs") ?: return null
      val languages = (doc.get("languages") as List<*>).map { Language.valueOf(it as String) }
      val companyName = doc.getString("companyName") ?: ""
      val phone = doc.getString("phone") ?: ""

      // Convert schedule
      val scheduleDoc = doc.get("schedule") as? Map<*, *>
      val schedule = convertSchedule(scheduleDoc)

      return Provider(
          doc.id,
          name,
          service,
          imageUrl,
          companyName,
          phone,
          location,
          description,
          popular,
          rating,
          price,
          nbrOfJobs = nbrOfJobs,
          languages,
          schedule)
    } catch (e: Exception) {
      Log.e("ProviderRepositoryFirestore", "failed to convert doc $e")
      return null
    }
  }

  /**
   * Converts a Firestore document map into a Schedule object.
   *
   * @param scheduleDoc The map containing schedule data from Firestore, with the following
   *   structure:
   *     - "regularHours": Map<String, List<Map>> where each entry is a day mapped to a list of time
   *       slots
   *     - "_exceptions": List<Map> where each map contains a timestamp and list of time slots for
   *       special dates
   *
   * @return A Schedule object containing the regular hours and exceptions. Returns an empty
   *   Schedule if input is null.
   */
  private fun convertSchedule(scheduleDoc: Map<*, *>?): Schedule {
    if (scheduleDoc == null) return Schedule()

    // Convert regular hours
    val regularHours =
        (scheduleDoc["regularHours"] as? Map<*, *>)
            ?.mapNotNull { (day, slots) ->
              val dayStr = (day as? String) ?: return@mapNotNull null
              val convertedSlots = convertTimeSlots(slots as? List<*>).toSet().toMutableList()
              dayStr to convertedSlots
            }
            ?.toMap()
            ?.toMutableMap() ?: mutableMapOf()

    // Convert exceptions
    val exceptions =
        (scheduleDoc["_exceptions"] as? List<*>)
            ?.mapNotNull { exception ->
              (exception as? Map<*, *>)?.let { exMap ->
                val timestamp = exMap["timestamp"] as? Timestamp ?: return@mapNotNull null
                val timeSlots = convertTimeSlots(exMap["timeSlots"] as? List<*>)
                ScheduleException(timestamp, timeSlots.toMutableList())
              }
            }
            ?.toMutableList() ?: mutableListOf()

    return Schedule(regularHours, exceptions)
  }

  override fun init(onSuccess: () -> Unit) {
    FirebaseAuth.getInstance().addAuthStateListener { onSuccess() }
  }

  override fun addListenerOnProviders(
      onSuccess: (List<Provider>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).addSnapshotListener { value, error ->
      if (error != null) {
        onFailure(error)
        return@addSnapshotListener
      }
      if (value != null) {
        val providers = value.mapNotNull { convertDoc(it) }
        onSuccess(providers)
      }
    }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun addProvider(
      provider: Provider,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    if (imageUri != null) {
      uploadImageToStorage(
          storage,
          providersImagesPath,
          imageUri,
          onSuccess = { imageUrl ->
            Log.e("UploadImageTo Storage", "$imageUrl")
            val providerWithImage = provider.copy(imageUrl = imageUrl)
            performFirestoreOperation(
                db.collection(collectionPath).document(provider.uid).set(providerWithImage),
                onSuccess,
                onFailure)
          },
          onFailure = { Log.e("add Provider", "Failed to add provider $it") })
    } else {
      performFirestoreOperation(
          db.collection(collectionPath).document(provider.uid).set(provider), onSuccess, onFailure)
    }
  }

  override fun deleteProvider(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(uid).delete(), onSuccess, onFailure)
  }

  override fun updateProvider(
      provider: Provider,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(provider.uid).set(provider), onSuccess, onFailure)
  }

  override fun filterProviders(filter: () -> Unit) {
    filter()
  }

  override fun getProviders(
      service: Services?,
      onSuccess: (List<Provider>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (service != null) {
      val collectionRef = db.collection(collectionPath).whereEqualTo("service", service.toString())
      collectionRef.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val providers =
              task.result?.mapNotNull { document -> convertDoc(document) } ?: emptyList()
          onSuccess(providers)
        } else {
          Log.e("ProviderRepositoryFirestore", "failed to get Providers")
          task.exception?.let { onFailure(it) }
        }
      }
    } else {
      val collectionRef = db.collection(collectionPath)
      collectionRef.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val providers =
              task.result?.mapNotNull { document -> convertDoc(document) } ?: emptyList()
          onSuccess(providers)
        } else {
          Log.e("ProviderRepositoryFirestore", "failed to get Providers")
          task.exception?.let { onFailure(it) }
        }
      }
    }
  }

  override fun getProvider(
      userId: String,
      onSuccess: (Provider?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.e("Get Provider", "Debut $userId")
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        val provider = convertDoc(doc)
        Log.e("Let's go", "$provider")

        onSuccess(provider)
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }

  override suspend fun returnProvider(uid: String): Provider? {
    return try {
      val doc = db.collection(collectionPath).document(uid).get().await()
      val provider = convertDoc(doc)
      Log.e("Get Provider", "Success: $provider")
      provider
    } catch (e: Exception) {
      Log.e("Get Provider", "Failed to get provider: $e")
      null
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
          Log.e("ProviderRepositoryFirestore", "Failed to perform Task ")
          onFailure(e)
        }
      }
    }
  }

  /**
   * Converts a list of time slot maps from Firestore into a list of TimeSlot objects.
   *
   * @param slots A list of maps, where each map contains:
   *     - "startHour": Long - The hour when the time slot starts (0-23)
   *     - "startMinute": Long - The minute when the time slot starts (0-59)
   *     - "endHour": Long - The hour when the time slot ends (0-23)
   *     - "endMinute": Long - The minute when the time slot ends (0-59)
   *
   * @return A list of TimeSlot objects. Returns an empty list if input is null or conversion fails.
   */
  private fun convertTimeSlots(slots: List<*>?): List<TimeSlot> {
    return slots?.mapNotNull { slot ->
      (slot as? Map<*, *>)?.let { slotMap ->
        val startHour = (slotMap["startHour"] as? Long)?.toInt() ?: return@mapNotNull null
        val startMinute = (slotMap["startMinute"] as? Long)?.toInt() ?: return@mapNotNull null
        val endHour = (slotMap["endHour"] as? Long)?.toInt() ?: return@mapNotNull null
        val endMinute = (slotMap["endMinute"] as? Long)?.toInt() ?: return@mapNotNull null
        TimeSlot(startHour, startMinute, endHour, endMinute)
      }
    } ?: emptyList()
  }
}
