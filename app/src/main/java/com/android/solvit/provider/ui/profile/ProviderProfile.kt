package com.android.solvit.provider.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProviderProfileScreen(
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    navigationActions: NavigationActions
) {
  val userId = Firebase.auth.currentUser?.uid ?: "-1"
  val provider =
      listProviderViewModel.providersList.collectAsState().value.first { it.uid == userId }
  Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
    ProfileHeader(navigationActions, provider, authViewModel)
    Spacer(modifier = Modifier.height(10.dp))
    JobsDoneSection()
    Spacer(modifier = Modifier.height(10.dp))
    StatsSection(provider = provider)
  }
}

@Composable
fun ProfileHeader(
    navigationActions: NavigationActions,
    provider: Provider,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier =
            Modifier.background(colorScheme.primaryContainer)
                .height(400.dp)
                .padding(8.dp)
                .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          Column(
              modifier = Modifier.align(Alignment.Start),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.Bottom) {
                Box {
                  IconButton(
                      onClick = { navigationActions.goBack() },
                      modifier = Modifier.testTag("backButton")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "GoBackButton",
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.error)
                      }
                }
              }

          Spacer(modifier = Modifier.height(20.dp))

          Box(
              modifier =
                  Modifier.size(130.dp)
                      .background(colorScheme.background, shape = CircleShape)
                      .testTag("profileImage"),
              contentAlignment = Alignment.Center) {
                AsyncImage(
                    model =
                        if (provider.imageUrl != "") provider.imageUrl
                        else R.drawable.empty_profile_img,
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(110.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant, shape = CircleShape),
                    contentScale = ContentScale.Crop)
              }

          Spacer(modifier = Modifier.height(40.dp))

          Text(
              text = provider.name,
              modifier = Modifier.testTag("professionalName"),
              color = colorScheme.onBackground,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(40.dp))

          Button(
              onClick = { authViewModel.logout {} },
              modifier = Modifier.testTag("logoutButton"),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Transparent, contentColor = colorScheme.error),
              border = BorderStroke(1.dp, colorScheme.error),
          ) {
            Text("Logout")
          }
        }

    Column(
        modifier = Modifier.padding(vertical = 20.dp).padding(8.dp).weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start) {
          val titleColor = colorScheme.onBackground
          val bodyColor = colorScheme.error

          @Composable
          fun TitleText(text: String, fontSize: TextUnit = 21.sp, testTag: String = "") {
            Text(
                text = text,
                color = titleColor,
                fontSize = fontSize,
                modifier = Modifier.testTag(testTag))
          }

          @Composable
          fun BodyText(text: String, fontSize: TextUnit = 15.sp, testTag: String = "") {
            Text(
                text = text,
                color = bodyColor,
                fontSize = fontSize,
                modifier = Modifier.testTag(testTag))
          }

          Column(modifier = Modifier.align(Alignment.End)) { TitleText("Profile") }

          Spacer(modifier = Modifier.height(20.dp))

          Column {
            TitleText("Company name", testTag = "companyNameTitle")
            BodyText(provider.companyName.ifEmpty { "Not provided" }, testTag = "companyName")
          }

          Column {
            TitleText("Profession", testTag = "serviceTitle")
            BodyText(provider.service.toString(), testTag = "service")
          }

          Column {
            TitleText("Contact", testTag = "contactTitle")
            BodyText(provider.phone, testTag = "contact")
          }

          Column {
            TitleText("Location", testTag = "locationTitle")
            BodyText(provider.location.name, testTag = "location")
          }

          var isOpen by remember { mutableStateOf(true) }

          Column {
            TitleText("Position", testTag = "positionTitle")
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                // TODO : Stores the value position in memory
                modifier = Modifier.testTag("position"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Box(
                      modifier =
                          Modifier.size(32.dp, 16.dp)
                              .background(colorScheme.onSurface, shape = CircleShape)
                              .clickable { isOpen = !isOpen },
                      contentAlignment =
                          if (isOpen) Alignment.CenterEnd else Alignment.CenterStart) {
                        Box(
                            modifier =
                                Modifier.size(12.dp)
                                    .background(
                                        if (isOpen) colorScheme.secondary else colorScheme.error,
                                        shape = CircleShape))
                      }

                  Spacer(modifier = Modifier.width(8.dp))

                  Text(
                      text = if (isOpen) "open" else "close",
                      color = if (isOpen) colorScheme.secondary else colorScheme.error,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold)
                }
          }
        }
  }
}

@Composable
fun JobsDoneSection() {
  // TODO : Change the hardcoded value to jobs done by the provider
  Column(modifier = Modifier.fillMaxWidth().background(colorScheme.background).padding(16.dp)) {
    Text(
        "Jobs done",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.align(Alignment.CenterHorizontally).testTag("jobsDoneTitle"))
    Spacer(modifier = Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      JobItem("Back end")
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
              .background(colorScheme.background, shape = RoundedCornerShape(12.dp))
              .padding(8.dp)
              .testTag("jobItem"),
      contentAlignment = Alignment.Center) {
        Text(
            text = title,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = colorScheme.error)
      }
}

@Composable
fun StatsSection(provider: Provider) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .height(400.dp)
              .background(colorScheme.primary)
              .padding(16.dp)
              .testTag("statsSection"),
      horizontalAlignment = Alignment.Start) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
            Text(
                provider.rating.toString(),
                fontSize = 40.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold)
            Text("Average Rating", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
          Column(horizontalAlignment = Alignment.End) {
            // TODO : Change the hardcoded value to the actual number of jobs completed by the
            // provider
            Text(
                "37", fontSize = 40.sp, color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            Text("Jobs Completed", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
            Text(
                provider.price.toString(),
                fontSize = 20.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold)
            Text("pay range", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
                provider.deliveryTime.seconds.div(3600).toString() + " hours",
                fontSize = 20.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold)
            Text("delivery Time", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
            // TODO : Change the hardcoded value to the actual availability of the provider
            Text(
                "Excellent",
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold)
            Text("Availability", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                provider.popular.toString().replaceFirstChar {
                  if (it.isLowerCase()) it.uppercase() else it.toString()
                },
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold)
            Text("Popular", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
                if (provider.languages.isEmpty()) "Not provided" else provider.languages.toString(),
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold)
            Text("Languages", fontSize = 10.sp, color = colorScheme.onPrimary)
          }
        }
      }
}
