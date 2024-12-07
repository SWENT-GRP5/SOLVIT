package com.android.solvit.provider.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

/**
 * A composable function that displays the provider's profile screen. It includes the profile header
 * and the statistics section. It also ensures that the provider's data is updated from Firebase
 * each time the screen is launched.
 *
 * @param listProviderViewModel The ViewModel managing the list of providers.
 * @param authViewModel The ViewModel managing authentication.
 * @param navigationActions Actions for navigating between screens.
 */
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun ProviderProfileScreen(
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    navigationActions: NavigationActions
) {
  LaunchedEffect(Unit) { listProviderViewModel.getProviders() }
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  val user = authViewModel.user.collectAsState()
  val userId = user.value?.uid ?: "-1"
  val providers = listProviderViewModel.providersList.collectAsState()
  val provider by remember { mutableStateOf(providers.value.firstOrNull { it.uid == userId }) }

  // Log.d("ProviderProfileScreen", "Provider: $provider")

  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(colorScheme.background)
              .verticalScroll(rememberScrollState())) {
        ProfileHeader(navigationActions, provider!!, authViewModel)
        DescriptionSection(provider = provider!!)
        StatsSection(provider = provider!!)
      }
}

/**
 * A composable function that displays the profile header for the provider. It includes the profile
 * picture, name, and other basic information.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param provider The provider whose profile is being displayed.
 * @param authViewModel The ViewModel managing authentication.
 */
@Composable
fun ProfileHeader(
    navigationActions: NavigationActions,
    provider: Provider,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  var fullName by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var service by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }

  var showLogoutDialog by remember { mutableStateOf(false) }

  LaunchedEffect(provider) {
    fullName = provider.name
    companyName = provider.companyName
    service = Services.format(provider.service)
    phone = provider.phone
    location = provider.location.name
  }

  Row(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier =
            Modifier.background(colorScheme.primaryContainer)
                .wrapContentHeight()
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
              text = fullName,
              modifier = Modifier.testTag("professionalName"),
              color = colorScheme.onBackground,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis)

          Spacer(modifier = Modifier.height(50.dp))

          Button(
              onClick = { showLogoutDialog = true },
              modifier = Modifier.testTag("logoutButton"),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Transparent, contentColor = colorScheme.error),
              border = BorderStroke(1.dp, colorScheme.error),
          ) {
            Text("Logout")
          }

          if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log out", modifier = Modifier.testTag("LogoutDialogTitle")) },
                text = {
                  Text(
                      "Are you sure you want to log out?",
                      modifier = Modifier.testTag("LogoutDialogText"))
                },
                confirmButton = {
                  Button(
                      onClick = {
                        authViewModel.logout {}
                        showLogoutDialog = false
                      },
                      modifier = Modifier.testTag("LogoutDialogConfirmButton")) {
                        Text("Log out")
                      }
                },
                dismissButton = {
                  Button(
                      onClick = { showLogoutDialog = false },
                      modifier = Modifier.testTag("LogoutDialogDismissButton")) {
                        Text("Cancel")
                      }
                },
                modifier = Modifier.testTag("LogoutDialog"))
          }
        }

    Column(
        modifier = Modifier.wrapContentHeight().padding(8.dp).weight(1f),
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
          fun BodyText(
              text: String,
              fontSize: TextUnit = 15.sp,
              testTag: String = "",
              maxLines: Int = Int.MAX_VALUE
          ) {
            Text(
                text = text,
                color = bodyColor,
                fontSize = fontSize,
                modifier = Modifier.testTag(testTag),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis)
          }

          Column(
              modifier = Modifier.align(Alignment.End),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.Bottom) {
                Box {
                  IconButton(
                      onClick = { navigationActions.navigateTo(Screen.PROVIDER_MODIFY_PROFILE) },
                      modifier = Modifier.size(24.dp).testTag("editProfileButton")) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(24.dp))
                      }
                }
              }

          Spacer(modifier = Modifier.height(5.dp))

          Column {
            TitleText("Company name", testTag = "companyNameTitle")
            BodyText(
                companyName.ifEmpty { "Not provided" }.replaceFirstChar { it.uppercase() },
                testTag = "companyName",
                maxLines = 2)
          }

          Column {
            TitleText("Profession", testTag = "serviceTitle")
            BodyText(service.ifEmpty { "Not provided" }, testTag = "service", maxLines = 1)
          }

          Column {
            TitleText("Phone number", testTag = "phoneNumberTitle")
            BodyText(phone.ifEmpty { "Not provided" }, testTag = "phoneNumber", maxLines = 1)
          }

          Column {
            TitleText("Location", testTag = "locationTitle")
            BodyText(location.ifEmpty { "Not provided" }, testTag = "location", maxLines = 3)
          }
        }
  }
}

/**
 * Displays a styled description section with a title and provider description, supporting state
 * updates and test tags.
 */
@Composable
fun DescriptionSection(provider: Provider) {

  var description by remember { mutableStateOf("") }

  LaunchedEffect(provider) { description = provider.description }
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .wrapContentHeight()
              .background(colorScheme.primary)
              .padding(16.dp)
              .testTag("descriptionSection"),
      horizontalAlignment = Alignment.Start) {
        Column {
          Text(
              "Description",
              fontSize = 40.sp,
              color = colorScheme.onPrimary,
              fontWeight = FontWeight.Bold,
              maxLines = 7,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("descriptionTitle"))
          Text(
              description,
              fontSize = 15.sp,
              color = colorScheme.onPrimary,
              maxLines = 7,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("descriptionText"))
        }
      }
}

/**
 * A composable function that displays the statistics section for a provider. It shows the
 * provider's average rating, popularity status, and the languages they support.
 *
 * @param provider The provider whose statistics are to be displayed.
 */
@Composable
fun StatsSection(
    provider: Provider,
    viewModel: ServiceRequestViewModel = viewModel(factory = ServiceRequestViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  var rating by remember { mutableStateOf("") }
  var popular by remember { mutableStateOf("") }

  val user by authViewModel.user.collectAsState()
  val providerId = user?.uid ?: "-1"

  val pendingTasks by viewModel.pendingRequests.collectAsState()
  val filteredPendingTasks = pendingTasks.filter { it.providerId == providerId }

  val acceptedTasks by viewModel.acceptedRequests.collectAsState()
  val filteredAcceptedTasks = acceptedTasks.filter { it.providerId == providerId }

  val scheduledTasks by viewModel.scheduledRequests.collectAsState()
  val filteredScheduledTasks = scheduledTasks.filter { it.providerId == providerId }

  val completedTasks by viewModel.completedRequests.collectAsState()
  val filteredCompletedTasks = completedTasks.filter { it.providerId == providerId }

  val canceledTasks by viewModel.cancelledRequests.collectAsState()
  val filteredCanceledTasks = canceledTasks.filter { it.providerId == providerId }

  val archivedTasks by viewModel.archivedRequests.collectAsState()
  val filteredArchivedTasks = archivedTasks.filter { it.providerId == providerId }

  val earnings = filteredCompletedTasks.sumOf { it.agreedPrice ?: 0.0 }

  LaunchedEffect(provider) {
    rating = provider.rating.toString()
    popular = provider.popular.toString()
  }
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .height(500.dp)
              .background(colorScheme.primary)
              .padding(16.dp)
              .testTag("statsSection"),
      horizontalAlignment = Alignment.Start) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
            Text(
                rating,
                fontSize = 40.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("ratingText"))
            Text(
                "Average Rating",
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                modifier = Modifier.testTag("ratingLabel"))
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                popular.replaceFirstChar {
                  if (it.isLowerCase()) it.uppercase() else it.toString()
                },
                fontSize = 40.sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("popularityText"))
            Text(
                "Popular",
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                modifier = Modifier.testTag("popularityLabel"))
          }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
          Column(horizontalAlignment = Alignment.Start) {
            Text(
                "$earnings CHF",
                fontSize =
                    (if (earnings > 999999) 10
                        else if (earnings > 999 && earnings < 999999) 20 else 40)
                        .sp,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("earningsText"))
            Text(
                "Earnings",
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                modifier = Modifier.testTag("earningsLabel"))
          }
        }

        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            LanguageList(provider)
            Text(
                (if (provider.languages.size > 1) "Languages" else "Language"),
                fontSize = 15.sp,
                color = colorScheme.onPrimary,
                modifier = Modifier.testTag("languagesLabel"))
          }
          Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Column {
              Text(
                  "Pending tasks: ${filteredPendingTasks.size}",
                  fontSize = 20.sp,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("pendingTasksText"))
              Text(
                  "Accepted tasks: ${filteredAcceptedTasks.size}",
                  fontSize = 20.sp,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("acceptedTasksText"))
              Text(
                  "Scheduled tasks: ${filteredScheduledTasks.size}",
                  fontSize = 20.sp,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("scheduledTasksText"))
              Text(
                  "Completed tasks: ${filteredCompletedTasks.size}",
                  fontSize = 20.sp,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("completedTasksText"))
              Text(
                  "Canceled tasks: ${filteredCanceledTasks.size}",
                  fontSize = 20.sp,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("canceledTasksText"))
              Text(
                  "Archived tasks: ${filteredArchivedTasks.size}",
                  fontSize = 20.sp,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("archivedTasksText"))
            }
          }
        }
      }
}

/**
 * A composable function that displays a list of languages for a provider. It shows either all
 * languages or only the first three, with an option to toggle between them.
 *
 * @param provider The provider whose languages are to be displayed.
 */
@Composable
fun LanguageList(provider: Provider) {

  var showAll by remember {
    mutableStateOf(false)
  } // State to toggle between showing 3 items or all

  Column(
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.testTag("languageListColumn") // Test tag for the whole section
      ) {
        if (provider.languages.isEmpty()) {
          Text(
              "Not provided",
              color = colorScheme.error,
              modifier = Modifier.testTag("noLanguagesText") // Tag for "Not provided" text
              )
        } else {
          LazyColumn(
              modifier =
                  Modifier.fillMaxWidth()
                      .heightIn(
                          max =
                              if (!showAll && provider.languages.size > 3) 200.dp
                              else Dp.Unspecified)
                      .testTag("languagesLazyColumn"), // Tag for the LazyColumn
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Show either all items or just the first 3 based on showAll
                val languagesToShow =
                    if (showAll) provider.languages else provider.languages.take(3)
                items(languagesToShow) { language ->
                  Text(
                      text = language.name.replaceFirstChar { it.uppercase() },
                      fontSize = 40.sp,
                      color = colorScheme.onPrimary,
                      fontWeight = FontWeight.Bold,
                      modifier =
                          Modifier.testTag(
                              "languageItem_${language.name}") // Tag for each language item
                      )
                }
                if (!showAll && provider.languages.size > 3) {
                  item {
                    Text(
                        "View more...",
                        fontSize = 16.sp,
                        color = colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        modifier =
                            Modifier.clickable {
                                  showAll = true // Updates state to show all items
                                }
                                .testTag("viewMoreButton") // Tag for the "View more" button
                        )
                  }
                }
              }
        }
      }
}
