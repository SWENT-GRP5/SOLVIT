package com.android.solvit.ui.requests

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestType
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.google.firebase.Timestamp
import java.util.Calendar

// Composable function representing the top bar with a menu, slogan, and notifications icon
@Composable
fun RequestsTopBar() {
  Row(
      modifier = Modifier.testTag("RequestsTopBar").fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // Menu button
    IconButton(modifier = Modifier.testTag("MenuOption"), onClick = { TODO() }) {
      Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Option")
    }

    // Display the app's slogan
    Row(modifier = Modifier.testTag("SloganIcon"), verticalAlignment = Alignment.CenterVertically) {
      Text(
          text = "Solv",
          style =
              TextStyle(
                  fontSize = 15.sp,
                  fontWeight = FontWeight(700),
                  color = Color(0xFF333333),
              ))
      Text(
          text = "it",
          style =
              TextStyle(
                  fontSize = 15.sp,
                  fontWeight = FontWeight(700),
                  color = Color(0xFF3D823B),
              ))
    }

    // Notifications button
    IconButton(onClick = { TODO() }) {
      Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
    }
  }
}

// Composable function representing the search bar with a text input field
@Composable
fun SearchBar() {
  var userResearch by remember { mutableStateOf("") }
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    BasicTextField(
        value = "",
        onValueChange = { userResearch = it },
        singleLine = true,
        textStyle = TextStyle(color = Color.Gray, fontSize = 16.sp),
        modifier =
            Modifier.shadow(
                    elevation = 20.dp,
                    spotColor = Color(0x0F000000),
                    ambientColor = Color(0x0F000000))
                .border(
                    width = 1.dp,
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(size = 8.dp))
                .width(265.dp)
                .height(32.99994.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 8.dp))
                .testTag("SearchBar")) {
          // Placeholder text in the search bar
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.width(14.dp).height(15.dp),
                painter = painterResource(id = R.drawable.search_icon),
                contentDescription = "image description",
                contentScale = ContentScale.Crop)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Search requests",
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.daibanna)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFAFAFAF),
                    ))
          }
        }
  }
}

// Composable function that displays the title screen text and an accompanying image
@Composable
fun TitleScreen() {
  Box(
      modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("TitleScreen"),
  ) {
    Text(
        text = "Find Your Seeker",
        fontSize = 30.sp,
        fontFamily = FontFamily(Font(R.font.ruwudu)),
        fontWeight = FontWeight(400),
        color = Color(0xFF00C853),
        textAlign = TextAlign.Center,
    )

    Image(
        modifier =
            Modifier.align(
                    Alignment
                        .BottomEnd) // Aligns the image to the bottom end (right under "Seeker")
                .offset(
                    x = (-170).dp,
                    y = 0.dp) // Adjust the X and Y offsets to place the image properly
                .width(50.dp) // Adjust the width to match "Seeker"
                .height(5.dp), // Adjust the height of the image
        painter = painterResource(id = R.drawable.title_icon),
        contentDescription = "image description",
        contentScale = ContentScale.Fit)
  }
}

// Displays a list of service requests using LazyColumn
@Composable
fun ListRequests(requests: List<ServiceRequest>) {
  LazyColumn(
      modifier = Modifier.fillMaxSize().testTag("requests"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(requests) { request ->
          Column(
              modifier =
                  Modifier.fillMaxWidth().padding(8.dp).background(color = Color(0xFFFAFAFA))) {
                HorizontalDivider(
                    Modifier.border(width = 2.dp, color = Color(0xFFE0E0E0))
                        .padding(2.dp)
                        .fillMaxWidth()
                        .height(0.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {

                      // Profile picture
                      Image(
                          painter =
                              painterResource(
                                  id =
                                      R.drawable
                                          .image_user), // TODO Replace with actual image resource
                          contentDescription = "Profile Picture",
                          modifier = Modifier.size(50.dp).clip(CircleShape))

                      Spacer(modifier = Modifier.width(8.dp))
                      // Assignee information
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.assigneeName,
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF000000),
                            letterSpacing = 1.sp,
                        )
                        request.location?.let {
                          Text(text = it.name, fontSize = 12.sp, color = Color.Gray)
                        }
                        // TODO date request was created

                      }
                      IconButton(onClick = { /*TODO report add ...*/}) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                      }
                    }

                Spacer(modifier = Modifier.height(8.dp))
                // Request description
                Text(
                    text = request.description,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 18.2.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF000000),
                        ))

                Spacer(modifier = Modifier.height(8.dp))
                // Display image related to the request
                AsyncImage(
                    modifier =
                        Modifier.width(237.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .align(Alignment.CenterHorizontally),
                    model = request.imageUrl,
                    placeholder = painterResource(id = R.drawable.loading),
                    error = painterResource(id = R.drawable.error),
                    contentDescription = "service image",
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    Modifier.border(width = 2.dp, color = Color(0xFFE0E0E0))
                        .padding(2.dp)
                        .fillMaxWidth()
                        .height(0.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Interaction bar for comments, sharing, and replying
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      InteractionBar("Comment", R.drawable.comment_icon)
                      InteractionBar("Share", R.drawable.share_icon)
                      InteractionBar("Reply", R.drawable.reply_icon)
                    }
              }
        }
      }
}

// A composable for the interaction bar, showing options to comment, share, and reply
@Composable
fun InteractionBar(text: String, icon: Int) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontFamily = FontFamily(Font(R.font.roboto)),
        fontWeight = FontWeight(500),
        color = Color(0xFF585C60))

    IconButton(onClick = { /* Handle comment click EPIC 3*/}) {
      Image(
          modifier = Modifier.padding(0.dp).width(21.dp).height(18.90004.dp),
          painter = painterResource(id = icon),
          contentDescription = text,
          contentScale = ContentScale.Crop)
    }
  }
}

// Main screen displaying the list of requests with the top bar, search bar, and title
@Composable
fun ListRequestsFeedScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory)
) {
  val requests by serviceRequestViewModel.requests.collectAsState()
  serviceRequestViewModel.getServiceRequests()
  val request =
      listOf(
          ServiceRequest(
              title = "Bathtub leak",
              description = "I hit my bath too hard and now it's leaking",
              assigneeName = "Nathan",
              dueDate = Timestamp(Calendar.getInstance().time),
              location =
                  Location(
                      48.8588897,
                      2.3200410217200766,
                      "Paris, Île-de-France, France métropolitaine, France"),
              status = ServiceRequestStatus.PENDING,
              uid = "gIoUWJGkTgLHgA7qts59",
              type = ServiceRequestType.PLUMBING,
              imageUrl =
                  "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"))

  Log.e("ListRequestsFeed", "$requests")
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("ListRequestsScreen"),
      topBar = { RequestsTopBar() },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = Color(0xFFF6F6F6))
                    .testTag("ScreenContent")) {
              SearchBar()
              Spacer(Modifier.height(15.dp))
              TitleScreen()
              ListRequests(request)
            }
      })
}
