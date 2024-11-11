package com.android.solvit.shared.model.jobs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JobDashboardViewModel(private val repository: JobDashboardRepository) : ViewModel() {

    private val _pendingJobs = MutableStateFlow<List<Job>>(emptyList())
    val pendingJobs: StateFlow<List<Job>> = _pendingJobs

    private val _currentJobs = MutableStateFlow<List<Job>>(emptyList())
    val currentJobs: StateFlow<List<Job>> = _currentJobs

    private val _historyJobs = MutableStateFlow<List<Job>>(emptyList())
    val historyJobs: StateFlow<List<Job>> = _historyJobs


    init {
    // loadJobs()
    loadHardcodedJobs()
  }

  // Define a companion object for the Factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JobDashboardViewModel(
                repository = JobDashboardRepositoryFirestore(FirebaseFirestore.getInstance()))
                as T
          }
        }
  }

  private fun loadHardcodedJobs() {
    // Define a list of hardcoded jobs for testing
    val hardcodedJobs =
        listOf(
            Job(
                id = "1",
                title = "Fix Leaky Faucet",
                description = "Repair the faucet in the kitchen that is leaking.",
                status = "CURRENT",
                location = GeoPoint(46.517238, 6.629614), // or set a GeoPoint if needed
                date = LocalDate.now(),
                time = LocalTime.of(10, 0),
                locationName = "Place de la Gare 1003 Lausanne"),
            Job(
                id = "2",
                title = "Paint Office Wall",
                description = "Apply one coat of white paint to the main office wall.",
                status = "CURRENT",
                location = GeoPoint(46.538917, 6.588126),
                date = LocalDate.now(),
                time = LocalTime.of(14, 0),
                locationName = "Place de la Gare 1020 Renens "),
            Job(
                id = "3",
                title = "Inspect HVAC",
                description = "Inspect and clean the HVAC system in the building.",
                status = "CURRENT",
                location = GeoPoint(46.511244, 6.496200),
                date = LocalDate.now(),
                time = LocalTime.of(16, 0),
                locationName = "Place de la Gare 1110 Morges"),
            Job(id = "4",
                title = "Fix Faucet",
                description = "Repair leaky faucet",
                status = "PENDING",
                location = GeoPoint(46.517238, 6.629614),
                date = LocalDate.now(), time = LocalTime.of(10, 0),
                locationName = "Place de la Gare 1003 Lausanne"),
            Job(id = "5",
                title = "Paint Wall",
                description = "Paint the office wall",
                status = "CURRENT",
                location = GeoPoint(46.538917, 6.588126),
                date = LocalDate.now(), time = LocalTime.of(14, 0),
                locationName = "Place de la Gare 1020 Renens"),
            Job(id = "6",
                title = "HVAC Inspection",
                description = "Inspect HVAC",
                status = "HISTORY",
                location = GeoPoint(46.511244, 6.496200),
                date = LocalDate.now(), time = LocalTime.of(16, 0),
                locationName = "Place de la Gare 1110 Morges"))

      // Divide jobs by status into respective lists
      _pendingJobs.value = hardcodedJobs.filter { it.status == "PENDING" }.sortedWith(compareBy({ it.date }, { it.time }))
      _currentJobs.value = hardcodedJobs.filter { it.status == "CURRENT" }.sortedWith(compareBy({ it.date }, { it.time }))
      _historyJobs.value = hardcodedJobs.filter { it.status == "HISTORY" }.sortedWith(compareBy({ it.date }, { it.time }))
  }

  private fun loadJobs() {
      viewModelScope.launch {
          repository.getCurrentJobs()
              .catch { exception -> Log.e("JobDashboardViewModel", "Error loading current jobs", exception) }
              .collect { jobs -> _currentJobs.value = jobs }
          repository.getPendingJobs()
              .catch { exception -> Log.e("JobDashboardViewModel", "Error loading pending jobs", exception) }
              .collect { jobs -> _pendingJobs.value = jobs }
          repository.getHistoryJobs()
              .catch { exception -> Log.e("JobDashboardViewModel", "Error loading history jobs", exception) }
              .collect { jobs -> _historyJobs.value = jobs }
      }
  }

  fun getTodaySortedJobs(): List<Job> {
    val today = LocalDate.now()
    return _currentJobs.value
        .filter { it.date == today }
        .sortedBy { it.time }
  }

  // Function to confirm a pending job and move it to current
  fun confirmJob(job: Job) {
      _pendingJobs.value = _pendingJobs.value.filter { it.id != job.id }
      _currentJobs.value = _currentJobs.value + job.copy(status = "CURRENT")
  }

    // Function to mark a current job as completed or canceled, moving it to history
  fun completeJob(job: Job, isCanceled: Boolean = false) {
     _currentJobs.value = _currentJobs.value.filter { it.id != job.id }
     val historyStatus = if (isCanceled) "CANCELED" else "COMPLETED"
     _historyJobs.value = _historyJobs.value + job.copy(status = historyStatus)
  }

}
