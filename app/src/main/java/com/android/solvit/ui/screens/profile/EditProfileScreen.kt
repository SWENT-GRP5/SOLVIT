package com.android.solvit.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.ui.navigation.NavigationActions


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
