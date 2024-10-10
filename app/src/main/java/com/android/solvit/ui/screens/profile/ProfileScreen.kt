package com.android.solvit.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.R
import com.android.solvit.ui.navigation.NavigationActions
import com.android.solvit.ui.navigation.Screen

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, navigationActions: NavigationActions) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.userProfile.collectAsState()

    // Display the profile information if it's available
    if (userProfile.isNotEmpty()) {
        val profile = userProfile[0]  // Assuming only one profile in the list for simplicity

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.empty_profile_img),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 16.dp)
            )
            // Header Section with Title and Edit Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                }
            }

            // Display Name
            ProfileField(label = "Name", value = profile.name)

            // Spacer between fields
            Spacer(modifier = Modifier.height(16.dp))

            // Display Email with Icon
            ProfileFieldWithIcon(
                icon = Icons.Filled.Email,
                label = "Email",
                value = profile.email
            )

            // Spacer between fields
            Spacer(modifier = Modifier.height(16.dp))

            // Display Phone with Icon
            ProfileFieldWithIcon(
                icon = Icons.Filled.Phone,
                label = "Phone",
                value = profile.phone
            )
        }
    } else {
        // Fallback UI when the profile data is not available
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No profile data available", style = MaterialTheme.typography.body1)
        }
    }
}

// Reusable composable for displaying a profile field
@Composable
fun ProfileField(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ProfileFieldWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontWeight = FontWeight.SemiBold)
            Text(
                text = value,
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.body1
            )
        }
    }
}
