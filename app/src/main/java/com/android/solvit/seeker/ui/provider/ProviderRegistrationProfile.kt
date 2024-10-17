package com.android.solvit.seeker.ui.provider

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProviderRegistrationScreen(
    viewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  var fullName by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  // represent the current authentified user
  val user by authViewModel.user.collectAsState()

  // Step tracking: Role, Details, Preferences
  var currentStep by remember { mutableStateOf(1) }
  val scrollState = rememberScrollState()
  val isFormComplete =
      fullName.isNotBlank() &&
          phone.isNotBlank() &&
          location.isNotBlank() &&
          password.isNotBlank() &&
          confirmPassword.isNotBlank()
  // && password == confirmPassword

  val context = LocalContext.current

  Scaffold(
      content = {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                // Back Button
                IconButton(
                    onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                      Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                label = { Text("Full Name", color = Color.Black) },
                placeholder = { Text("Enter your full name") },
                modifier = Modifier.fillMaxWidth().testTag("fullNameInput"),
                shape = RoundedCornerShape(12.dp),
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00C853), // Green outline for focused state
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
                        focusedBorderColor = Color(0xFF00C853), // Green outline for focused state
                        unfocusedBorderColor = Color.Gray // Gray outline for unfocused state
                        ))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Business/Company Name", color = Color.Black) },
                placeholder = {
                  Text("Enter your business name (optional for independent providers) ")
                },
                modifier = Modifier.fillMaxWidth().testTag("companyNameInput"),
                shape = RoundedCornerShape(12.dp),
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF28A745), // Green outline for focused state
                        unfocusedBorderColor = Color.Gray // Gray outline for unfocused state
                        ))
            Spacer(modifier = Modifier.height(16.dp))
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location", color = Color.Black) },
                placeholder = { Text("Enter your location or business location") },
                modifier = Modifier.fillMaxWidth().testTag("locationInput"),
                shape = RoundedCornerShape(12.dp),
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00C853), // Green outline for focused state
                        unfocusedBorderColor = Color.Gray // Gray outline for unfocused state
                        ))
            Spacer(modifier = Modifier.height(16.dp))
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Black) },
                placeholder = { Text("Enter your password") },
                modifier = Modifier.fillMaxWidth().testTag("passwordInput"),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(), // Hide password
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00C853), unfocusedBorderColor = Color.Gray))
            Spacer(modifier = Modifier.height(16.dp))
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color.Black) },
                placeholder = { Text("Re-enter your password") },
                modifier = Modifier.fillMaxWidth().testTag("confirmPasswordInput"),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(), // Hide password
                colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00C853), unfocusedBorderColor = Color.Gray))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                  // Move to next step (Step 2: Preferences)
                  if (password != confirmPassword) {
                    // Show toast if passwords do not match
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                  } else {
                    // Move to next step (Step 2: Preferences)
                    currentStep = 2
                  }
                },
                modifier =
                    Modifier.fillMaxWidth().height(60.dp).testTag("completeRegistrationButton"),
                enabled = isFormComplete,
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(backgroundColor = Color(0xFF28A745)) // Green button
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
                      color = Color.Blue)
                  Spacer(modifier = Modifier.height(100.dp))
                  Button(
                      onClick = { currentStep = 3 },
                      modifier = Modifier.fillMaxWidth().testTag("savePreferencesButton"),
                      colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF28A745))) {
                        Text("Save Preferences", color = Color.White)
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
                  val onSuccess: () -> Unit = {
                    val newProviderProfile =
                        Provider(
                            uid = Firebase.auth.currentUser!!.uid,
                            name = fullName,
                            phone = phone,
                            companyName = companyName,
                            location = Location(0.0, 0.0, location))
                    viewModel.addProvider(newProviderProfile)
                  }
                  // Complete registration and navigate
                  if (authViewModel.googleAccount.value == null) {
                    authViewModel.setPassword(password)
                    authViewModel.registerWithEmailAndPassword(onSuccess, {})
                  } else {
                    authViewModel.registerWithGoogle(onSuccess, {})
                  }
                  // navigationActions.goBack() // Navigate after saving
                },
                modifier = Modifier.fillMaxWidth().testTag("continueDashboardButton"),
                colors =
                    ButtonDefaults.buttonColors(backgroundColor = Color(0xFF28A745)) // Green button
                ) {
                  Text("Continue to My Dashboard", color = Color.White)
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
            color = Color.Black,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 4.dp))
      }
}
