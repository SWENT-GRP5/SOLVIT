package com.android.solvit.ui.requests

import android.icu.util.GregorianCalendar
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.model.map.Location
import com.android.solvit.model.map.LocationViewModel
import com.android.solvit.model.requests.ServiceRequest
import com.android.solvit.model.requests.ServiceRequestStatus
import com.android.solvit.model.requests.ServiceRequestType
import com.android.solvit.model.requests.ServiceRequestViewModel
import com.android.solvit.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.Calendar

@Composable
fun EditRequestScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {
  requestViewModel.selectRequest(
      ServiceRequest(
          title = "Bathtub leak",
          description = "I hit my bath too hard and now it's leaking",
          assigneeName = "assignee",
          dueDate = Timestamp(Calendar.getInstance().time),
          location =
              Location(
                  48.8588897,
                  2.3200410217200766,
                  "Paris, Île-de-France, France métropolitaine, France"),
          status = ServiceRequestStatus.PENDING,
          uid = "gIoUWJGkTgLHgA7qts59",
          type = ServiceRequestType.PLUMBING,
          imageUrl =
              "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"))

  val request = requestViewModel.selectedRequest.value ?: return
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
  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf(request.type.name) }
  val filteredServiceTypes =
      ServiceRequestType.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(request.type) }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  val imageUrl = request.imageUrl
  val localContext = LocalContext.current

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
      onLocationSelected = { selectedLocation = it },
      dueDate = dueDate,
      onDueDateChange = { dueDate = it },
      selectedImageUri = selectedImageUri,
      imageUrl = imageUrl,
      onImageSelected = { uri -> selectedImageUri = uri },
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
                    assigneeName = request.assigneeName,
                    dueDate = Timestamp(calendar.time),
                    location = selectedLocation,
                    status = request.status,
                    uid = request.uid,
                    type = selectedServiceType,
                    imageUrl = selectedImageUri.toString())
            if (selectedImageUri != null) {
              requestViewModel.saveServiceRequestWithImage(serviceRequest, selectedImageUri!!)
              navigationActions.goBack()
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
