package com.android.solvit.seeker.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
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
    viewModel: SeekerProfileViewModel,
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  // Collect the user profile from the StateFlow
  val user by authViewModel.user.collectAsState()
  val userProfile by viewModel.seekerProfile.collectAsState()
  user?.let { viewModel.getUserProfile(it.uid) }
  // viewModel.getUserProfile("fRTusprXsOfd3bS7RD4z44xpLZh1")

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
                      .padding(bottom = 16.dp))
          TextButton(onClick = { /* Handle change picture */}) { Text("Change Picture") }

          Spacer(modifier = Modifier.height(16.dp))

          // Full Name Input
          OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Full Name") },
              modifier = Modifier.fillMaxWidth().testTag("profileName"))

          Spacer(modifier = Modifier.height(16.dp))

          // Username Input
          OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              label = { Text("Username") },
              modifier = Modifier.fillMaxWidth().testTag("profileUsername"))

          Spacer(modifier = Modifier.height(16.dp))

          // Email Input
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag("profileEmail"))

          Spacer(modifier = Modifier.height(16.dp))

          // Phone Number Input
          OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Phone Number") },
              modifier = Modifier.fillMaxWidth().testTag("profilePhone"))

          Spacer(modifier = Modifier.height(16.dp))

          // Address Input
          OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Address") },
              modifier = Modifier.fillMaxWidth().testTag("profileAddress"))

          Spacer(modifier = Modifier.height(16.dp))

          // Save Button
          Button(
              onClick = {
                userProfile?.let { profile ->
                  // Update the profile with the new values
                  viewModel.updateUserProfile(
                      SeekerProfile(
                          uid = profile.uid, // Use the existing UID
                          name = fullName,
                          username = username,
                          email = email,
                          phone = phone,
                          address = address))
                }
                navigationActions.goBack()
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) {
                Text("Save", color = Color.White)
              }
        }
      }
}
