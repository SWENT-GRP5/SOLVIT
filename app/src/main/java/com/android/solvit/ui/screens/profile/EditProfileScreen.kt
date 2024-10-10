package com.android.solvit.ui.screens.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.ui.navigation.NavigationActions
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.android.solvit.R


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    navigationActions: NavigationActions
) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.userProfile.collectAsState()

    // Assuming a single user profile (adjust as needed)
    val currentProfile = userProfile.firstOrNull()

    // Initialize fields with existing user data
    var name by remember { mutableStateOf(currentProfile?.name ?: "") }
    var email by remember { mutableStateOf(currentProfile?.email ?: "") }
    var phone by remember { mutableStateOf(currentProfile?.phone ?: "") }

    Scaffold(
        modifier = Modifier.testTag("editScreen"),
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", modifier = Modifier.testTag("editTodoTitle")) },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag("goBackButton"),
                        onClick = { navigationActions.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
                    }
                })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Name Input
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Input
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Input
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Changes Button
                Button(onClick = {
                    currentProfile?.let { profile ->
                        // Update the profile with the new values
                        viewModel.updateUserProfile(
                            UserProfile(
                                uid = profile.uid, // Ensure to use the existing uid
                                name = name,
                                email = email,
                                phone = phone
                            )
                        )
                    }
                    navigationActions.goBack()
                }) {
                    Text("Save Changes")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Button to go back without saving
                Button(onClick = {
                    navigationActions.goBack() // Navigate back to the ProfileScreen without saving
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}
*/

/*
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    navigationActions: NavigationActions
) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.userProfile.collectAsState()

    // Assuming a single user profile (adjust as needed)
    val currentProfile = userProfile.firstOrNull()

    // Initialize fields with existing user data
    var name by remember { mutableStateOf(currentProfile?.name ?: "") }
    var email by remember { mutableStateOf(currentProfile?.email ?: "") }
    var phone by remember { mutableStateOf(currentProfile?.phone ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.empty_profile_img), // Replace with actual profile image later
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .padding(bottom = 16.dp)
            )
            TextButton(onClick = { /* Handle change picture */ }) {
                Text("Change Picture")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Id") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Update Button
            Button(
                onClick = {
                    currentProfile?.let { profile ->
                        // Update the profile with the new values
                        viewModel.updateUserProfile(
                            UserProfile(
                                uid = profile.uid, // Use the existing UID
                                name = name,
                                email = email,
                                phone = phone
                            )
                        )
                    }
                    navigationActions.goBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update")
            }
        }
    }
}
*/

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    navigationActions: NavigationActions
) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.userProfile.collectAsState()

    // Assuming a single user profile (adjust as needed)
    val currentProfile = userProfile.firstOrNull()

    // Initialize fields with existing user data
    var fullName by remember { mutableStateOf(currentProfile?.name ?: "") }
    var username by remember { mutableStateOf(currentProfile?.username ?: "") }
    var email by remember { mutableStateOf(currentProfile?.email ?: "") }
    var phone by remember { mutableStateOf(currentProfile?.phone ?: "") }
    var address by remember { mutableStateOf(currentProfile?.address ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color(0xFF002366), // Set blue background
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.empty_profile_img), // Replace with actual profile image later
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .padding(bottom = 16.dp)
            )
            TextButton(onClick = { /* Handle change picture */ }) {
                Text("Change Picture")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Address Input
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    currentProfile?.let { profile ->
                        // Update the profile with the new values
                        viewModel.updateUserProfile(
                            UserProfile(
                                uid = profile.uid, // Use the existing UID
                                name = fullName,
                                username=username,
                                email = email,
                                phone = phone,
                                address=address
                            )
                        )
                    }
                    navigationActions.goBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}