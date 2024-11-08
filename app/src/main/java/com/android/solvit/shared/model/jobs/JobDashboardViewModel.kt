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

  private val _currentJobs = MutableStateFlow<List<Job>>(emptyList())
  val currentJobs: StateFlow<List<Job>> = _currentJobs

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
                locationName = "Place de la Gare 1110 Morges"))

    // Update the StateFlow with the hardcoded jobs
    _currentJobs.update { hardcodedJobs }
  }

  private fun loadJobs() {
    viewModelScope.launch {
      repository
          .getCurrentJobs()
          .catch { exception ->
            // Handle error, e.g., log it or update an error state
            Log.e("JobDashboardViewModel", "Error loading jobs", exception)
          }
          .collect { jobs -> _currentJobs.update { jobs } }
    }
  }

  fun getTodaySortedJobs(): List<Job> {
    val today = LocalDate.now()
    return _currentJobs.value
        .filter { it.date == today }
        .sortedWith(compareBy({ it.date }, { it.time }))
  }
}
