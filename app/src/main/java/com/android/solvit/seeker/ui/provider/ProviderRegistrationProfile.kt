package com.android.solvit.seeker.ui.provider

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.profile.Stepper
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.ui.authentication.CustomOutlinedTextField
import com.android.solvit.shared.ui.authentication.GoBackButton
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint(
    "UnusedMaterialScaffoldPaddingParameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "SourceLockedOrientationActivity")
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
  var currentStep by remember { mutableIntStateOf(1) }

  val backgroundColor = colorScheme.background

  val isFullNameOk = fullName.isNotBlank() && fullName.length > 2
  val isPhoneOk = phone.isNotBlank() && phone.all { it.isDigit() || it == '+' } && phone.length > 6
  val isCompanyNameOk = companyName.isNotBlank() && companyName.length > 2
  val isLocationOK = selectedLocation != null

  val isFormComplete = isFullNameOk && isPhoneOk && isCompanyNameOk && isLocationOK

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Provider Registration") },
            navigationIcon = {
              if (currentStep > 1) {
                IconButton(onClick = { currentStep -= 1 }) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
              } else {
                GoBackButton(navigationActions)
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.background(backgroundColor)
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())) {
              Stepper(currentStep = currentStep, isFormComplete)
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
                    text = "Sign Up as a Provider",
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier.testTag("signUpProviderTitle").align(Alignment.CenterHorizontally))

                Spacer(modifier = Modifier.height(16.dp))
                // Full Name
                CustomOutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    isValueOk = isFullNameOk,
                    errorMessage = "Your full name must be at least 3 characters",
                    leadingIcon = Icons.Default.Person,
                    leadingIconDescription = "Person Icon",
                    testTag = "fullNameInput",
                    errorTestTag = "fullNameErrorProviderRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Number
                CustomOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    placeholder = "Enter your phone number",
                    isValueOk = isPhoneOk,
                    errorMessage = "Your phone number must be at least 6 digits",
                    leadingIcon = Icons.Default.Phone,
                    leadingIconDescription = "Phone Icon",
                    testTag = "phoneNumberInput",
                    errorTestTag = "phoneNumberErrorProviderRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                CustomOutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = "Business/Company Name",
                    placeholder = "Enter your business name (optional for independent providers)",
                    isValueOk = isFullNameOk,
                    errorMessage = "Your company name must be at least 3 characters",
                    leadingIcon = Icons.Default.Build,
                    leadingIconDescription = "Company Icon",
                    testTag = "companyNameInput",
                    errorTestTag = "companyNameErrorProviderRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                LocationDropdown(
                    locationQuery = locationQuery,
                    onLocationQueryChange = { locationViewModel.setQuery(it) },
                    showDropdownLocation = showDropdown,
                    onShowDropdownLocationChange = { showDropdown = it },
                    locationSuggestions = locationSuggestions.filterNotNull(),
                    onLocationSelected = { selectedLocation = it },
                    requestLocation = null,
                    backgroundColor = colorScheme.background,
                    isValueOk = isLocationOK)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                      // Move to next step (Step 2: Preferences)
                      currentStep = 2
                    },
                    modifier =
                        Modifier.fillMaxWidth().height(60.dp).testTag("completeRegistrationButton"),
                    enabled = isFormComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
                    ) {
                      Text("Complete registration", color = Color.White)
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
                          style = MaterialTheme.typography.titleLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally)
                                  .testTag("preferencesTitle"),
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
                          style = MaterialTheme.typography.bodyLarge,
                          modifier = Modifier.align(Alignment.CenterHorizontally),
                          textAlign = TextAlign.Center,
                          color = Color.Blue)
                      Spacer(modifier = Modifier.height(100.dp))
                      Button(
                          onClick = { currentStep = 3 },
                          modifier = Modifier.fillMaxWidth().testTag("savePreferencesButton"),
                          colors = ButtonDefaults.buttonColors(Color(0xFF28A745))) {
                            Text("Save Preferences", color = Color.White)
                          }
                      Text(
                          text = "You can always update your preferences in your profile settings.",
                          style = MaterialTheme.typography.bodyLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally).testTag("footerText"),
                          textAlign = TextAlign.Center)
                    }
              }

              // Completion Step
              if (currentStep == 3) {
                // Completion screen
                Text(
                    text = "You're All Set!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally).testTag("confirmationTitle"))

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter =
                        painterResource(id = R.drawable.alldoneprovider), // Your image resource
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
                    style = MaterialTheme.typography.bodyLarge,
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
                    colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
                    ) {
                      Text("Continue to My Dashboard", color = colorScheme.onSecondary)
                    }
              }
            }
      })
}
