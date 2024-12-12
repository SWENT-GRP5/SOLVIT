package com.android.solvit.provider.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.provider.model.profile.ProviderViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.seeker.ui.profile.AboutAppCard
import com.android.solvit.seeker.ui.profile.LogoutDialog
import com.android.solvit.seeker.ui.profile.ProfileInfoCard
import com.android.solvit.seeker.ui.profile.ProfileTopBar
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestStatus.Companion.getStatusColor
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

/**
 * A composable function that displays the provider's profile screen. It includes the profile header
 * and the statistics section. It also ensures that the provider's data is updated from Firebase
 * each time the screen is launched.
 *
 * @param providerViewModel The ViewModel managing the provider profile.
 * @param authViewModel The ViewModel managing authentication.
 * @param navigationActions Actions for navigating between screens.
 */
@SuppressLint("SourceLockedOrientationActivity", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProviderProfileScreen(
    providerViewModel: ProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    serviceRequestViewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  val user by authViewModel.user.collectAsState()
  val provider by providerViewModel.userProvider.collectAsState()
  LaunchedEffect(user) { user?.uid?.let { providerViewModel.getProvider(it) } }

  // State for logout dialog
  var showLogoutDialog by remember { mutableStateOf(false) }

  // Display the profile information if it's available
  Scaffold(
      topBar = { ProfileTopBar(navigationActions, onLogout = { showLogoutDialog = true }) },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = Screen.PROVIDER_PROFILE)
      },
      containerColor = Color.Transparent,
  ) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("ProfileContent"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

          // profile info card
          ProfileInfoCard(
              fullName = provider?.name ?: "",
              email = provider?.companyName ?: "",
              imageUrl = provider?.imageUrl ?: "",
              onEdit = { navigationActions.navigateTo(Screen.PROVIDER_MODIFY_PROFILE) })

          // additional infos cards
          Card(
              modifier = Modifier.fillMaxWidth().testTag("AdditionalInfosCard"),
              colors = CardDefaults.cardColors(containerColor = colorScheme.background),
              elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)) {
                      ProviderItem(
                          icon = Icons.Default.Build,
                          text = if (provider == null) "" else Services.format(provider!!.service),
                          tag = "ServiceItem")
                      ProviderItem(
                          icon = Icons.Default.LocationOn,
                          text = provider?.location?.name ?: "",
                          tag = "LocationItem")
                      ProviderItem(
                          icon = Icons.Default.Info,
                          text = provider?.description ?: "",
                          tag = "DescriptionItem",
                          maxLines = 3)
                      ProviderItem(
                          icon = Icons.Default.ThumbUp,
                          drawable = R.drawable.language_icon,
                          text =
                              provider?.languages?.joinToString { lng ->
                                lng.name.lowercase().replaceFirstChar { it.uppercase() }
                              } ?: "",
                          tag = "LanguagesItem")
                    }
              }

          // insights card
          provider?.let { InsightsCard(it, serviceRequestViewModel) }

          // about app & support
          AboutAppCard(context)

          Spacer(modifier = Modifier.height(16.dp))
        }
  }

  // Logout Confirmation Dialog
  if (showLogoutDialog) {
    LogoutDialog(
        onLogout = {
          authViewModel.logout {}
          showLogoutDialog = false
        },
        onDismiss = { showLogoutDialog = false })
  }
}

/**
 * A composable function that displays an item with an icon and text.
 *
 * @param icon The icon to display.
 * @param drawable The drawable to display.
 * @param text The text to display.
 * @param tag The tag for the item.
 * @param maxLines The maximum number of lines for the text.
 */
@Composable
fun ProviderItem(
    icon: ImageVector,
    drawable: Int? = null,
    text: String,
    tag: String = "",
    maxLines: Int = 1
) {
  var expanded by remember { mutableStateOf(false) }
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(tag)) {
        if (drawable != null) {
          Icon(
              painter = painterResource(drawable),
              contentDescription = text,
              tint = colorScheme.primary,
              modifier = Modifier.size(32.dp))
        } else {
          Icon(
              imageVector = icon,
              contentDescription = text,
              tint = colorScheme.primary,
              modifier = Modifier.size(32.dp))
        }
        Text(
            text = text,
            color = colorScheme.onBackground,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { expanded = !expanded })
      }
}

/**
 * A composable function that displays the insights card for the provider. It includes the
 * provider's rating, popularity, earnings, and the number of tasks in each status.
 *
 * @param provider The provider whose insights are displayed.
 * @param serviceRequestViewModel The ViewModel managing service requests.
 */
@Composable
fun InsightsCard(provider: Provider, serviceRequestViewModel: ServiceRequestViewModel) {
  val pendingTasks by serviceRequestViewModel.pendingRequests.collectAsState()
  val filteredPendingTasks = pendingTasks.filter { it.providerId == provider.uid }
  val acceptedTasks by serviceRequestViewModel.acceptedRequests.collectAsState()
  val filteredAcceptedTasks = acceptedTasks.filter { it.providerId == provider.uid }
  val scheduledTasks by serviceRequestViewModel.scheduledRequests.collectAsState()
  val filteredScheduledTasks = scheduledTasks.filter { it.providerId == provider.uid }
  val completedTasks by serviceRequestViewModel.completedRequests.collectAsState()
  val filteredCompletedTasks = completedTasks.filter { it.providerId == provider.uid }
  val canceledTasks by serviceRequestViewModel.cancelledRequests.collectAsState()
  val filteredCanceledTasks = canceledTasks.filter { it.providerId == provider.uid }
  val archivedTasks by serviceRequestViewModel.archivedRequests.collectAsState()
  val filteredArchivedTasks = archivedTasks.filter { it.providerId == provider.uid }

  val earnings = filteredCompletedTasks.sumOf { it.agreedPrice ?: 0.0 }

  Card(
      modifier = Modifier.fillMaxWidth().testTag("InsightsCard"),
      colors = CardDefaults.cardColors(containerColor = colorScheme.background),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
              Text(
                  text = "Insights",
                  color = colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 24.sp,
                  modifier = Modifier.testTag("InsightsTitle"))
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                              modifier = Modifier.testTag("RatingItem")) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(32.dp))
                                Text(
                                    text = "${provider.rating}",
                                    color = colorScheme.onBackground,
                                )
                              }
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                              modifier = Modifier.testTag("PopularityItem")) {
                                Text(
                                    text = "Popular",
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp)
                                Text(text = "${provider.popular}", color = colorScheme.onBackground)
                              }
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                              modifier = Modifier.testTag("EarningsItem")) {
                                Icon(
                                    painter = painterResource(R.drawable.money_icon),
                                    contentDescription = "Earnings",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(32.dp))
                                Text(text = "$earnings CHF", color = colorScheme.onBackground)
                              }
                        }
                    Column(
                        modifier = Modifier.fillMaxWidth().testTag("TasksColumn"),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                          Text(
                              text = "Jobs",
                              color = colorScheme.primary,
                              fontWeight = FontWeight.Bold,
                              fontSize = 18.sp)
                          Column {
                            for (status in ServiceRequestStatus.entries) {
                              val size =
                                  when (status) {
                                    ServiceRequestStatus.PENDING -> filteredPendingTasks.size
                                    ServiceRequestStatus.ACCEPTED -> filteredAcceptedTasks.size
                                    ServiceRequestStatus.SCHEDULED -> filteredScheduledTasks.size
                                    ServiceRequestStatus.COMPLETED -> filteredCompletedTasks.size
                                    ServiceRequestStatus.CANCELED -> filteredCanceledTasks.size
                                    ServiceRequestStatus.ARCHIVED -> filteredArchivedTasks.size
                                  }
                              Text(
                                  text = "$size ${status.name.lowercase()}",
                                  color = getStatusColor(status),
                                  modifier = Modifier.testTag("${status.name}Tasks"))
                            }
                          }
                        }
                  }
            }
      }
}
