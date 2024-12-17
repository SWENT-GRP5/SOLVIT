package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.provider.ui.profile.UploadImage
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.CustomOutlinedTextField
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import com.android.solvit.shared.ui.utils.ValidationRegex

/**
 * A composable function that provides a multi-step screen for registering a seeker profile. The
 * registration process includes steps for inputting personal information, setting preferences, and
 * confirming the registration.
 *
 * @param viewModel The `SeekerProfileViewModel` to manage the seeker profile data.
 * @param navigationActions A set of navigation actions to handle transitions between screens.
 * @param locationViewModel The `LocationViewModel` to fetch and manage location suggestions.
 * @param authViewModel The `AuthViewModel` to manage authentication and user-related data.
 */
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
    onDispose {
      locationViewModel.clear()
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }

  var fullName by remember { mutableStateOf("") }
  var userName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  val locationQuery by locationViewModel.query.collectAsState()
  var seekerImageUri by remember { mutableStateOf<Uri?>(null) }
  var seekerImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

  // Represents the current authenticated user
  val user by authViewModel.user.collectAsState()
  // Represents the email of the current user
  val email by authViewModel.email.collectAsState()

  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  var selectedLocation by remember { mutableStateOf<Location?>(null) }

  // Step tracking: Role, Details, Preferences
  var currentStep by remember { mutableIntStateOf(1) }

  val backgroundColor = colorScheme.background

  val isFullNameOk = ValidationRegex.FULL_NAME_REGEX.matches(fullName)
  val isUserNameOk = userName.isNotBlank() && userName.length > 2
  val isPhoneOk = ValidationRegex.PHONE_REGEX.matches(phone)
  val isLocationOK = selectedLocation != null
  val isFormComplete = isFullNameOk && isUserNameOk && isPhoneOk && isLocationOK

  Scaffold(
      topBar = {
        TopAppBarInbox(
            "Seeker Registration",
            leftButtonAction =
                if (currentStep > 1) {
                  { currentStep -= 1 }
                } else {
                  { navigationActions.goBack() }
                },
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack)
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
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally))
                Text(
                    text = "Sign Up as a Seeker",
                    style = Typography.titleLarge,
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
                    errorMessage = "Enter a valid first and last name",
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
                    errorMessage = "Your phone number must be at least 6 digits",
                    leadingIcon = Icons.Default.Phone,
                    leadingIconDescription = "Phone Icon",
                    testTag = "phoneNumberInput",
                    errorTestTag = "phoneNumberErrorSeekerRegistration",
                    keyboardType = KeyboardType.Number)

                Spacer(modifier = Modifier.height(10.dp))

                LocationDropdown(
                    locationQuery = locationQuery,
                    onLocationQueryChange = { locationViewModel.setQuery(it) },
                    showDropdownLocation = showDropdown,
                    onShowDropdownLocationChange = { showDropdown = it },
                    locationSuggestions = locationSuggestions.filterNotNull(),
                    userLocations = user?.locations ?: emptyList(),
                    onLocationSelected = {
                      selectedLocation = it
                      authViewModel.addUserLocation(it, {}, {})
                    },
                    requestLocation = null,
                    backgroundColor = colorScheme.background,
                    isValueOk = isLocationOK)

                Spacer(modifier = Modifier.height(30.dp))

                UploadImage(seekerImageUri, null) { uri: Uri? ->
                  seekerImageUri = uri
                  uri?.let { seekerImageBitmap = loadBitmapFromUri(context, it) }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                      if (isFormComplete) {
                        currentStep = 2
                      } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth().height(60.dp).testTag("completeRegistrationButton"),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        if (isFormComplete) buttonColors(colorScheme.secondary)
                        else buttonColors(colorScheme.onSurfaceVariant)) {
                      Text(
                          "Complete registration",
                          color = colorScheme.onSecondary,
                          style = Typography.bodyLarge)
                    }
              }
              // Preferences Step
              if (currentStep == 2) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth() // Ensure content takes up full width
                            .padding(16.dp)) {
                      Text(
                          text = "Set Your Preferences",
                          style = Typography.titleLarge,
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
                              Modifier.size(100.dp)
                                  .align(Alignment.CenterHorizontally)
                                  .testTag("preferencesIllustration"))
                      Spacer(modifier = Modifier.height(20.dp))
                      val userId = user?.uid
                      if (userId != null) {
                        Preferences(userId, viewModel)
                      } else {
                        Text(
                            text = "User not authenticated. Please sign in again.",
                            color = colorScheme.error,
                            style = Typography.bodyMedium,
                            modifier =
                                Modifier.align(Alignment.CenterHorizontally)
                                    .testTag("userNotAuthenticatedError"))
                      }
                      Spacer(modifier = Modifier.height(20.dp))
                      Button(
                          onClick = { currentStep = 3 },
                          modifier = Modifier.fillMaxWidth().testTag("savePreferencesButton"),
                          colors = buttonColors(colorScheme.secondary)) {
                            Text("Save Preferences", color = colorScheme.onSecondary)
                          }
                      Text(
                          text = "You can always update your preferences in your profile settings.",
                          style = Typography.bodyLarge,
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
                    style = Typography.titleLarge,
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
                    style = Typography.bodyLarge,
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
                      viewModel.addUserProfile(newUserProfile, seekerImageUri)
                      authViewModel.setUserName(userName)
                      authViewModel.registered()
                      authViewModel.completeRegistration(
                          {
                            Toast.makeText(
                                    context,
                                    "Registration Successfully Completed",
                                    Toast.LENGTH_SHORT)
                                .show()
                          },
                          {
                            Toast.makeText(
                                    context, "Failed to complete registration", Toast.LENGTH_SHORT)
                                .show()
                          })
                      // navigationActions.goBack() // Navigate after saving
                    },
                    modifier = Modifier.fillMaxWidth().testTag("exploreServicesButton"),
                    colors = buttonColors(colorScheme.secondary) // Green button
                    ) {
                      Text("Continue to Explore Services", color = colorScheme.onSecondary)
                    }
              }
            }
      })
}

/**
 * A composable function that displays a step indicator for a multi-step registration process.
 *
 * @param currentStep The current step in the registration process.
 * @param isFormComplete A boolean indicating whether the current step's form is complete.
 */
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

/**
 * A composable function that displays a visual indicator for a single step in a stepper component.
 *
 * @param stepNumber The number of the step being represented.
 * @param isCompleted A boolean indicating whether the step is completed.
 * @param label A text label describing the step.
 */
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
                  style = Typography.titleLarge)
            }

        // Display the label below the circle
        Text(
            text = label,
            color = colorScheme.onBackground,
            style = Typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp) // Add space between circle and label
            )
      }
}
