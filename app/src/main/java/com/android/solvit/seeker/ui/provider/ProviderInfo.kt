package com.android.solvit.seeker.ui.provider

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.Review
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.SecondaryPackage
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox

/**
 * Main screen to display detailed information about a provider.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param providerViewModel ViewModel for provider data.
 * @param reviewsViewModel ViewModel for reviews data.
 * @param requestViewModel ViewModel for service requests.
 * @param authViewModel ViewModel for user authentication.
 * @param packageProposalViewModel ViewModel for package proposals.
 */
@Composable
fun ProviderInfoScreen(
    navigationActions: NavigationActions,
    providerViewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    reviewsViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory),
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    packageProposalViewModel: PackageProposalViewModel =
        viewModel(factory = PackageProposalViewModel.Factory)
) {
  val provider = providerViewModel.selectedProvider.collectAsState().value ?: return
  val reviews =
      reviewsViewModel.reviews.collectAsState().value.filter { it.providerId == provider.uid }

  var selectedTab by remember { mutableStateOf(ProviderTab.DETAILS) }
  val selectedPackage = remember { mutableStateOf<PackageProposal?>(null) }
  val showDialog = remember { mutableStateOf(false) }

  val packagesProposal by packageProposalViewModel.proposal.collectAsState()
  val user = authViewModel.user.collectAsState()
  val userId = user.value?.uid ?: "-1"

  val packages = packagesProposal.filter { it.providerId == provider.uid }

  Scaffold(
      containerColor = colorScheme.surface,
      topBar = {
        TopAppBarInbox(
            title = "Provider",
            testTagTitle = "topBarTitle",
            leftButtonAction = { navigationActions.goBack() },
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack,
            testTagLeft = "backButton",
            testTagGeneral = "ProviderTopBar")
      },
      content = { padding ->
        Column(modifier = Modifier.background(colorScheme.surface).fillMaxSize().padding(padding)) {
          ProviderHeader(provider)
          ProviderTabs(
              selectedTab = selectedTab,
              displayPackages = packages.isNotEmpty(),
              onTabSelected = { newTab -> selectedTab = newTab })
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
                    navigationActions,
                )
          }
        }
      },
      bottomBar = { BottomBar(showDialog = showDialog) })
}

/**
 * Card to display package details, including price, description, and features.
 *
 * @param packageProposal The package proposal to display.
 * @param selectedIndex Whether this package is currently selected.
 * @param onIsSelectedChange Callback for when the selection state changes.
 * @param modifier Modifier to style the card.
 * @param selectedPackage Mutable state for the selected package.
 */
@Composable
fun PackageCard(
    packageProposal: PackageProposal,
    selectedIndex: Boolean,
    onIsSelectedChange: () -> Unit,
    modifier: Modifier,
    selectedPackage: MutableState<PackageProposal?> = remember { mutableStateOf(null) },
) {
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  val dynamicBottomPadding = screenHeight * 0.1f

  Card(
      modifier = modifier,
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = if (!selectedIndex) colorScheme.surface else SecondaryPackage,
          )) {
        Box(modifier = Modifier.fillMaxSize()) {
          Column(
              modifier =
                  Modifier.padding(
                          start = 20.dp, end = 20.dp, top = 20.dp, bottom = dynamicBottomPadding)
                      .fillMaxHeight()
                      .verticalScroll(rememberScrollState())
                      .testTag("PackageContent"),
              horizontalAlignment = Alignment.Start) {
                // Price of the Package
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      modifier = Modifier.testTag("price"),
                      text = "CHF${packageProposal.price}",
                      style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                      color =
                          if (!selectedIndex) colorScheme.onPrimaryContainer
                          else colorScheme.onPrimary)
                  Spacer(modifier = Modifier.width(8.dp)) // Increased space between price and unit
                  Text(
                      text = "/hour",
                      style = Typography.bodySmall,
                      color =
                          if (!selectedIndex) colorScheme.onPrimaryContainer
                          else colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Title of the Package
                Text(
                    text = packageProposal.title,
                    style = Typography.titleMedium,
                    color =
                        if (!selectedIndex) colorScheme.onPrimaryContainer
                        else colorScheme.onPrimary)
                Spacer(
                    modifier =
                        Modifier.height(12.dp)) // Increased space between title and description
                // Description of the Package
                Text(
                    text = packageProposal.description,
                    style = Typography.bodyMedium,
                    color = if (!selectedIndex) colorScheme.onSurface else colorScheme.onPrimary)
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
                      Spacer(
                          modifier = Modifier.width(8.dp)) // Increased space between icon and text
                      Text(
                          text = feature,
                          style = Typography.bodyMedium,
                          color =
                              if (!selectedIndex) colorScheme.onSurface else colorScheme.onPrimary)
                    }
                  }
                }
                Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom
          }
          Button(
              onClick = {
                // Toggle the selected package: if already selected, unselect it
                selectedPackage.value =
                    if (selectedPackage.value == packageProposal) null else packageProposal
                onIsSelectedChange()
              },
              colors =
                  if (!selectedIndex)
                      ButtonDefaults.buttonColors(
                          containerColor = colorScheme.primary.copy(alpha = 0.6f))
                  else ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                // Update the button text based on the selection state
                Text(
                    text =
                        if (selectedPackage.value == packageProposal) "Unselect package"
                        else "Select package",
                )
              }
        }
      }
}
/**
 * Screen displaying the list of packages offered by the provider.
 *
 * @param provider The provider object.
 * @param packages List of packages proposed by the provider.
 * @param selectedPackage Mutable state for the selected package.
 * @param showDialog Mutable state controlling the display of the dialog.
 * @param requestViewModel ViewModel for handling service requests.
 * @param userId Current user's ID.
 * @param navigationActions Navigation actions for navigating to different screens.
 */
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
  var boxHeightPx by remember { mutableIntStateOf(0) }
  Box(
      modifier =
          Modifier.fillMaxSize() // Fills the entire available space
              .onSizeChanged { size ->
                boxHeightPx = size.height // Get height of box
              },
      contentAlignment = Alignment.Center // Centers the LazyRow within the Box
      ) {
        // Horizontal scrollable list
        LazyRow(
            modifier =
                Modifier.fillMaxSize().testTag("packagesScrollableList").align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(20.dp), // Adjusted for spacing
            contentPadding =
                PaddingValues(top = 40.dp, start = 12.dp, end = 12.dp), // Increased padding
        ) {
          items(packages.size) { index ->
            // If package is selected, we display it bigger
            val size = if (selectedIndex == index) boxHeightPx * 1f else boxHeightPx * 0.3f

            // we calculate the height difference that we then divide by 2
            val offset = if (selectedIndex == index) (-(size * 0.025f)).dp else 0.dp

            PackageCard(
                packageProposal = packages[index],
                selectedIndex = (selectedIndex == index),
                modifier =
                    Modifier.width(260.dp)
                        .height(size.dp) // Slightly wider for better touch targets
                        .testTag("PackageCard")
                        .offset(y = offset)
                        .shadow(
                            if (selectedIndex == index) 16.dp else 4.dp, RoundedCornerShape(16.dp)),
                selectedPackage = selectedPackage,
                onIsSelectedChange = { selectedIndex = if (selectedIndex == index) -1 else index },
            )
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

/**
 * Header section displaying provider details such as name and service type.
 *
 * @param provider The provider object containing details.
 */
@Composable
fun ProviderHeader(provider: Provider) {
  Box(
      modifier =
          Modifier.fillMaxWidth().background(colorScheme.background).testTag("providerHeader")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
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
                      text = Services.format(provider.service),
                      color = colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag("providerService"))
                }
              }
            }
      }
}

/**
 * Tab navigation for provider details, packages, and reviews.
 *
 * @param selectedTab Currently selected tab.
 * @param onTabSelected Callback when a tab is selected.
 * @param displayPackages Whether the "Packages" tab should be displayed.
 */
@Composable
fun ProviderTabs(
    selectedTab: ProviderTab,
    onTabSelected: (ProviderTab) -> Unit,
    displayPackages: Boolean
) {
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val dynamicFontSize = (screenWidth.value * 0.03).sp
  TabRow(
      selectedTabIndex = selectedTab.ordinal,
      modifier = Modifier.fillMaxWidth().testTag("providerTabs"),
      containerColor = colorScheme.primary,
      contentColor = colorScheme.onPrimary,
  ) {
    ProviderTab.entries
        .filter { if (!displayPackages) it != ProviderTab.PACKAGES else true }
        .forEach { tab ->
          Tab(
              modifier = Modifier.testTag(tab.name.lowercase() + "Tab"),
              text = {
                Text(
                    text = tab.title,
                    style = Typography.titleMedium.copy(fontSize = dynamicFontSize),
                    maxLines = 1,
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

/**
 * Screen displaying provider details such as rating, description, and contact information.
 *
 * @param provider The provider object.
 * @param selectedPackage Currently selected package.
 * @param reviews List of reviews for the provider.
 * @param showDialog Mutable state controlling the display of the dialog.
 * @param requestViewModel ViewModel for handling service requests.
 * @param userId Current user's ID.
 * @param navigationActions Navigation actions for navigating to different screens.
 */
@Composable
fun ProviderDetails(
    provider: Provider,
    selectedPackage: MutableState<PackageProposal?>,
    reviews: List<Review>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel,
    userId: String,
    navigationActions: NavigationActions,
) {
  val nbrOfJobs = provider.nbrOfJobs.toInt()
  Column(
      modifier =
          Modifier.padding(16.dp)
              .fillMaxWidth()
              .background(color = colorScheme.surface, shape = RoundedCornerShape(16.dp))
              .testTag("providerDetails")
              .verticalScroll(rememberScrollState())) {
        Rubric(modifier = Modifier.testTag("detailsSection")) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {
                RatingStars(provider.rating.toInt())
                Text(
                    text = if (reviews.size > 100) "+100 Reviews" else "${reviews.size} Reviews",
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("reviewsCount"))
                Text(
                    text =
                        if (nbrOfJobs <= 1) "$nbrOfJobs Job"
                        else if (nbrOfJobs <= 100) "$nbrOfJobs Jobs" else "+100 Jobs",
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("jobsCount"))
              }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
              provider.companyName,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = colorScheme.onBackground,
              modifier = Modifier.testTag("providerCompanyName"))
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

/**
 * Reusable component to display a rounded background section with customizable content.
 *
 * @param modifier Modifier to style the component.
 * @param content Composable content to display inside the rubric.
 */
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

/**
 * Screen displaying reviews for the provider.
 *
 * @param provider The provider object.
 * @param selectedPackage Currently selected package.
 * @param reviews List of reviews for the provider.
 * @param showDialog Mutable state controlling the display of the dialog.
 * @param requestViewModel ViewModel for handling service requests.
 * @param userId Current user's ID.
 * @param navigationActions Navigation actions for navigating to different screens.
 */
@Composable
fun ProviderReviews(
    provider: Provider,
    selectedPackage: MutableState<PackageProposal?>,
    reviews: List<Review>,
    showDialog: MutableState<Boolean>,
    requestViewModel: ServiceRequestViewModel,
    userId: String,
    navigationActions: NavigationActions,
) {
  Column(
      modifier =
          Modifier.padding(16.dp)
              .fillMaxWidth()
              .background(color = colorScheme.surface, shape = RoundedCornerShape(16.dp))
              .testTag("providerReviews")
              .verticalScroll(rememberScrollState())) {
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
        if (reviews.isEmpty()) {
          Text(
              "No reviews yet",
              modifier = Modifier.padding(16.dp),
              color = colorScheme.onSurfaceVariant)
        } else {
          reviews.forEach { ReviewRow(it) }
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

/**
 * Component to display a single review, including rating and comments.
 *
 * @param review The review object to display.
 */
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

/**
 * Component to display a star rating.
 *
 * @param rating The rating value (out of 5).
 */
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

/**
 * Bottom bar with a "Book Now" button.
 *
 * @param showDialog Mutable state controlling the display of the booking dialog.
 */
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

/**
 * Dialog to select a service request or create a new one.
 *
 * @param providerId ID of the provider.
 * @param providerType Type of service provided.
 * @param selectedPackage Currently selected package.
 * @param showDialog Mutable state controlling the display of the dialog.
 * @param requestViewModel ViewModel for handling service requests.
 * @param userId Current user's ID.
 * @param navigationActions Navigation actions for navigating to different screens.
 */
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
    Surface(shape = RoundedCornerShape(16.dp), color = colorScheme.background) {
      Card(
          modifier =
              Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                  .height(400.dp)
                  .testTag("dialog_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = colorScheme.background)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {
                  // Title
                  Text(
                      text = "Select request to assign",
                      style = Typography.titleLarge,
                      modifier =
                          Modifier.padding(bottom = 16.dp)
                              .testTag("dialog_title")
                              .align(Alignment.CenterHorizontally),
                      color = colorScheme.onPrimaryContainer)

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
                                          if (selectedRequest.value == request)
                                              colorScheme.secondary
                                          else colorScheme.background,
                                          RoundedCornerShape(12.dp))
                                      .shadow(elevation = 5.dp, shape = RoundedCornerShape(12.dp))
                                      .clickable { selectedRequest.value = request },
                              shape = RoundedCornerShape(12.dp),
                              colors =
                                  CardDefaults.cardColors(
                                      containerColor = colorScheme.background)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                      Box(
                                          modifier =
                                              Modifier.size(48.dp)
                                                  .clip(RoundedCornerShape(8.dp))
                                                  .background(Color.LightGray)) {
                                            AsyncImage(
                                                model =
                                                    if (!request.imageUrl.isNullOrEmpty())
                                                        request.imageUrl
                                                    else R.drawable.no_photo,
                                                placeholder =
                                                    painterResource(id = R.drawable.loading),
                                                error = painterResource(id = R.drawable.error),
                                                contentDescription = "Service Image",
                                                contentScale = ContentScale.Fit)
                                          }

                                      Spacer(Modifier.width(16.dp))
                                      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            modifier = Modifier.testTag("request_title"),
                                            text = request.title,
                                            style = Typography.titleMedium,
                                            color = colorScheme.onPrimaryContainer)
                                        Text(
                                            modifier = Modifier.testTag("request_description"),
                                            text = request.description,
                                            style = Typography.bodyMedium,
                                            color = colorScheme.onPrimaryContainer,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis)
                                      }
                                    }
                              }
                        }
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
                                  style = Typography.labelLarge,
                                  color = colorScheme.primary)
                            }

                        Button(
                            modifier =
                                Modifier.padding(horizontal = 8.dp).testTag("confirm_button"),
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
                                navigationActions.navigateAndSetBackStack(
                                    Route.BOOKING_DETAILS, listOf(Route.REQUESTS_OVERVIEW))
                              }
                              showDialog.value = false
                            },
                            shape = RoundedCornerShape(8.dp)) {
                              Text(
                                  text = "Confirm",
                                  style = Typography.labelLarge,
                                  color = colorScheme.onPrimary)
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
                            textDecoration = TextDecoration.Underline,
                            text = "Create a new request with this provider assigned",
                            style = Typography.bodyMedium,
                            color = colorScheme.primary)
                      }
                }
          }
    }
  }
}
