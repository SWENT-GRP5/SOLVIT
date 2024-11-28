package com.android.solvit.seeker.ui.request

import analyzeImagesWithOpenAI
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.icu.util.GregorianCalendar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.isInternetAvailable
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope


@Composable
fun CreateRequestScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
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
  val user by authViewModel.user.collectAsState()
  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf("") }
  val filteredServiceTypes =
      Services.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(Services.OTHER) }
  val localContext = LocalContext.current
  val userId = Firebase.auth.currentUser?.uid ?: "-1"

    var showAIAssistantDialog by remember { mutableStateOf(true) }
    var showMultiStepDialog by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisComplete by remember { mutableStateOf(false) }
    var imageUrls by remember { mutableStateOf<List<String>?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var analysisError by remember { mutableStateOf<String?>(null) }
    var analysisResult by remember { mutableStateOf<Triple<String, String, String>?>(null) }



    // AI Assistant Dialog
    if (showAIAssistantDialog) {
        AIAssistantDialog(
            onCancel = { showAIAssistantDialog = false },
            onUploadPictures = {
                showAIAssistantDialog = false
                showMultiStepDialog  = true
            }
        )
    }

    // Multi-Step Dialog
    if (showMultiStepDialog) {
        MultiStepDialog(
            requestViewModel = requestViewModel,
            showDialog = showMultiStepDialog,
            currentStep = currentStep,
            selectedImages = selectedImages,
            onImagesSelected = { images ->
                selectedImages = images
            },
            onRemoveImage = { uri ->
                selectedImages = selectedImages.filter { it != uri }
            },
            onStartAnalyzing = {
                currentStep = 2 // Move to the analyzing step
            },
            onAnalyzeComplete = { generatedType, generatedTitle, generatedDescription ->
                typeQuery = generatedType
                title = generatedTitle
                description = generatedDescription
                currentStep = 3 // Move to the analysis complete step
            },
            onClose = {
                showMultiStepDialog = false
                currentStep = 1 // Reset step for the next time
            }
        )
    }


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
      userLocations = user?.locations ?: emptyList(),
      onLocationSelected = {
        selectedLocation = it
        authViewModel.addUserLocation(it, {}, {})
      },
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
              if (!isInternetAvailable(localContext)) {
                Toast.makeText(
                        localContext,
                        "Image will show when you are back online",
                        Toast.LENGTH_SHORT)
                    .show()
              }
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
      submitButtonText = "Submit Request")
}
