package com.android.solvit.seeker.ui.provider

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProviderRegistrationScreen(
    viewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    navigationActions: NavigationActions,
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

  // Form fields
  var fullName by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val locationQuery by locationViewModel.query.collectAsState()

  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  // represent the current authenticated user
  val user by authViewModel.user.collectAsState()

  // Step tracking: Role, Details, Preferences
  var currentStep by remember { mutableStateOf(1) }
  val scrollState = rememberScrollState()
  val isFormComplete = fullName.isNotBlank() && phone.isNotBlank() && selectedLocation != null

  Scaffold(
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Back Button
                IconButton(
                    onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                      Icon(
                          Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back",
                          tint = colorScheme.onBackground)
                    }
                Spacer(modifier = Modifier.width(8.dp))

                Stepper(currentStep = currentStep, isFormComplete)
              }

          Spacer(modifier = Modifier.height(16.dp))

          if (currentStep == 1) {
            Image(
                painter = painterResource(id = R.drawable.app_logo), // Update with your logo
                contentDescription = "App Logo",
                modifier =
                    Modifier.testTag("signUpIcon")
                        .size(150.dp) // Adjust the size as per your logo
                        .align(Alignment.CenterHorizontally))
            Text(
                text = "Sign Up as a Professional",
                style = MaterialTheme.typography.h6,
                modifier =
                    Modifier.testTag("signUpProfessionalTitle").align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(16.dp))
            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name", color = colorScheme.onBackground) },
                placeholder = { Text("Enter your full name") },
                modifier = Modifier.fillMaxWidth().testTag("fullNameInput"),
                shape = RoundedCornerShape(12.dp),
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor =
                            colorScheme.secondary, // Green outline for focused state
                        unfocusedBorderColor =
                            colorScheme.onSurfaceVariant // Gray outline for unfocused state
                        ))

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number", color = colorScheme.onBackground) },
                placeholder = { Text("Enter your phone number") },
                modifier = Modifier.fillMaxWidth().testTag("phoneNumberInput"),
                shape = RoundedCornerShape(12.dp),
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor =
                            colorScheme.secondary, // Green outline for focused state
                        unfocusedBorderColor =
                            colorScheme.onSurfaceVariant // Gray outline for unfocused state
                        ))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Business/Company Name", color = colorScheme.onBackground) },
                placeholder = {
                  Text("Enter your business name (optional for independent providers) ")
                },
                modifier = Modifier.fillMaxWidth().testTag("companyNameInput"),
                shape = RoundedCornerShape(12.dp),
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor =
                            colorScheme.secondary, // Green outline for focused state
                        unfocusedBorderColor =
                            colorScheme.onSurfaceVariant // Gray outline for unfocused state
                        ))
            Spacer(modifier = Modifier.height(16.dp))

            LocationDropdown(
                locationQuery = locationQuery,
                onLocationQueryChange = { locationViewModel.setQuery(it) },
                showDropdownLocation = showDropdown,
                onShowDropdownLocationChange = { showDropdown = it },
                locationSuggestions = locationSuggestions.filterNotNull(),
                onLocationSelected = { selectedLocation = it },
                requestLocation = null,
                backgroundColor = Color.White)
            // Location Input
            ExposedDropdownMenuBox(
                expanded = showDropdown && locationSuggestions.isNotEmpty(),
                onExpandedChange = { showDropdown = it }) {
                  OutlinedTextField(
                      value = locationQuery,
                      onValueChange = {
                        locationViewModel.setQuery(it)
                        showDropdown = true // Show dropdown when user starts typing
                      },
                      label = { Text("Location", color = colorScheme.onBackground) },
                      placeholder = { Text("Enter an Address or Location") },
                      modifier = Modifier.menuAnchor().fillMaxWidth().testTag("locationInput"),
                      singleLine = true,
                      shape = RoundedCornerShape(12.dp),
                      colors =
                          TextFieldDefaults.outlinedTextFieldColors(
                              focusedBorderColor =
                                  colorScheme.secondary, // Green outline for focused state
                              unfocusedBorderColor =
                                  colorScheme.onSurfaceVariant // Gray outline for unfocused state
                              ))

                  // Dropdown menu for location suggestions
                  ExposedDropdownMenu(
                      expanded = showDropdown && locationSuggestions.isNotEmpty(),
                      onDismissRequest = { showDropdown = false }) {
                        locationSuggestions.filterNotNull().take(3).forEach { location ->
                          DropdownMenuItem(
                              text = {
                                Text(
                                    text =
                                        location.name.take(30) +
                                            if (location.name.length > 30) "..." else "",
                                    maxLines = 1)
                              },
                              onClick = {
                                locationViewModel.setQuery(location.name)
                                selectedLocation =
                                    location // Set selectedLocation as non-null Location
                                showDropdown = false // Close dropdown on selection
                              },
                              modifier = Modifier.padding(8.dp).testTag("locationResult"))
                        }
                      }
                }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                  // Move to next step (Step 2: Preferences)
                  currentStep = 2
                },
                modifier =
                    Modifier.fillMaxWidth().height(60.dp).testTag("completeRegistrationButton"),
                enabled = isFormComplete,
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = colorScheme.secondary) // Green button
                ) {
                  Text("Complete registration", color = colorScheme.onSecondary)
                }
          }
          // Preferences Step
          if (currentStep == 2) {
            Column(
                modifier =
                    Modifier.fillMaxWidth() // Ensure content takes up full width
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
                verticalArrangement = Arrangement.Center // Center vertically
                ) {
                  Text(
                      text = "Set Your Preferences",
                      style = MaterialTheme.typography.h6,
                      modifier =
                          Modifier.align(Alignment.CenterHorizontally).testTag("preferencesTitle"),
                      textAlign = TextAlign.Center // Center the text
                      )
                  Spacer(modifier = Modifier.height(16.dp))
                  Image(
                      painter = painterResource(id = R.drawable.providerpref),
                      contentDescription = "Completion Image",
                      modifier =
                          Modifier.size(300.dp)
                              .testTag("preferencesIllustration")
                              .align(Alignment.CenterHorizontally))
                  Spacer(modifier = Modifier.height(20.dp))
                  Text(
                      text = "This feature is not implemented yet.",
                      style = MaterialTheme.typography.body1,
                      modifier = Modifier.align(Alignment.CenterHorizontally),
                      textAlign = TextAlign.Center,
                      color = colorScheme.primary)
                  Spacer(modifier = Modifier.height(100.dp))
                  Button(
                      onClick = { currentStep = 3 },
                      modifier = Modifier.fillMaxWidth().testTag("savePreferencesButton"),
                      colors =
                          ButtonDefaults.buttonColors(backgroundColor = colorScheme.secondary)) {
                        Text("Save Preferences", color = colorScheme.onSecondary)
                      }
                  Text(
                      text = "You can always update your preferences in your profile settings.",
                      style = MaterialTheme.typography.body1,
                      modifier = Modifier.align(Alignment.CenterHorizontally).testTag("footerText"),
                      textAlign = TextAlign.Center)
                }
          }

          // Completion Step
          if (currentStep == 3) {
            // Completion screen
            Text(
                text = "You're All Set!",
                style = MaterialTheme.typography.h6,
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag("confirmationTitle"))

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.alldoneprovider), // Your image resource
                contentDescription = "Completion Image",
                modifier =
                    Modifier.size(200.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag("celebrationIllustration") // Adjust size as needed
                )
            Spacer(modifier = Modifier.height(100.dp))
            // Completion message
            Text(
                text =
                    "Your profile has been successfully created. " +
                        "You're ready to start offering your services to customers." +
                        "Start connecting with customers, respond to requests, and grow your business on Solvit.",
                style = MaterialTheme.typography.body1,
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .testTag("successMessageText"), // Add horizontal padding
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                  // Complete registration and navigate
                  val loc = selectedLocation ?: Location(0.0, 0.0, "")
                  val newProviderProfile =
                      Provider(
                          uid = user!!.uid,
                          name = fullName,
                          phone = phone,
                          companyName = companyName,
                          location = loc)
                  viewModel.addProvider(newProviderProfile)
                  authViewModel.registered()
                  // navigationActions.goBack() // Navigate after saving
                },
                modifier = Modifier.fillMaxWidth().testTag("continueDashboardButton"),
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = colorScheme.secondary) // Green button
                ) {
                  Text("Continue to My Dashboard", color = colorScheme.onSecondary)
                }
          }
        }
      })
}

@Composable
fun Stepper(currentStep: Int, isFormComplete: Boolean) {
  // Stepper for 3 steps with circles and check icons
  val stepLabels = listOf("Information", "Details", "All Done")
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        stepLabels.forEachIndexed { index, label ->
          StepCircle(
              stepNumber = index + 1,
              isCompleted =
                  (index == 0 && isFormComplete) ||
                      currentStep > index + 1, // Change to reflect form completion
              label = label)
          if (index < stepLabels.size - 1) {
            Spacer(modifier = Modifier.width(8.dp)) // Spacer between circles
          }
        }
      }
}

@Composable
fun StepCircle(stepNumber: Int, isCompleted: Boolean, label: String) {
  // Use a Column to stack the circle and the label vertically
  val testTag = "stepCircle-$stepNumber-${if (isCompleted) "completed" else "incomplete"}"
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.width(80.dp).testTag(testTag) // Set a width for alignment
      ) {
        Box(
            modifier =
                Modifier.size(40.dp)
                    .background(
                        if (isCompleted) colorScheme.secondary else colorScheme.onSurfaceVariant,
                        shape = CircleShape),
            contentAlignment = Alignment.Center) {
              Text(
                  text = if (isCompleted) "✔" else stepNumber.toString(),
                  color = colorScheme.onSecondary,
                  style = MaterialTheme.typography.h6)
            }

        // Display the label below the circle
        Text(
            text = label,
            color = colorScheme.onBackground,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 4.dp))
      }
}
