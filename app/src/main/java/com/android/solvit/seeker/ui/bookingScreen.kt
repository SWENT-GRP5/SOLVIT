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

@Composable
fun ServiceBookingScreen() {
  // Scaffold to include the TopAppBar
  Scaffold(
      topBar = {
        TopAppBar(
            backgroundColor = Color.White,
            title = {
              Box(
                  modifier = Modifier.fillMaxWidth(),
              ) {
                Text("Your booking", color = Color.Black, fontWeight = FontWeight.Bold)
              }
            },
            navigationIcon = {
              IconButton(
                  onClick = { /* Handle back navigation */},
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                  }
            },
            actions = {
              Box(modifier = Modifier.size(48.dp))
            } // Empty box to balance the navigation icon
            )
      }) { innerPadding ->
        // Main content inside Column with padding for innerPadding from Scaffold
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .background(Color.White) // White background for the entire screen
                    .padding(innerPadding) // Ensure innerPadding is applied
                    .verticalScroll(rememberScrollState()) // Ensure scrollable content
            ) {
              // Problem description
              Text(
                  text = "Problem Description",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 8.dp).testTag("problem_description_label"))

              Text(
                  text =
                      "I've been having an issue with the kitchen sink. It's draining very slowly, and every time I run the water, it starts to back up. I tried using a plunger, but it didn’t make a difference. The drain is making gurgling sounds, and it smells unpleasant, which suggests a blockage somewhere in the pipes. The issue seems to be getting worse, and I’m worried it might lead to more serious plumbing problems if not addressed soon.",
                  fontSize = 16.sp,
                  color = Color.Gray,
                  modifier = Modifier.padding(bottom = 16.dp).testTag("problem_description"))

              // Row for the two parallel boxes
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(220.dp) // Set a fixed height for the row
                          .padding(vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    // Left box: Profile and rating
                    Box(
                        modifier =
                            Modifier.weight(1f)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(16.dp)) {
                          Box(
                              modifier =
                                  Modifier.fillMaxSize()
                                      .background(Color.Gray, RoundedCornerShape(8.dp))
                                      .testTag("profile_image_container")) {
                                Image(
                                    painter =
                                        painterResource(
                                            id = R.drawable.empty_profile_img), // Replace with your
                                    // drawable resource ID
                                    contentDescription = "Plumber Image",
                                    contentScale =
                                        ContentScale
                                            .Crop, // Makes the image fill the box and crop if
                                    // necessary
                                    modifier = Modifier.fillMaxSize() // Fills the entire box
                                    )

                                // Rating at the bottom-right corner
                                Box(
                                    modifier =
                                        Modifier.align(
                                                Alignment.BottomEnd) // Align the rating box to the
                                            // bottom-right corner
                                            .padding(
                                                8.dp) // Add padding to keep it away from the edge
                                    ) {
                                      Row(
                                          verticalAlignment =
                                              Alignment
                                                  .CenterVertically, // Vertically center the icon
                                          // and text
                                          horizontalArrangement =
                                              Arrangement.spacedBy(
                                                  4.dp) // Add space between the icon and text
                                          ) {
                                            // Star Icon
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Rating",
                                                tint = Color.Yellow,
                                                modifier =
                                                    Modifier.size(24.dp)
                                                        .testTag("rating_star_icon"))

                                            // Rating Value (e.g., 4.7)
                                            Text(
                                                text = "4.7", // Replace with actual rating value
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.testTag("rating_value"))
                                          }
                                    }
                              }
                        }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right box: Price and appointment in blue box
                    Box(
                        modifier =
                            Modifier.weight(1f) // Makes the box take equal space
                                .fillMaxHeight() // Ensures the height of the box matches the other
                                .background(
                                    Color(0xFF2196F3),
                                    RoundedCornerShape(
                                        8.dp)) // Blue background with rounded corners
                                .padding(16.dp)
                                .testTag("price_appointment_box")) {
                          Column(
                              horizontalAlignment = Alignment.CenterHorizontally,
                              modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Price agreed on:",
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "$156.57",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Your appointment:",
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "24/05/2025",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White)
                                Text(
                                    text = "14:30",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White)
                              }
                        }
                  }

              // Address and Map section
              Text(
                  text = "Address",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).testTag("address_label"))

              // Google Map Composable
              val mapPosition = rememberCameraPositionState {
                position =
                    com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                        LatLng(48.8566, 2.3522), 10f) // Replace with actual coordinates
              }

              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(200.dp)
                          .background(Color.Gray, RoundedCornerShape(8.dp))
                          .testTag("google_map_container")) {
                    GoogleMap(cameraPositionState = mapPosition, modifier = Modifier.fillMaxSize())
                  }
            }
      }
}

@Preview(showBackground = true)
@Composable
fun ServiceBookingScreenPreview() {
  ServiceBookingScreen()
}
