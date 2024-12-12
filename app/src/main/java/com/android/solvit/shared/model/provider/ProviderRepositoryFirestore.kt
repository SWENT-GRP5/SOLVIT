package com.android.solvit.shared.model.provider

import android.net.Uri
import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.uploadImageToStorage
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalTime
import java.time.ZoneId
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
      @Suppress("UNCHECKED_CAST")
      val scheduleMap = (doc.get("schedule") as? Map<String, Any>) ?: mapOf()
      val schedule = convertSchedule(scheduleMap)

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

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
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

  private fun convertTimeSlots(timeSlotsAnyList: List<Map<String, Any>>): List<TimeSlot> {
    return timeSlotsAnyList.mapNotNull { slotMap ->
      try {
        val startHour = (slotMap["startHour"] as? Number)?.toInt()
        val startMinute = (slotMap["startMinute"] as? Number)?.toInt()
        val endHour = (slotMap["endHour"] as? Number)?.toInt()
        val endMinute = (slotMap["endMinute"] as? Number)?.toInt()

        if (startHour != null &&
            startMinute != null &&
            endHour != null &&
            endMinute != null &&
            startHour in 0..23 &&
            startMinute in 0..59 &&
            endHour in 0..23 &&
            endMinute in 0..59) {
          val startTime = LocalTime.of(startHour, startMinute)
          val endTime = LocalTime.of(endHour, endMinute)
          if (startTime.isBefore(endTime)) {
            TimeSlot(startTime, endTime)
          } else null
        } else null
      } catch (e: Exception) {
        null
      }
    }
  }

  private fun convertSchedule(scheduleMap: Map<String, Any>): Schedule {
    @Suppress("UNCHECKED_CAST")
    val regularHoursMap = (scheduleMap["regularHours"] as? Map<String, Any>) ?: mapOf<String, Any>()
    @Suppress("UNCHECKED_CAST")
    val exceptionsList = (scheduleMap["exceptions"] as? List<Map<String, Any>>) ?: listOf()

    val regularHours = mutableMapOf<String, MutableList<TimeSlot>>()
    for ((day, timeSlotsAny) in regularHoursMap) {
      if (!day.matches("MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY".toRegex())) {
        continue
      }
      @Suppress("UNCHECKED_CAST")
      val timeSlotsList = timeSlotsAny as? List<Map<String, Any>> ?: continue
      val slots = convertTimeSlots(timeSlotsList)
      if (slots.isNotEmpty()) {
        regularHours[day] = slots.toMutableList()
      }
    }

    val exceptions = mutableListOf<ScheduleException>()
    for (exceptionMap in exceptionsList) {
      try {
        val timestamp = exceptionMap["timestamp"] as? Timestamp ?: continue
        @Suppress("UNCHECKED_CAST")
        val timeSlotsAnyList = exceptionMap["timeSlots"] as? List<Map<String, Any>> ?: continue
        val typeString = exceptionMap["type"] as? String ?: continue

        val type = ExceptionType.valueOf(typeString)
        val timeSlots = convertTimeSlots(timeSlotsAnyList)
        if (timeSlots.isNotEmpty()) {
          val date = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
          val exception = ScheduleException(date, timeSlots.toMutableList(), type)
          exceptions.add(exception)
        }
      } catch (e: IllegalArgumentException) {
        continue // Skip invalid exception type or data
      }
    }

    return Schedule(regularHours, exceptions)
  }
}
