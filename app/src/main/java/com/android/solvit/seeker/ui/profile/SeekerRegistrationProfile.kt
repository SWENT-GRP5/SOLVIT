package com.android.solvit.seeker.ui.profile

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.ui.authentication.CustomOutlinedTextField
import com.android.solvit.shared.ui.authentication.GoBackButton
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "SourceLockedOrientationActivity")
@Composable
fun SeekerRegistrationScreen(
    viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory),
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

  var fullName by remember { mutableStateOf("") }
  var userName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  val locationQuery by locationViewModel.query.collectAsState()

  // represent the current authentified user
  val user by authViewModel.user.collectAsState()
  // represent the email of the current user
  val email by authViewModel.email.collectAsState()

  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  var selectedLocation by remember { mutableStateOf<Location?>(null) }

  // Step tracking: Role, Details, Preferences
  var currentStep by remember { mutableIntStateOf(1) }

  val backgroundColor = colorScheme.background

  val isFullNameOk = fullName.isNotBlank() && fullName.length > 2
  val isUserNameOk = userName.isNotBlank() && userName.length > 2
  val isPhoneOk = phone.isNotBlank() && phone.all { it.isDigit() || it == '+' } && phone.length > 6
  val isLocationOK = selectedLocation != null
  val isFormComplete = isFullNameOk && isUserNameOk && isPhoneOk && isLocationOK

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Seeker Registration") },
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
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier =
                        Modifier.testTag("signUpIcon")
                            .size(150.dp)
                            .align(Alignment.CenterHorizontally))
                Text(
                    text = "Sign Up as a Seeker",
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier.testTag("signUpSeekerTitle").align(Alignment.CenterHorizontally))

                Spacer(modifier = Modifier.height(16.dp))

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
                    errorTestTag = "fullNameErrorSeekerRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                CustomOutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = "User Name",
                    placeholder = "Enter your user name",
                    isValueOk = isUserNameOk,
                    errorMessage = "Your user name must be at least 3 characters",
                    leadingIcon = Icons.Default.Person,
                    leadingIconDescription = "Person Icon",
                    testTag = "userNameInput",
                    errorTestTag = "userNameErrorSeekerRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                CustomOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    placeholder = "Enter your phone number",
                    isValueOk = isPhoneOk,
                    errorMessage = "Your phone number must be at least 7 digits",
                    leadingIcon = Icons.Default.Phone,
                    leadingIconDescription = "Phone Icon",
                    testTag = "phoneNumberInput",
                    errorTestTag = "phoneNumberErrorSeekerRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                LocationDropdown(
                    locationQuery = locationQuery,
                    onLocationQueryChange = { locationViewModel.setQuery(it) },
                    showDropdownLocation = showDropdown,
                    onShowDropdownLocationChange = { showDropdown = it },
                    locationSuggestions = locationSuggestions.filterNotNull(),
                    userLocations = user?.locations ?: emptyList(),
                    onLocationSelected = { selectedLocation = it },
                    requestLocation = null,
                    backgroundColor = colorScheme.background,
                    isValueOk = isLocationOK)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { currentStep = 2 },
                    modifier =
                        Modifier.fillMaxWidth().height(60.dp).testTag("completeRegistrationButton"),
                    enabled = isFormComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
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
                          style = MaterialTheme.typography.titleLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally)
                                  .testTag("preferencesTitle"),
                          textAlign = TextAlign.Center // Center the text
                          )
                      Spacer(modifier = Modifier.height(16.dp))
                      Image(
                          painter = painterResource(id = R.drawable.userpref),
                          contentDescription = "Completion Image",
                          modifier =
                              Modifier.size(300.dp)
                                  .align(Alignment.CenterHorizontally)
                                  .testTag("preferencesIllustration"))
                      Spacer(modifier = Modifier.height(20.dp))
                      Text(
                          text = "This feature is not implemented yet.",
                          style = MaterialTheme.typography.bodyLarge,
                          modifier = Modifier.align(Alignment.CenterHorizontally),
                          textAlign = TextAlign.Center,
                          color = colorScheme.primary)
                      Spacer(modifier = Modifier.height(100.dp))
                      Button(
                          onClick = { currentStep = 3 },
                          modifier = Modifier.fillMaxWidth().testTag("savePreferencesButton"),
                          colors = ButtonDefaults.buttonColors(colorScheme.secondary)) {
                            Text("Save Preferences", color = colorScheme.onSecondary)
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
                    painter = painterResource(id = R.drawable.alldoneuser), // Your image resource
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
                            "You're ready to explore the best services tailored to your needs. " +
                            "Start browsing through available services, connect with experts, and solve any challenge.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .testTag("successMessageText"), // Add horizontal padding
                    textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                      // Complete registration and navigate
                      val newUserProfile =
                          SeekerProfile(
                              uid = user!!.uid,
                              name = fullName,
                              username = userName,
                              phone = phone,
                              email = email)
                      viewModel.addUserProfile(newUserProfile)
                      authViewModel.registered()
                      // navigationActions.goBack() // Navigate after saving
                    },
                    modifier = Modifier.fillMaxWidth().testTag("exploreServicesButton"),
                    colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
                    ) {
                      Text("Continue to Explore Services", color = colorScheme.onSecondary)
                    }
              }
            }
      })
}

@Composable
fun Stepper(currentStep: Int, isFormComplete: Boolean) {
  val stepLabels = listOf("Information", "Details", "All Done")
  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.Top) {
        stepLabels.forEachIndexed { index, label ->
          StepCircle(
              stepNumber = index + 1,
              isCompleted = (index == 0 && isFormComplete) || currentStep > index + 1,
              label = label,
          )

          if (index < stepLabels.size - 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
}

@Composable
fun StepCircle(stepNumber: Int, isCompleted: Boolean, label: String) {
  val testTag = "stepCircle-$stepNumber-${if (isCompleted) "completed" else "incomplete"}"

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.width(100.dp).testTag(testTag)) {
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
                  style = MaterialTheme.typography.titleLarge)
            }

        // Display the label below the circle
        Text(
            text = label,
            color = colorScheme.onBackground, // You can customize this color
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp) // Add space between circle and label
            )
      }
}
