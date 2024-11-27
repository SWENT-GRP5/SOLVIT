package com.android.solvit.seeker.ui.request

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
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
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth

@Composable
fun CreateRequestScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    notificationViewModel: NotificationsViewModel= viewModel(factory = NotificationsViewModel.Factory),
    listProviderViewModel: ListProviderViewModel= viewModel(factory = ListProviderViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dueDate by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val locationQuery by locationViewModel.query.collectAsState()
  var showDropdownLocation by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf("") }
  val filteredServiceTypes =
      Services.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(Services.OTHER) }
  val localContext = LocalContext.current
  val userId = Firebase.auth.currentUser?.uid ?: "-1"

  RequestScreen(
      navigationActions = navigationActions,
      screenTitle = "Create a new request",
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
      selectedRequest = null,
      requestViewModel = requestViewModel,
      showDropdownLocation = showDropdownLocation,
      onShowDropdownLocationChange = { showDropdownLocation = it },
      locationSuggestions = locationSuggestions.filterNotNull(),
      onLocationSelected = { selectedLocation = it },
      selectedLocation = selectedLocation,
      dueDate = dueDate,
      onDueDateChange = { dueDate = it },
      selectedImageUri = selectedImageUri,
      imageUrl = null,
      onImageSelected = { uri ->
        selectedImageUri = uri
        uri?.let { selectedImageBitmap = loadBitmapFromUri(localContext, it) }
      },
      onSubmit = {
        val calendar = GregorianCalendar()
        val parts = dueDate.split("/")
        if (parts.size == 3) {
          try {
            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt(), 0, 0, 0)
            val serviceRequest =
                ServiceRequest(
                    title = title,
                    description = description,
                    userId = userId,
                    dueDate = Timestamp(calendar.time),
                    location = selectedLocation,
                    status = ServiceRequestStatus.PENDING,
                    uid = requestViewModel.getNewUid(),
                    type = selectedServiceType,
                    imageUrl = null)
            if (selectedImageUri != null) {
              requestViewModel.saveServiceRequestWithImage(serviceRequest, selectedImageUri!!)
            } else {
              requestViewModel.saveServiceRequest(serviceRequest)
            }
            notificationViewModel.sendNotifications(serviceRequest,listProviderViewModel.providersList.value)
            navigationActions.goBack()
            return@RequestScreen
          } catch (_: NumberFormatException) {}
        }
        Toast.makeText(localContext, "Invalid format, date must be DD/MM/YYYY.", Toast.LENGTH_SHORT)
            .show()
      },
      submitButtonText = "Submit Request")
}
