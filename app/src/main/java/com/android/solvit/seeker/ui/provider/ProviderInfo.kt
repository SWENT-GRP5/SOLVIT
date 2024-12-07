package com.android.solvit.seeker.ui.provider

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.Review
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route

@Composable
fun ProviderInfoScreen(
    navigationActions: NavigationActions,
    providerViewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    reviewsViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory),
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  val provider = providerViewModel.selectedProvider.collectAsState().value ?: return
  val reviews =
      reviewsViewModel.reviews.collectAsState().value.filter { it.providerId == provider.uid }

  var selectedTab by remember { mutableStateOf(ProviderTab.DETAILS) }
  val selectedPackage = remember { mutableStateOf<PackageProposal?>(null) }
  val showDialog = remember { mutableStateOf(false) }

  val user = authViewModel.user.collectAsState()
  val userId = user.value?.uid ?: "-1"

  // Since We still don't give the possibility to provider to add packages (for the moment we're use
  // a default list of packages for all providers)
  val packages =
      listOf(
          PackageProposal(
              uid = "1",
              title = "Basic Maintenance",
              description = "Ideal for minor repairs and maintenance tasks.",
              price = 49.99,
              bulletPoints =
                  listOf(
                      "Fix leaky faucets", "Unclog drains", "Inspect plumbing for minor issues")),
          PackageProposal(
              uid = "2",
              title = "Standard Service",
              description = "Comprehensive service for common plumbing needs.",
              price = 89.99,
              bulletPoints =
                  listOf(
                      "Repair leaks and clogs",
                      "Replace faucets and fixtures",
                      "Inspect and clear drain pipes")),
          PackageProposal(
              uid = "3",
              title = "Premium Installation",
              description = "For extensive plumbing work, including installations.",
              price = 149.99,
              bulletPoints =
                  listOf(
                      "Install new water heater",
                      "Full pipe installation or replacement",
                      "Advanced leak detection and repair")))

  Scaffold(
      containerColor = colorScheme.surface,
      topBar = { ProviderTopBar(onBackClick = { navigationActions.goBack() }) },
      content = { padding ->
        Column(modifier = Modifier.background(colorScheme.surface).padding(padding)) {
          ProviderHeader(provider)
          ProviderTabs(selectedTab = selectedTab) { newTab -> selectedTab = newTab }
          // Display content based on the selected tab
          when (selectedTab) {
            ProviderTab.DETAILS ->
                ProviderDetails(
                    provider,
                    selectedPackage,
                    reviews,
                    showDialog,
                    requestViewModel,
                    userId,
                    navigationActions)
            ProviderTab.PACKAGES ->
                ProviderPackages(
                    provider,
                    packages,
                    selectedPackage,
                    showDialog,
                    requestViewModel,
                    userId,
                    navigationActions)
            ProviderTab.REVIEWS ->
                ProviderReviews(
                    provider,
                    selectedPackage,
                    reviews,
                    showDialog,
                    requestViewModel,
                    userId,
                    navigationActions)
          }
        }
      },
      bottomBar = { BottomBar(showDialog = showDialog) })
}

@Composable
fun PackageCard(
    packageProposal: PackageProposal,
    isSelected: Boolean,
    modifier: Modifier,
    selectedPackage: MutableState<PackageProposal?> = remember { mutableStateOf(null) }
) {
  Card(
      modifier = modifier.fillMaxHeight(),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = if (!isSelected) colorScheme.surface else colorScheme.secondary,
          )) {
        Column(
            modifier =
                Modifier.padding(20.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .testTag("PackageContent"),
            horizontalAlignment = Alignment.Start) {
              // Price of the Package
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.testTag("price"),
                    text = "$${packageProposal.price}",
                    style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color =
                        if (!isSelected) colorScheme.onPrimaryContainer else colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp)) // Increased space between price and unit
                Text(
                    text = "/hour",
                    style = typography.bodySmall,
                    color =
                        if (!isSelected) colorScheme.onPrimaryContainer else colorScheme.onPrimary)
              }
              // Title of the Package
              Text(
                  text = packageProposal.title,
                  style = typography.titleMedium,
                  color =
                      if (!isSelected) colorScheme.onPrimaryContainer else colorScheme.onPrimary)
              Spacer(
                  modifier =
                      Modifier.height(12.dp)) // Increased space between title and description
              // Description of the Package
              Text(
                  text = packageProposal.description,
                  style = typography.bodyMedium,
                  color = if (!isSelected) colorScheme.onSurface else colorScheme.onPrimary)
              Spacer(
                  modifier =
                      Modifier.height(12.dp)) // Increased space between description and features
              // Important infos about the package
              Column {
                packageProposal.bulletPoints.forEach { feature ->
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier =
                            Modifier.size(18.dp)) // Slightly bigger icon for better visibility
                    Spacer(modifier = Modifier.width(8.dp)) // Increased space between icon and text
                    Text(
                        text = feature,
                        style = typography.bodyMedium,
                        color = if (!isSelected) colorScheme.onSurface else colorScheme.onPrimary)
                  }
                }
              }
              Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom
              Button(
                  enabled = isSelected,
                  onClick = {
                    // Toggle the selected package: if already selected, unselect it
                    selectedPackage.value =
                        if (selectedPackage.value == packageProposal) null else packageProposal
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                  modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    // Update the button text based on the selection state
                    Text(
                        text =
                            if (selectedPackage.value == packageProposal) "Unselect package"
                            else "Choose package")
                  }
            }
      }
}

@Composable
fun ProviderPackages(
    provider: Provider,
    packages: List<PackageProposal>,
    selectedPackage: MutableState<PackageProposal?>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel,
    userId: String,
    navigationActions: NavigationActions
) {
  var selectedIndex by remember { mutableIntStateOf(-1) }
  Box(
      modifier = Modifier.fillMaxSize(), // Fills the entire available space
      contentAlignment = Alignment.Center // Centers the LazyRow within the Box
      ) {
        // Horizontal scrollable list
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("packagesScrollableList"),
            horizontalArrangement = Arrangement.spacedBy(20.dp), // Adjusted for spacing
            contentPadding =
                PaddingValues(top = 40.dp, start = 12.dp, end = 12.dp), // Increased padding
        ) {
          items(packages.size) { index ->
            // If package is selected, we display it bigger
            val isSelected = selectedIndex == index
            val size by
                animateDpAsState(
                    targetValue = if (isSelected) 350.dp else 320.dp, label = "PackageCardSize")
            PackageCard(
                packageProposal = packages[index],
                isSelected = isSelected,
                modifier =
                    Modifier.width(260.dp) // Slightly wider for better touch targets
                        .height(size)
                        .clickable { selectedIndex = if (isSelected) -1 else index }
                        .testTag("PackageCard"),
                selectedPackage = selectedPackage)
          }
        }
        if (showDialog.value) {
          SelectRequestDialog(
              provider.uid,
              provider.service,
              selectedPackage,
              showDialog,
              requestViewModel,
              userId,
              navigationActions)
        }
      }
}

@Composable
fun ProviderTopBar(onBackClick: () -> Unit) {
  val context = LocalContext.current
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(color = colorScheme.background)
              .testTag("ProviderTopBar"),
      verticalAlignment = Alignment.CenterVertically) {
        // Back button on the left
        IconButton(onClick = onBackClick, modifier = Modifier.testTag("backButton")) {
          Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = colorScheme.onBackground)
        }

        // Title in the center
        Text(
            text = "Provider",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).testTag("topBarTitle"),
            textAlign = TextAlign.Start,
            color = colorScheme.onBackground)

        // Menu icon on the right
        IconButton(
            onClick = { Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.testTag("menuButton")) {
              Icon(
                  painter = painterResource(id = R.drawable.menu_icon),
                  contentDescription = "Menu",
                  modifier = Modifier.size(24.dp),
                  tint = colorScheme.onBackground)
            }
      }
}

@Composable
fun ProviderHeader(provider: Provider) {
  Box(
      modifier =
          Modifier.fillMaxWidth().background(colorScheme.background).testTag("providerHeader")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              val context = LocalContext.current
              Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model =
                        if (provider.imageUrl != "") provider.imageUrl else R.drawable.default_pdp,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier.size(128.dp)
                            .border(2.dp, Color.Transparent, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("providerImage"))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                  Text(
                      text = provider.name,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.testTag("providerName"),
                      color = colorScheme.onBackground)
                  Text(
                      text = provider.companyName,
                      color = colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag("providerCompanyName"))
                }
              }

              // Share icon on the right
              IconButton(
                  onClick = {
                    Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("shareButton")) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = colorScheme.onBackground)
                  }
            }
      }
}

@Composable
fun ProviderTabs(selectedTab: ProviderTab, onTabSelected: (ProviderTab) -> Unit) {
  TabRow(
      selectedTabIndex = selectedTab.ordinal,
      modifier = Modifier.fillMaxWidth().testTag("providerTabs"),
      containerColor = colorScheme.primary,
      contentColor = colorScheme.onPrimary,
  ) {
    ProviderTab.entries.forEach { tab ->
      Tab(
          modifier = Modifier.testTag(tab.name.lowercase() + "Tab"),
          text = {
            Text(
                text = tab.title,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color =
                    if (selectedTab == tab) colorScheme.onPrimary
                    else colorScheme.onPrimary.copy(alpha = 0.6f),
                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal)
          },
          selected = selectedTab == tab,
          onClick = { onTabSelected(tab) })
    }
  }
}

enum class ProviderTab(val title: String) {
  DETAILS("Details"),
  PACKAGES("Packages"),
  REVIEWS("Reviews")
}

@Composable
fun ProviderDetails(
    provider: Provider,
    selectedPackage: MutableState<PackageProposal?>,
    reviews: List<Review>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel,
    userId: String,
    navigationActions: NavigationActions
) {
  Column(
      modifier =
          Modifier.padding(16.dp)
              .fillMaxWidth()
              .background(color = colorScheme.surface, shape = RoundedCornerShape(16.dp))
              .testTag("providerDetails")) {
        Rubric(modifier = Modifier.testTag("detailsSection")) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {
                RatingStars(provider.rating.toInt())
                Text(
                    text = "${reviews.size} Reviews",
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("reviewsCount"))
                Text(
                    text = "15 Jobs", // Replace with actual job count
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("jobsCount"))
              }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
              "Refrigerator repair",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = colorScheme.onBackground)
          Text(
              text = "CHF ${provider.price}/hour",
              fontSize = 16.sp,
              color = colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 4.dp).testTag("priceDisplay"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Descriptions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("descriptionTitle"),
            color = colorScheme.onBackground)

        Spacer(modifier = Modifier.height(8.dp))

        Rubric(modifier = Modifier.testTag("descriptionSection")) {
          Text(
              text = provider.description,
              modifier = Modifier.padding(vertical = 4.dp).testTag("descriptionText"),
              color = colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Contact",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("contactTitle"),
            color = colorScheme.onBackground)

        Spacer(modifier = Modifier.height(8.dp))

        Rubric(modifier = Modifier.testTag("contactSection")) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.message),
                    contentDescription = "Message",
                    modifier = Modifier.size(64.dp).testTag("messageIcon"),
                    tint = Color.Unspecified)
                Text(
                    text =
                        "The contractor's contacts are visible only to its customers. If you are interested in the services of this contractor - offer him an order.",
                    modifier = Modifier.padding(vertical = 4.dp).testTag("contactText"),
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
              }
        }
        if (showDialog.value) {
          SelectRequestDialog(
              provider.uid,
              provider.service,
              selectedPackage,
              showDialog,
              requestViewModel,
              userId,
              navigationActions)
        }
      }
}

@Composable
fun Rubric(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .background(color = colorScheme.background, shape = RoundedCornerShape(16.dp))
              .border(width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
              .padding(16.dp)) {
        content()
      }
}

@Composable
fun ProviderReviews(
    provider: Provider,
    selectedPackage: MutableState<PackageProposal?>,
    reviews: List<Review>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel,
    userId: String,
    navigationActions: NavigationActions
) {
  Column(
      modifier =
          Modifier.padding(16.dp)
              .fillMaxWidth()
              .background(color = colorScheme.surface, shape = RoundedCornerShape(16.dp))
              .testTag("providerReviews")) {
        Column(
            Modifier.fillMaxWidth()
                .background(color = colorScheme.background, shape = RoundedCornerShape(16.dp))
                .border(width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("reviewsOverview")) {
              Text(
                  "Overall",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("overallTitle"))

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth().testTag("overallRating")) {
                    Text(
                        provider.rating.toString(),
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground)
                    RatingStars(provider.rating.toInt())
                  }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                  "${reviews.size} " + if (reviews.size > 1) "Reviews" else "Review",
                  modifier = Modifier.padding(vertical = 4.dp),
                  color = colorScheme.onSurfaceVariant)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Reviews",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("reviewsTitle"),
            color = colorScheme.onBackground)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
          if (reviews.isEmpty()) {
            item {
              Text(
                  "No reviews yet",
                  modifier = Modifier.padding(16.dp),
                  color = colorScheme.onSurfaceVariant)
            }
          }
          items(reviews) { review -> ReviewRow(review) }
        }

        if (showDialog.value) {
          SelectRequestDialog(
              provider.uid,
              provider.service,
              selectedPackage,
              showDialog,
              requestViewModel,
              userId,
              navigationActions)
        }
      }
}

@Composable
fun ReviewRow(review: Review) {
  Column(
      Modifier.fillMaxWidth()
          .padding(top = 16.dp)
          .background(color = colorScheme.background, shape = RoundedCornerShape(16.dp))
          .border(width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
          .padding(4.dp)
          .testTag("reviewRow")) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Image(
              painter = painterResource(id = R.drawable.default_pdp),
              contentDescription = "Profile Picture",
              modifier =
                  Modifier.size(64.dp)
                      .border(2.dp, Color.Transparent, RoundedCornerShape(16.dp))
                      .clip(RoundedCornerShape(16.dp))
                      .testTag("reviewerImage"))

          RatingStars(review.rating)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            review.comment,
            modifier = Modifier.padding(vertical = 4.dp).testTag("reviewComment"),
            color = colorScheme.onSurfaceVariant)
      }
}

@Composable
fun RatingStars(rating: Int) {
  Row(
      modifier = Modifier.testTag("ratingStars"),
  ) {
    repeat(5) { index ->
      Icon(
          painter = painterResource(id = R.drawable.star),
          modifier = Modifier.size(24.dp),
          contentDescription = "Rating Star",
          tint = if (index < rating) colorScheme.primary else colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun BottomBar(showDialog: MutableState<Boolean>) {
  Row(
      modifier = Modifier.background(colorScheme.surface).fillMaxWidth().testTag("bottomBar"),
      horizontalArrangement = Arrangement.Center) {
        Button(
            modifier =
                Modifier.padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.primary)
                    .size(200.dp, 50.dp)
                    .testTag("bookNowButton"),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            onClick = { showDialog.value = true }) {
              Text(
                  "Book Now",
                  color = colorScheme.onPrimary,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold)
            }
      }
}

@Composable
fun SelectRequestDialog(
    providerId: String,
    providerType: Services,
    selectedPackage: MutableState<PackageProposal?>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel,
    userId: String,
    navigationActions: NavigationActions
) {
  val requests =
      requestViewModel.pendingRequests.collectAsState().value.filter {
        it.userId == userId && it.type == providerType
      }

  val selectedRequest = remember { mutableStateOf<ServiceRequest?>(null) }

  Dialog(onDismissRequest = { showDialog.value = false }) {
    Card(
        modifier =
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                .height(400.dp)
                .testTag("dialog_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)) {
          Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Title
                Text(
                    text = "Choose the concerned service request:",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp).testTag("dialog_title"),
                    color = colorScheme.onSurface)

                // LazyColumn to display requests
                LazyColumn(
                    modifier =
                        Modifier.testTag("requests_column")
                            .fillMaxWidth()
                            .weight(
                                1f), // Allows the list to scroll properly when content overflows
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                      items(requests) { request ->
                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag("request_card")
                                    .border(
                                        2.dp,
                                        if (selectedRequest.value == request) colorScheme.primary
                                        else colorScheme.surface,
                                        RoundedCornerShape(12.dp))
                                    .clickable { selectedRequest.value = request },
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = colorScheme.primaryContainer)) {
                              Column(
                                  modifier = Modifier.padding(12.dp),
                                  verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                          Text(
                                              text = "Title:",
                                              style = typography.bodyMedium,
                                              color = colorScheme.onPrimaryContainer)
                                          Text(
                                              modifier = Modifier.testTag("request_title"),
                                              text = request.title,
                                              style = typography.bodyMedium,
                                              color = colorScheme.onPrimaryContainer)
                                        }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                          Text(
                                              text = "Description:",
                                              style = typography.bodyMedium,
                                              color = colorScheme.onPrimaryContainer)
                                          Text(
                                              modifier = Modifier.testTag("request_description"),
                                              text = request.description,
                                              style = typography.bodyMedium,
                                              color = colorScheme.onPrimaryContainer,
                                              maxLines = 2,
                                              overflow = TextOverflow.Ellipsis)
                                        }
                                  }
                            }
                      }
                    }

                TextButton(
                    onClick = {
                      requestViewModel.selectProvider(providerId, providerType)
                      navigationActions.navigateTo(Route.CREATE_REQUEST)
                    },
                    modifier = Modifier.testTag("clear_selection_button")) {
                      Text(
                          textAlign = TextAlign.Center,
                          text = "Create a new request with this provider assigned",
                          style = typography.bodyMedium,
                          color = colorScheme.primary)
                    }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                      TextButton(
                          onClick = { showDialog.value = false },
                          modifier =
                              Modifier.padding(horizontal = 8.dp).testTag("dismiss_button")) {
                            Text(
                                text = "Dismiss",
                                style = typography.labelLarge,
                                color = colorScheme.primary)
                          }

                      Button(
                          modifier = Modifier.padding(horizontal = 8.dp).testTag("confirm_button"),
                          enabled = selectedRequest.value != null,
                          onClick = {
                            selectedRequest.value?.let {
                              var request = it.copy(providerId = providerId)
                              val packageProposal = selectedPackage.value
                              if (packageProposal != null) {
                                request =
                                    request.copy(
                                        packageId = packageProposal.uid,
                                        agreedPrice = packageProposal.price)
                              }
                              requestViewModel.saveServiceRequest(request)
                              requestViewModel.selectRequest(request)
                              navigationActions.navigateTo(Route.BOOKING_DETAILS)
                            }
                            showDialog.value = false
                          },
                          shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "Confirm",
                                style = typography.labelLarge,
                                color = colorScheme.onPrimary)
                          }
                    }
              }
        }
  }
}
