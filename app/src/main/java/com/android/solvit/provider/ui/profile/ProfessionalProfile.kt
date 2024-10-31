package com.android.solvit.provider.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.R
import com.android.solvit.shared.ui.authentication.VerticalSpacer
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun ProfessionalProfileScreen(navigationActions: NavigationActions) {
  Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
    ProfileHeader(navigationActions)
    Spacer(modifier = Modifier.height(16.dp))
    JobsDoneSection()
    Spacer(modifier = Modifier.height(16.dp))
    StatsSection()
  }
}

@Composable
fun ProfileHeader(navigationActions: NavigationActions) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier =
            Modifier.background(Color(0, 121, 107, 100)).height(350.dp).padding(8.dp).weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {

        Column(modifier = Modifier.align(Alignment.Start),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom) {

          Box {
            IconButton(
                onClick = { navigationActions.goBack() }) {
                  Icon(
                      Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back",
                      modifier = Modifier.size(24.dp),
                      tint = Color(239, 70, 55))
                }
          }}

        VerticalSpacer(40.dp)

          Box(
              modifier = Modifier.size(130.dp).background(Color.White, shape = CircleShape),
              contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.empty_profile_img),
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD1DC), shape = CircleShape),
                    contentScale = ContentScale.Crop)
              }

          VerticalSpacer(16.dp)

          Text("Rim Abkari", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

    Column(
        modifier = Modifier.padding(vertical = 20.dp).padding(8.dp).weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start) {
          val titleColor = Color(0, 0, 1)
          val bodyColor = Color(239, 70, 55)
          @Composable
          fun TitleText(text: String, fontSize: TextUnit = 21.sp) {
            Text(text = text, color = titleColor, fontSize = fontSize)
          }

          @Composable
          fun BodyText(text: String, fontSize: TextUnit = 15.sp) {
            Text(text = text, color = bodyColor, fontSize = fontSize)
          }

          Column(modifier = Modifier.align(Alignment.End)) { TitleText("Profile") }

          VerticalSpacer(40.dp)

          Column {
            TitleText("Profession")
            BodyText("Contractor")
          }

          Column {
            TitleText("Contact")
            BodyText("+234 808 2344 4675")
          }

          Column {
            TitleText("Location")
            BodyText("Lagos")
          }

          var isOpen by remember { mutableStateOf(true) }

          Column {
            TitleText("Position")
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Box(
                      modifier =
                          Modifier.size(32.dp, 16.dp)
                              .background(Color(0xFF4A4A4A), shape = CircleShape)
                              .clickable { isOpen = !isOpen },
                      contentAlignment =
                          if (isOpen) Alignment.CenterEnd else Alignment.CenterStart) {
                        Box(
                            modifier =
                                Modifier.size(12.dp)
                                    .background(
                                        if (isOpen) Color.Green else Color(239, 70, 55),
                                        shape = CircleShape))
                      }

                  Spacer(modifier = Modifier.width(8.dp))

                  Text(
                      text = if (isOpen) "open" else "close",
                      color = if (isOpen) Color.Green else Color(239, 70, 55),
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold)
                }
          }
        }
  }
}

@Composable
fun JobsDoneSection() {
  Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
    Text(
        "Jobs done",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.align(Alignment.CenterHorizontally))
    Spacer(modifier = Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      JobItem("Product Design")
      JobItem("Front end")
      JobItem("Visual Designer")
      JobItem("Voyager")
    }
  }
}

@Composable
fun JobItem(title: String) {
  Box(
      modifier =
          Modifier.size(80.dp)
              .shadow(4.dp, shape = RoundedCornerShape(12.dp))
              .background(Color.White, shape = RoundedCornerShape(12.dp))
              .padding(8.dp),
      contentAlignment = Alignment.Center) {
        Text(
            text = title,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = Color(239, 70, 55))
      }
}

@Composable
fun StatsSection() {
  Column(
      modifier = Modifier.fillMaxWidth().height(400.dp).background(Color(0,121,107)).padding(16.dp),
      horizontalAlignment = Alignment.Start) {
      Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
              Text("4,3", fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
              Text("Average Rating", fontSize = 7.sp, color = Color.White)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("37", fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
              Text("Jobs Completed", fontSize = 7.sp, color = Color.White)
          }
      }
      Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
              Text("pay range", fontSize = 7.sp, color = Color.White)
              Text("150k - 200k", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
              Text("(negotiable)", fontSize = 7.sp, color = Color.White)
          }
          Column(horizontalAlignment = Alignment.Start) {
              Text("02", fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
              Text("ongoing", fontSize = 7.sp, color = Color.White)
          }
      }
      Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
              Text("Availability", fontSize = 10.sp, color = Color.White)
              Text("Excellent", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
          }
          Column(horizontalAlignment = Alignment.Start) {
              Text("Service", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
              Text("Good", fontSize = 7.sp, color = Color.White)
          }
          Column(horizontalAlignment = Alignment.Start) {
              Text("Quality", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
              Text("Good", fontSize = 7.sp, color = Color.White)
          }
}}}
