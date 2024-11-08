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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.authentication.GoBackButton
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SeekerRegistrationScreen(
    viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory),
    navigationActions: NavigationActions,
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
  // var username by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var userName by remember { mutableStateOf("") }
  // represent the current authentified user
  val user by authViewModel.user.collectAsState()
  // represent the email of the current user
  val email by authViewModel.email.collectAsState()

  // Step tracking: Role, Details, Preferences
  var currentStep by remember { mutableIntStateOf(1) }
  val backgroundColor = Color(0xFFFFFFFF)
  val isFormComplete = fullName.isNotBlank() && phone.isNotBlank() && address.isNotBlank()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Stepper(currentStep = currentStep, isFormComplete) },
            navigationIcon = {
              if (currentStep > 1) {
                IconButton(onClick = { currentStep -= 1 }) {
                  Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                Modifier.padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())) {
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
                    text = "Sign Up as a Customer",
                    style = MaterialTheme.typography.h6,
                    modifier =
                        Modifier.testTag("signUpCustomerTitle").align(Alignment.CenterHorizontally))

                Spacer(modifier = Modifier.height(16.dp))
                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", color = Color.Black) },
                    placeholder = { Text("Enter your full name") },
                    modifier = Modifier.fillMaxWidth().testTag("fullNameInput"),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor =
                                Color(0xFF00C853), // Green outline for focused state
                            unfocusedBorderColor = Color.Gray // Gray outline for unfocused state
                            ))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("User Name", color = Color.Black) },
                    placeholder = { Text("Enter your user name") },
                    modifier = Modifier.fillMaxWidth().testTag("userNameInput"),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor =
                                Color(0xFF00C853), // Green outline for focused state
                            unfocusedBorderColor = Color.Gray // Gray outline for unfocused state
                            ))

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = Color.Black) },
                    placeholder = { Text("Enter your phone number") },
                    modifier = Modifier.fillMaxWidth().testTag("phoneNumberInput"),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor =
                                Color(0xFF00C853), // Green outline for focused state
                            unfocusedBorderColor = Color.Gray // Gray outline for unfocused state
                            ))
                Spacer(modifier = Modifier.height(16.dp))
                // Location
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Location", color = Color.Black) },
                    placeholder = { Text("Enter your location or business location") },
                    modifier = Modifier.fillMaxWidth().testTag("locationInput"),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color.Gray))
                Spacer(modifier = Modifier.height(16.dp))
                // Password Field

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { currentStep = 2 },
                    modifier =
                        Modifier.fillMaxWidth().height(60.dp).testTag("completeRegistrationButton"),
                    enabled = isFormComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF28A745)) // Green button
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
                          style = MaterialTheme.typography.h6,
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
                          style = MaterialTheme.typography.body1,
                          modifier = Modifier.align(Alignment.CenterHorizontally),
                          textAlign = TextAlign.Center,
                          color = Color.Blue)
                      Spacer(modifier = Modifier.height(100.dp))
                      Button(
                          onClick = { currentStep = 3 },
                          modifier = Modifier.fillMaxWidth().testTag("savePreferencesButton"),
                          colors =
                              ButtonDefaults.buttonColors(backgroundColor = Color(0xFF28A745))) {
                            Text("Save Preferences", color = Color.White)
                          }
                      Text(
                          text = "You can always update your preferences in your profile settings.",
                          style = MaterialTheme.typography.body1,
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
                    style = MaterialTheme.typography.h6,
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
                    style = MaterialTheme.typography.body1,
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
                              address = address,
                              email = email)
                      viewModel.addUserProfile(newUserProfile)
                      authViewModel.registered()
                      // navigationActions.goBack() // Navigate after saving
                    },
                    modifier = Modifier.fillMaxWidth().testTag("exploreServicesButton"),
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF28A745)) // Green button
                    ) {
                      Text("Continue to Explore Services", color = Color.White)
                    }
              }
            }
      })
}

@Composable
fun Stepper(currentStep: Int, isFormComplete: Boolean) {
  val stepLabels = listOf("Information", "Details", "All Done")

  Row(
      modifier = Modifier.fillMaxWidth().height(560.dp).padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        stepLabels.forEachIndexed { index, label ->
          StepCircle(
              stepNumber = index + 1,
              isCompleted = (index == 0 && isFormComplete) || currentStep > index + 1,
              label = label,
          )

          if (index < stepLabels.size - 1) {
            Spacer(modifier = Modifier.width(8.dp))
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
                        if (isCompleted) Color(0xFF28A745) else Color.Gray, shape = CircleShape),
            contentAlignment = Alignment.Center) {
              Text(
                  text = if (isCompleted) "✔" else stepNumber.toString(),
                  color = Color.White,
                  style = MaterialTheme.typography.h6)
            }

        // Display the label below the circle
        Text(
            text = label,
            color = Color.Black, // You can customize this color
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 4.dp) // Add space between circle and label
            )
      }
}
