package com.android.solvit.seeker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * A composable function that displays a service booking screen for users, including a top bar,
 * service details, price, appointment, and a map showing the service location.
 */
@Composable
fun ServiceBookingScreen() {
    // Scaffold provides the basic structure for the screen with a top bar and content
    Scaffold(
        topBar = {
            // TopAppBar displays the navigation icon and title of the screen
            TopAppBar(
                backgroundColor = Color.White, // White background for the top bar
                title = {
                    // Centered title within the AppBar
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Your booking", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    // Navigation icon to go back to the previous screen (currently unhandled)
                    IconButton(
                        onClick = { /* Handle back navigation */},
                        modifier = Modifier.testTag("goBackButton")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    // Empty action space to balance the top bar layout (could be used for future actions)
                    Box(modifier = Modifier.size(48.dp))
                }
            )
        }
    ) { innerPadding ->
        // Main content of the screen inside a Column
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen
                .padding(16.dp) // Padding for content
                .background(Color.White) // White background for the screen
                .padding(innerPadding) // Additional padding to respect Scaffold's inner padding
                .verticalScroll(rememberScrollState()) // Make the content scrollable
        ) {
            // Problem description section
            Text(
                text = "Problem Description", // Title for the problem description
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp).testTag("problem_description_label")
            )

            // Detailed description of the problem faced by the user
            Text(
                text =
                "I've been having an issue with the kitchen sink. It's draining very slowly, and every time I run the water, it starts to back up. I tried using a plunger, but it didn’t make a difference. The drain is making gurgling sounds, and it smells unpleasant, which suggests a blockage somewhere in the pipes. The issue seems to be getting worse, and I’m worried it might lead to more serious plumbing problems if not addressed soon.",
                fontSize = 16.sp,
                color = Color.Gray, // Gray color for better readability
                modifier = Modifier.padding(bottom = 16.dp).testTag("problem_description")
            )

            // A row containing two parallel sections: Profile/Rating and Price/Appointment
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Occupies full width of the screen
                    .height(220.dp) // Fixed height for uniformity
                    .padding(vertical = 16.dp), // Padding between the row and other components
                horizontalArrangement = Arrangement.SpaceBetween // Space between the boxes
            ) {
                // Left box: Profile and rating section
                Box(
                    modifier = Modifier
                        .weight(1f) // Takes up half of the available width
                        .background(Color.White, RoundedCornerShape(8.dp)) // White background with rounded corners
                        .padding(16.dp) // Padding inside the box
                ) {
                    // Container for the profile image
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Fill the available space in the box
                            .background(Color.Gray, RoundedCornerShape(8.dp)) // Gray background and rounded corners
                            .testTag("profile_image_container") // Test tag for UI testing
                    ) {
                        // Profile image
                        Image(
                            painter = painterResource(id = R.drawable.empty_profile_img), // Placeholder image
                            contentDescription = "Plumber Image", // Content description for accessibility
                            contentScale = ContentScale.Crop, // Scale the image to crop and fill the box
                            modifier = Modifier.fillMaxSize() // Fills the entire container
                        )

                        // Rating section displayed at the bottom-right corner of the image
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd) // Positioned at the bottom-right
                                .padding(8.dp) // Padding from the edges
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, // Align star and text vertically
                                horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between star and rating value
                            ) {
                                // Star icon representing rating
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color.Yellow, // Yellow star color
                                    modifier = Modifier.size(24.dp).testTag("rating_star_icon")
                                )

                                // Rating value (e.g., 4.7)
                                Text(
                                    text = "4.7", // Example rating value
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White, // White text for good contrast
                                    modifier = Modifier.testTag("rating_value")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp)) // Space between the two boxes

                // Right box: Price and appointment information
                Box(
                    modifier = Modifier
                        .weight(1f) // Takes up half of the available width
                        .fillMaxHeight() // Match the height of the left box
                        .background(Color(0xFF2196F3), RoundedCornerShape(8.dp)) // Blue background with rounded corners
                        .padding(16.dp) // Padding inside the box
                        .testTag("price_appointment_box") // Test tag for UI testing
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, // Center the text inside the column
                        modifier = Modifier.fillMaxWidth() // Ensure the column takes up the full width
                    ) {
                        // Price agreed upon
                        Text(
                            text = "Price agreed on:",
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Space between the text and price
                        Text(
                            text = "$156.57", // Example price
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White // White text for contrast
                        )

                        Spacer(modifier = Modifier.height(16.dp)) // Space between price and appointment text

                        // Appointment date and time
                        Text(
                            text = "Your appointment:",
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Space between the text and date
                        Text(
                            text = "24/05/2025", // Example date
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "14:30", // Example time
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Address and map section
            Text(
                text = "Address", // Address label
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).testTag("address_label")
            )

            // Google Map showing the service location
            val mapPosition = rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                    LatLng(48.8566, 2.3522), 10f // Example coordinates (Paris)
                )
            }

            // Container for the Google Map
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Fill the available width
                    .height(200.dp) // Fixed height for the map
                    .background(Color.Gray, RoundedCornerShape(8.dp)) // Gray background with rounded corners
                    .testTag("google_map_container") // Test tag for UI testing
            ) {
                GoogleMap(cameraPositionState = mapPosition, modifier = Modifier.fillMaxSize()) // Display the map
            }
        }
    }
}

