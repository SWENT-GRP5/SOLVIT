package com.android.solvit.seeker.ui.provider

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.provider.PackageProposal
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.review.Review
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun ProviderInfoScreen(
    navigationActions: NavigationActions,
    providerViewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    reviewsViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory)
) {
  val provider = providerViewModel.selectedProvider.collectAsState().value ?: return
  val reviews =
      reviewsViewModel.reviews.collectAsState().value.filter { it.providerId == provider.uid }

  var selectedTabIndex by remember { mutableIntStateOf(0) }

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
          ProviderTabs(selectedTabIndex) { newIndex -> selectedTabIndex = newIndex }

          // Display content based on the selected tab
          when (selectedTabIndex) {
            0 ->
                ProviderDetails(
                    provider, reviews) // Display ProviderDetails if "Profile" tab is selected
            1 -> ProviderPackages(provider, packages) // Display packages proposals of provider
            2 ->
                ProviderReviews(
                    provider, reviews) // Display ProviderReviews if "Reviews" tab is selected
          }
        }
      },
      bottomBar = { BottomBar() })
}

@Composable
fun PackageCard(packageProposal: PackageProposal, isSelected: Boolean, modifier: Modifier) {
  val context = LocalContext.current
  Card(
      modifier = modifier.fillMaxHeight(),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (!isSelected) MaterialTheme.colorScheme.surface else Color(0xFF1C1651),
          )) {
        Column(
            modifier = Modifier.padding(25.dp).fillMaxHeight().testTag("PackageContent"),
            horizontalAlignment = Alignment.Start) {
              // Price of the Package
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.testTag("price"),
                    text = "$${packageProposal.price}",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (!isSelected) Color(0xFF231D4F) else Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/hour",
                    style = MaterialTheme.typography.bodySmall, // Smaller style for the unit
                    color = if (!isSelected) Color(0xFF231D4F) else Color.White)
              }
              // Title of the Package
              Text(
                  text = packageProposal.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = if (!isSelected) Color(0xFF231D4F) else Color.White)
              Spacer(modifier = Modifier.height(8.dp))
              // Description of the Package
              Text(
                  text = packageProposal.description,
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (!isSelected) MaterialTheme.colorScheme.onSurface else Color.White)
              Spacer(modifier = Modifier.height(8.dp))
              // Important infos about the package
              Column {
                packageProposal.bulletPoints.forEach { feature ->
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (!isSelected) MaterialTheme.colorScheme.onSurface else Color.White)
                  }
                }
              }
              Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom
              Button(
                  onClick = {
                    Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
                  },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor =
                              if (isSelected) Color(0xFFBB6BD9) else Color(0xFF49746F)),
                  modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Choose plan")
                  }
            }
      }
}

@Composable
fun ProviderPackages(provider: Provider, packages: List<PackageProposal>) {
  var selectedIndex by remember { mutableStateOf(-1) }
  Box(
      modifier = Modifier.fillMaxSize(), // Fills the entire available space
      contentAlignment = Alignment.Center // Centers the LazyRow within the Box
      ) {
        // Horizontal scrollable list
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("packagesScrollableList"),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 30.dp, start = 8.dp, end = 8.dp),
        ) {
          items(packages.size) { index ->
            // If package is selected, we display it bigger
            val isSelected = selectedIndex == index
            val size by animateDpAsState(targetValue = if (isSelected) 335.dp else 320.dp)

            PackageCard(
                packageProposal = packages[index],
                isSelected = isSelected,
                modifier =
                    Modifier.width(250.dp)
                        .height(size)
                        .clickable { selectedIndex = if (isSelected) -1 else index }
                        .testTag("PackageCard"))
          }
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
fun ProviderTabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
  TabRow(
      selectedTabIndex = selectedTabIndex,
      modifier = Modifier.fillMaxWidth().testTag("providerTabs"),
      containerColor = colorScheme.primary,
      contentColor = colorScheme.onPrimary,
      indicator = { tabPositions ->
        TabRowDefaults.Indicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            height = 3.dp,
            color = colorScheme.onPrimary)
      }) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.testTag("profileTab"),
        ) {
          Text(
              "Profile",
              modifier = Modifier.padding(16.dp),
              color =
                  if (selectedTabIndex == 0) colorScheme.onPrimary
                  else colorScheme.onPrimary.copy(alpha = 0.6f),
              fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal)
        }
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.testTag("packagesTab")) {
              Text("Packages", modifier = Modifier.padding(16.dp))
            }
        Tab(
            selected = selectedTabIndex == 2,
            onClick = { onTabSelected(2) },
            modifier = Modifier.testTag("reviewsTab")) {
              Text(
                  "Reviews",
                  modifier = Modifier.padding(16.dp),
                  color =
                      if (selectedTabIndex == 1) colorScheme.onPrimary
                      else colorScheme.onPrimary.copy(alpha = 0.6f),
                  fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal)
            }
      }
}

@Composable
fun ProviderDetails(provider: Provider, reviews: List<Review>) {
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
fun ProviderReviews(provider: Provider, reviews: List<Review>) {
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
fun BottomBar() {
  val context = LocalContext.current
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
            onClick = { Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show() }) {
              Text(
                  "Book Now",
                  color = colorScheme.onPrimary,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold)
            }
      }
}
