package com.android.solvit.seeker.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun EditSeekerProfileScreen(
    viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory),
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  // Collect the user profile from the StateFlow
  val user by authViewModel.user.collectAsState()
  val userProfile by viewModel.seekerProfile.collectAsState()
  user?.let { viewModel.getUserProfile(it.uid) }

  var fullName by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }

  LaunchedEffect(userProfile) {
    fullName = userProfile.name
    username = userProfile.username
    email = userProfile.email
    phone = userProfile.phone
    address = userProfile.address
  }
  Scaffold(
      modifier = Modifier.testTag("editProfileScreen"),
      topBar = {
        TopAppBar(
            backgroundColor = Color(0xFF002366), // Set blue background
            title = { Text("Edit Profile", color = Color.White) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
              }
            })
      }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          Spacer(modifier = Modifier.height(16.dp))

          // Profile Picture
          Image(
              painter =
                  painterResource(
                      id = R.drawable.empty_profile_img), // Replace with actual profile image later
              contentDescription = "Profile Picture",
              modifier =
                  Modifier.size(100.dp)
                      .clip(CircleShape)
                      .border(2.dp, Color.Gray, CircleShape)
                      .padding(bottom = 16.dp)
                      .testTag("profilePicture"))
          TextButton(onClick = { /* Handle change picture */}) { Text("Change Picture") }

          Spacer(modifier = Modifier.height(16.dp))

          // Full Name Input
          OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Full Name") },
              modifier = Modifier.fillMaxWidth().testTag("fullNameInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Username Input
          OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              label = { Text("Username") },
              modifier = Modifier.fillMaxWidth().testTag("usernameInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Email Input
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag("emailInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Phone Number Input
          OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Phone Number") },
              modifier = Modifier.fillMaxWidth().testTag("phoneInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Address Input
          OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Address") },
              modifier = Modifier.fillMaxWidth().testTag("addressInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Save Button
          Button(
              onClick = {
                userProfile.let { profile ->
                  // Update the profile with the new values
                  viewModel.updateUserProfile(
                      SeekerProfile(
                          uid = userProfile.uid, // Use the existing UID
                          name = fullName,
                          username = username,
                          email = email,
                          phone = phone,
                          address = address))
                }
                navigationActions.goBack()
              },
              modifier = Modifier.fillMaxWidth().testTag("saveButton"),
              colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) {
                Text("Save", color = Color.White)
              }
        }
      }
}
