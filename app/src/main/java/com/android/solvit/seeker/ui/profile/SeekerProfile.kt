package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SeekerProfileScreen(viewModel: SeekerProfileViewModel, navigationActions: NavigationActions) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.seekerProfile.collectAsState()

    // Display the profile information if it's available
    userProfile.let { profile ->
        Scaffold(
            modifier = Modifier.testTag("profileScreen"),
            topBar = {
                TopAppBar(
                    backgroundColor = Color.Blue, // Set blue background
                    title = { Text("Profile", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navigationActions.goBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Handle notification click */}) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White)
                        }
                    })
            }) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture
                Image(
                    painter =
                    painterResource(
                        id =
                        R.drawable
                            .empty_profile_img), // Replace with actual profile image later
                    contentDescription = "Profile Picture",
                    modifier =
                    Modifier.size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(bottom = 16.dp)
                        .testTag("profilePicture"))

                // Edit Profile Button
                Button(
                    onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                    modifier =
                    Modifier.align(Alignment.CenterHorizontally).testTag("editProfileButton"),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)) {
                    Text("Edit Profile", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sections
                LazyColumn {
                    item {
                        SectionTitle("Headline", Modifier.testTag("sectionHeadline"))
                        ProfileOptionItem("Popular", Modifier.testTag("optionPopular"))
                    }
                    item {
                        SectionTitle("Content")
                        ProfileOptionItem("Favourite", Modifier.testTag("optionFavourite"))
                        ProfileOptionItem(
                            "Billing", Modifier.testTag("optionBilling")) // Billing Section Added
                    }
                    item {
                        SectionTitle("Preferences")
                        ProfileOptionItem("Language", Modifier.testTag("optionLanguage"))
                        ProfileOptionItem("Darkmode", Modifier.testTag("optionDarkmode"))
                    }
                }
            }
        }
    }
        ?: run {
            // Fallback when no profile data is available
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No profile data available")
            }
        }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    // Use Box to wrap the Text and apply a background color to the section title
    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F6F6)) // Set background color
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1.copy(color = Color.Black), // Set text color
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileOptionItem(optionName: String, modifier: Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(optionName)
        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
}