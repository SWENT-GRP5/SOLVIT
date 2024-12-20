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
      val longitude = locationDoc["longitude"] as? Double ?: return null
      val nameLoc = locationDoc["name"] as? String ?: return null
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
      Log.e("ProviderRepositoryFirestore", "Failed to convert doc", e)
      return null
    }
  }

  /** Initializes the repository and triggers the success callback. */
  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  /**
   * Adds a real-time listener to the providers collection in Firestore.
   *
   * @param onSuccess Callback with the list of providers on successful data fetch.
   * @param onFailure Callback with an exception if the operation fails.
   */
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

  /**
   * Adds a provider to Firestore, uploading an image if provided.
   *
   * @param provider The provider data to be added.
   * @param imageUri The optional image URI to upload.
   * @param onSuccess Callback triggered on success.
   * @param onFailure Callback triggered on failure.
   */
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
            Log.e("UploadImageTo Storage", imageUrl)
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

  /**
   * Deletes a provider by its unique Firestore document ID.
   *
   * @param uid The unique provider document ID.
   * @param onSuccess Callback triggered on successful deletion.
   * @param onFailure Callback triggered on failure.
   */
  override fun deleteProvider(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(uid).delete(), onSuccess, onFailure)
  }

  /**
   * Updates the provider's data in Firestore.
   *
   * @param provider The updated provider data.
   * @param onSuccess Callback triggered on success.
   * @param onFailure Callback triggered on failure.
   */
  override fun updateProvider(
      provider: Provider,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(provider.uid).set(provider), onSuccess, onFailure)
  }

  /**
   * Applies the provided filter function to the provider data.
   *
   * @param filter The filter logic to apply.
   */
  override fun filterProviders(filter: () -> Unit) {
    filter()
  }

  /**
   * Fetches providers based on the given service type.
   *
   * @param service The service type to filter by (optional).
   * @param onSuccess Callback triggered with the list of matching providers.
   * @param onFailure Callback triggered on failure.
   */
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

  /**
   * Retrieves a specific provider by its unique Firestore user ID.
   *
   * @param userId The unique provider user ID.
   * @param onSuccess Callback triggered with the retrieved provider.
   * @param onFailure Callback triggered on failure.
   */
  override fun getProvider(
      userId: String,
      onSuccess: (Provider?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(userId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val doc = task.result
        val provider = convertDoc(doc)
        onSuccess(provider)
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }

  /**
   * Fetches a specific provider as a suspend function for coroutines.
   *
   * @param uid The unique provider user ID.
   * @return The provider if found, or null if not.
   */
  override suspend fun returnProvider(uid: String): Provider? {
    return try {
      val doc = db.collection(collectionPath).document(uid).get().await()
      val provider = convertDoc(doc)
      provider
    } catch (e: Exception) {
      Log.e("ProviderRepositoryFirestore", "Failed to get provider", e)
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

        if (startHour != null && startMinute != null && endHour != null && endMinute != null) {
          try {
            TimeSlot(startHour, startMinute, endHour, endMinute)
          } catch (e: IllegalArgumentException) {
            Log.e("ProviderRepositoryFirestore", "Invalid time values: $e")
            null
          }
        } else {
          Log.e("ProviderRepositoryFirestore", "Missing time values in slot: $slotMap")
          null
        }
      } catch (e: Exception) {
        Log.e("ProviderRepositoryFirestore", "Error converting time slot: $e")
        null
      }
    }
  }

  private fun convertSchedule(scheduleMap: Map<String, Any>): Schedule {
    try {
      @Suppress("UNCHECKED_CAST")
      val regularHoursMap = (scheduleMap["regularHours"] as? Map<String, Any>) ?: mapOf()
      @Suppress("UNCHECKED_CAST")
      val exceptionsList = (scheduleMap["exceptions"] as? List<Map<String, Any>>) ?: listOf()
      @Suppress("UNCHECKED_CAST")
      val acceptedTimeSlotsList =
          (scheduleMap["acceptedTimeSlots"] as? List<Map<String, Any>>) ?: listOf()

      Log.d("ProviderRepositoryFirestore", "Converting regularHours: $regularHoursMap")
      Log.d("ProviderRepositoryFirestore", "Converting exceptions: $exceptionsList")
      Log.d("ProviderRepositoryFirestore", "Converting acceptedTimeSlots: $acceptedTimeSlotsList")

      val regularHours = mutableMapOf<String, MutableList<TimeSlot>>()
      for ((day, timeSlotsAny) in regularHoursMap) {
        if (!day.matches("MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY".toRegex())) {
          Log.w("ProviderRepositoryFirestore", "Invalid day format: $day")
          continue
        }
        @Suppress("UNCHECKED_CAST") val timeSlotsList = timeSlotsAny as? List<Map<String, Any>>
        if (timeSlotsList == null) {
          Log.w("ProviderRepositoryFirestore", "Invalid time slots list for day: $day")
          continue
        }
        val slots = convertTimeSlots(timeSlotsList)
        if (slots.isNotEmpty()) {
          regularHours[day] = slots.toMutableList()
        }
      }

      val exceptions = mutableListOf<ScheduleException>()
      for (exceptionMap in exceptionsList) {
        try {
          val timestamp = exceptionMap["timestamp"] as? Timestamp
          if (timestamp == null) {
            Log.w("ProviderRepositoryFirestore", "Missing timestamp in exception: $exceptionMap")
            continue
          }

          @Suppress("UNCHECKED_CAST")
          val timeSlotsAnyList = exceptionMap["timeSlots"] as? List<Map<String, Any>>
          if (timeSlotsAnyList == null) {
            Log.w("ProviderRepositoryFirestore", "Missing timeSlots in exception: $exceptionMap")
            continue
          }

          val typeString = exceptionMap["type"] as? String
          if (typeString == null) {
            Log.w("ProviderRepositoryFirestore", "Missing type in exception: $exceptionMap")
            continue
          }

          val type =
              try {
                ExceptionType.valueOf(typeString)
              } catch (e: IllegalArgumentException) {
                Log.w("ProviderRepositoryFirestore", "Invalid exception type: $typeString")
                continue
              }

          val timeSlots = convertTimeSlots(timeSlotsAnyList)
          if (timeSlots.isEmpty()) {
            Log.w("ProviderRepositoryFirestore", "No valid time slots in exception: $exceptionMap")
            continue
          }

          val date = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
          exceptions.add(ScheduleException(date, timeSlots.toMutableList(), type))
        } catch (e: Exception) {
          Log.e("ProviderRepositoryFirestore", "Error converting exception: ${e.message}")
          continue
        }
      }

      val acceptedTimeSlots = mutableListOf<AcceptedTimeSlot>()
      for (acceptedTimeSlotMap in acceptedTimeSlotsList) {
        try {
          val requestId = acceptedTimeSlotMap["requestId"] as? String
          if (requestId == null) {
            Log.w(
                "ProviderRepositoryFirestore",
                "Missing requestId in accepted time slot: $acceptedTimeSlotMap")
            continue
          }

          val startTime = acceptedTimeSlotMap["startTime"] as? Timestamp
          if (startTime == null) {
            Log.w(
                "ProviderRepositoryFirestore",
                "Missing startTime in accepted time slot: $acceptedTimeSlotMap")
            continue
          }

          val duration = (acceptedTimeSlotMap["duration"] as? Number)?.toInt() ?: 60
          acceptedTimeSlots.add(AcceptedTimeSlot(requestId, startTime, duration))
        } catch (e: Exception) {
          Log.e("ProviderRepositoryFirestore", "Error converting accepted time slot: ${e.message}")
          continue
        }
      }

      val schedule = Schedule(regularHours, exceptions, acceptedTimeSlots)
      Log.d("ProviderRepositoryFirestore", "Successfully converted schedule: $schedule")
      return schedule
    } catch (e: Exception) {
      Log.e("ProviderRepositoryFirestore", "Error converting schedule: ${e.message}")
      return Schedule(mutableMapOf(), mutableListOf(), mutableListOf())
    }
  }
}
