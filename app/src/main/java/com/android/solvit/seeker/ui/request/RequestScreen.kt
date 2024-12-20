package com.android.solvit.seeker.ui.request

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.solvit.R
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox

/**
 * A composable screen for creating or editing a service request. The screen allows users to input
 * the title, description, service type, location, and due date for a service request. Users can
 * also select an image and submit the request. If editing an existing request, users can delete the
 * request.
 *
 * This screen locks the orientation to portrait and provides a background image that changes based
 * on the system theme (light or dark).
 *
 * @param navigationActions The navigation actions used to handle screen navigation, including going
 *   back to the previous screen.
 * @param screenTitle The title displayed in the top app bar.
 * @param title The current title of the service request.
 * @param onTitleChange A lambda function called when the title changes.
 * @param description The current description of the service request.
 * @param onDescriptionChange A lambda function called when the description changes.
 * @param typeQuery The query for filtering service types.
 * @param onTypeQueryChange A lambda function called when the service type query changes.
 * @param showDropdownType A flag that determines whether the dropdown for service types is shown.
 * @param onShowDropdownTypeChange A lambda function called when the visibility of the service type
 *   dropdown changes.
 * @param filteredServiceTypes A list of filtered service types based on the query.
 * @param onServiceTypeSelected A lambda function called when a service type is selected.
 * @param locationQuery The query for filtering locations.
 * @param onLocationQueryChange A lambda function called when the location query changes.
 * @param showDropdownLocation A flag that determines whether the dropdown for locations is shown.
 * @param onShowDropdownLocationChange A lambda function called when the visibility of the location
 *   dropdown changes.
 * @param locationSuggestions A list of location suggestions based on the query.
 * @param userLocations A list of locations associated with the user.
 * @param onLocationSelected A lambda function called when a location is selected.
 * @param selectedLocation The currently selected location.
 * @param selectedRequest The existing service request being edited, if any.
 * @param requestViewModel The view model responsible for handling service request operations.
 * @param dueDate The current due date for the service request.
 * @param onDueDateChange A lambda function called when the due date changes.
 * @param selectedImageUri The URI of the selected image for the service request.
 * @param imageUrl The URL of the image if it exists for the service request.
 * @param onImageSelected A lambda function called when an image is selected or deselected.
 * @param onSubmit A lambda function called when the submit button is clicked.
 * @param submitButtonText The text displayed on the submit button.
 */
@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(
    navigationActions: NavigationActions,
    screenTitle: String,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    typeQuery: String,
    onTypeQueryChange: (String) -> Unit,
    showDropdownType: Boolean,
    onShowDropdownTypeChange: (Boolean) -> Unit,
    filteredServiceTypes: List<Services>,
    onServiceTypeSelected: (Services) -> Unit,
    locationQuery: String,
    onLocationQueryChange: (String) -> Unit,
    showDropdownLocation: Boolean,
    onShowDropdownLocationChange: (Boolean) -> Unit,
    locationSuggestions: List<Location>,
    userLocations: List<Location>,
    onLocationSelected: (Location) -> Unit,
    selectedLocation: Location?,
    selectedRequest: ServiceRequest?,
    requestViewModel: ServiceRequestViewModel,
    dueDate: String,
    onDueDateChange: (String) -> Unit,
    selectedImageUri: Uri?,
    imageUrl: String?,
    onImageSelected: (Uri?) -> Unit,
    onSubmit: () -> Unit,
    submitButtonText: String
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  Box(modifier = Modifier.fillMaxSize()) {
    // Background Image
    Image(
        painter =
            painterResource(
                id =
                    if (isSystemInDarkTheme()) R.drawable.bg_request_dark
                    else R.drawable.bg_request),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().testTag("requestBackground"))

    Scaffold(
        modifier = Modifier.padding(16.dp).testTag("requestScreen"),
        containerColor = Color.Transparent,
        topBar = {
          TopAppBarInbox(
              title = screenTitle,
              testTagTitle = "screenTitle",
              leftButtonAction = {
                navigationActions.goBack()
                requestViewModel.unSelectProvider()
              },
              leftButtonForm = Icons.AutoMirrored.Outlined.ArrowBack,
              testTagLeft = "goBackButton",
              containerColor = Color.Transparent)
        },
        content = { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(10.dp)
                      .padding(paddingValues)
                      .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                ImagePicker(selectedImageUri, imageUrl, onImageSelected)
                TitleInput(title, onTitleChange)
                DescriptionInput(description, onDescriptionChange)
                ServiceTypeDropdown(
                    typeQuery,
                    onTypeQueryChange,
                    showDropdownType,
                    onShowDropdownTypeChange,
                    filteredServiceTypes,
                    onServiceTypeSelected)
                LocationDropdown(
                    locationQuery = locationQuery,
                    onLocationQueryChange = onLocationQueryChange,
                    showDropdownLocation = showDropdownLocation,
                    onShowDropdownLocationChange = onShowDropdownLocationChange,
                    locationSuggestions = locationSuggestions,
                    userLocations = userLocations,
                    onLocationSelected = onLocationSelected,
                    requestLocation = selectedRequest?.location,
                    isValueOk = selectedLocation != null)
                DatePickerFieldToModal(dueDate = dueDate, onDateChange = onDueDateChange)
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.height(50.dp).testTag("requestSubmit"),
                    shape = RoundedCornerShape(25.dp),
                    enabled =
                        title.isNotBlank() &&
                            description.isNotBlank() &&
                            dueDate.isNotBlank() &&
                            selectedLocation != null,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            disabledContainerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimary,
                            disabledContentColor = colorScheme.onPrimaryContainer)) {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Center,
                          modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(submitButtonText, style = Typography.bodyLarge)
                          }
                    }
                if (selectedRequest != null) {
                  DeleteButton(
                      request = selectedRequest,
                      requestViewModel = requestViewModel,
                      navigationActions)
                }
              }
        })
  }
}
