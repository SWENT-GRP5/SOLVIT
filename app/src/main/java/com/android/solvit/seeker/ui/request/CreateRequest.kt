package com.android.solvit.seeker.ui.request

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.icu.util.GregorianCalendar
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.request.analyzer.AIAssistantDialog
import com.android.solvit.shared.model.request.analyzer.MultiStepDialog
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.isInternetAvailable
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp

/**
 * A composable function that displays the screen for creating a new service request. It handles
 * input fields for title, description, location, service type, due date, and image selection, along
 * with the logic for showing AI assistant and multi-step dialog for service request analysis. This
 * function also handles form submission and navigation to the booking details page.
 *
 * @param navigationActions The navigation actions to navigate between screens.
 * @param requestViewModel The ViewModel instance for handling service request data.
 * @param locationViewModel The ViewModel instance for handling location data and suggestions.
 * @param authViewModel The authentication ViewModel instance to access user data.
 * @param notificationViewModel The ViewModel for managing notifications.
 * @param listProviderViewModel The ViewModel for handling the list of providers.
 */
@SuppressLint(
    "UnusedMaterialScaffoldPaddingParameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "SourceLockedOrientationActivity")
@Composable
fun CreateRequestScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    notificationViewModel: NotificationsViewModel =
        viewModel(factory = NotificationsViewModel.Factory),
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose {
      locationViewModel.clear()
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }

  val selectedProviderId = requestViewModel.selectedProviderId.collectAsStateWithLifecycle()
  val selectedProviderService =
      requestViewModel.selectedProviderService.collectAsStateWithLifecycle()

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dueDate by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val locationQuery by locationViewModel.query.collectAsStateWithLifecycle()
  var showDropdownLocation by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsStateWithLifecycle(
          initialValue = emptyList<Location?>())
  val user by authViewModel.user.collectAsStateWithLifecycle()
  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf("") }
  val filteredServiceTypes =
      Services.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(Services.OTHER) }
  selectedProviderService.value?.let { selectedServiceType = it }
  val localContext = LocalContext.current
  val userId = user?.uid ?: "-1"

  var showAIAssistantDialog by remember { mutableStateOf(true) }
  var showMultiStepDialog by remember { mutableStateOf(false) }
  var currentStep by remember { mutableIntStateOf(1) }
  var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

  // AI Assistant Dialog
  if (showAIAssistantDialog) {
    AIAssistantDialog(
        onCancel = { showAIAssistantDialog = false },
        onUploadPictures = {
          showAIAssistantDialog = false
          showMultiStepDialog = true
        })
  }

  // Multi-Step Dialog
  if (showMultiStepDialog) {
    MultiStepDialog(
        context = localContext,
        showDialog = showMultiStepDialog,
        currentStep = currentStep,
        selectedImages = selectedImages,
        onImagesSelected = { images ->
          selectedImages = images
          if (selectedImages.isNotEmpty()) {
            val uri = selectedImages[0]
            selectedImageUri = uri
          }
        },
        onRemoveImage = { uri ->
          selectedImages = selectedImages.filter { it != uri }
          if (selectedImages.isEmpty()) {
            selectedImageUri = null
          } else {
            val uri = selectedImages[0]
            selectedImageUri = uri
          }
        },
        onStartAnalyzing = {
          currentStep = 2 // Move to the analyzing step
        },
        onAnalyzeComplete = { generatedTitle, generatedType, generatedDescription ->
          title = generatedTitle
          typeQuery = generatedType
          selectedServiceType = Services.valueOf(generatedType)
          description = generatedDescription
          currentStep = 3 // Move to the analysis complete step
        },
        onClose = {
          showMultiStepDialog = false
          currentStep = 1 // Reset step for the next time
        })
  }

  RequestScreen(
      navigationActions = navigationActions,
      screenTitle = "Create a new request",
      title = title,
      onTitleChange = { title = it },
      description = description,
      onDescriptionChange = { description = it },
      typeQuery =
          if (selectedProviderService.value != null)
              Services.format(selectedProviderService.value!!)
          else typeQuery,
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
                    providerId = selectedProviderId.value,
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
              requestViewModel.saveServiceRequestWithImage(serviceRequest, selectedImageUri!!) {
                // Get the updated service request with the image URL
                requestViewModel.getServiceRequestById(serviceRequest.uid) { updatedRequest ->
                  requestViewModel.selectRequest(updatedRequest)
                  navigationActions.navigateAndSetBackStack(
                      Route.BOOKING_DETAILS, listOf(Route.REQUESTS_OVERVIEW))
                }
              }
            } else {
              requestViewModel.saveServiceRequest(serviceRequest)
              requestViewModel.getServiceRequestById(serviceRequest.uid) { request ->
                requestViewModel.selectRequest(request)
                navigationActions.navigateAndSetBackStack(
                    Route.BOOKING_DETAILS, listOf(Route.REQUESTS_OVERVIEW))
              }
            }
            requestViewModel.unSelectProvider()
            notificationViewModel.sendNotifications(
                serviceRequest, listProviderViewModel.providersList.value)
            return@RequestScreen
          } catch (_: NumberFormatException) {}
        }
        Toast.makeText(localContext, "Invalid format, date must be DD/MM/YYYY.", Toast.LENGTH_SHORT)
            .show()
      },
      submitButtonText = "Submit Request")
}
