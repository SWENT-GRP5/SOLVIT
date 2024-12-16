package com.android.solvit.seeker.ui.request

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.icu.util.GregorianCalendar
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.Calendar

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun EditRequestScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val localContext = LocalContext.current
  DisposableEffect(Unit) {
    val activity = localContext as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose {
      locationViewModel.clear()
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }
  val request = requestViewModel.selectedRequest.collectAsState().value ?: return
  var title by remember { mutableStateOf(request.title) }
  var description by remember { mutableStateOf(request.description) }
  var dueDate by remember {
    mutableStateOf(
        request.dueDate.let {
          val calendar = GregorianCalendar()
          calendar.time = request.dueDate.toDate()
          return@let "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                    calendar.get(
                        Calendar.YEAR
                    )
                }"
        })
  }
  var selectedLocation by remember { mutableStateOf(request.location) }
  val locationQuery by locationViewModel.query.collectAsState()
  var showDropdownLocation by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  val user by authViewModel.user.collectAsState()
  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf(request.type.name) }
  val filteredServiceTypes =
      Services.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(request.type) }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  val imageUrl = request.imageUrl

  RequestScreen(
      navigationActions = navigationActions,
      screenTitle = "Edit your request",
      title = title,
      onTitleChange = { title = it },
      description = description,
      onDescriptionChange = { description = it },
      typeQuery = typeQuery,
      onTypeQueryChange = { typeQuery = it },
      showDropdownType = showDropdownType,
      onShowDropdownTypeChange = { showDropdownType = it },
      filteredServiceTypes = filteredServiceTypes,
      onServiceTypeSelected = {
        typeQuery = it.name
        selectedServiceType = it
      },
      locationQuery = locationQuery,
      onLocationQueryChange = { locationViewModel.setQuery(it) },
      selectedRequest = request,
      requestViewModel = requestViewModel,
      showDropdownLocation = showDropdownLocation,
      onShowDropdownLocationChange = { showDropdownLocation = it },
      locationSuggestions = locationSuggestions.filterNotNull(),
      userLocations = user?.locations ?: emptyList(),
      onLocationSelected = { selectedLocation = it },
      selectedLocation = selectedLocation,
      dueDate = dueDate,
      onDueDateChange = { dueDate = it },
      selectedImageUri = selectedImageUri,
      imageUrl = imageUrl,
      onImageSelected = { uri -> if (uri != null) selectedImageUri = uri },
      onSubmit = {
        val calendar = GregorianCalendar()
        val parts = dueDate.split("/")
        if (parts.size == 3) {
          try {
            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt(), 0, 0, 0)
            val serviceRequest =
                request.copy(
                    title = title,
                    description = description,
                    dueDate = Timestamp(calendar.time),
                    location = selectedLocation,
                    type = selectedServiceType,
                    imageUrl = imageUrl)
            if (selectedImageUri != null) {
              requestViewModel.saveServiceRequestWithImage(serviceRequest, selectedImageUri!!) {
                // Get the updated service request with the image URL
                requestViewModel.getServiceRequestById(serviceRequest.uid) { updatedRequest ->
                  requestViewModel.selectRequest(updatedRequest)
                  navigationActions.goBack()
                }
              }
            } else {
              requestViewModel.saveServiceRequest(serviceRequest)
              navigationActions.goBack()
            }
            return@RequestScreen
          } catch (_: NumberFormatException) {}
        }
        Toast.makeText(localContext, "Invalid format, date must be DD/MM/YYYY.", Toast.LENGTH_SHORT)
            .show()
      },
      submitButtonText = "Save Edits")
}
