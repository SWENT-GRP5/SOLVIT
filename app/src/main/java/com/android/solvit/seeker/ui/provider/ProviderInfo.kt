package com.android.solvit.seeker.ui.provider

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

  Scaffold(
      topBar = { ProviderTopBar(onBackClick = { navigationActions.goBack() }) },
      content = { padding ->
        Column(modifier = Modifier.padding(padding)) {
          ProviderHeader(provider)
          ProviderTabs(selectedTabIndex) { newIndex -> selectedTabIndex = newIndex }

          // Display content based on the selected tab
          when (selectedTabIndex) {
            0 ->
                ProviderDetails(
                    provider, reviews) // Display ProviderDetails if "Profile" tab is selected
            1 ->
                ProviderReviews(
                    provider, reviews) // Display ProviderReviews if "Reviews" tab is selected
          }
        }
      },
      bottomBar = { BottomBar() })
}

@Composable
fun ProviderTopBar(onBackClick: () -> Unit) {
  val context = LocalContext.current
  Row(
      modifier = Modifier.fillMaxWidth().background(color = Color.White).testTag("ProviderTopBar"),
      verticalAlignment = Alignment.CenterVertically) {
        // Back button on the left
        IconButton(onClick = onBackClick, modifier = Modifier.testTag("backButton")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Title in the center
        Text(
            text = "Performer",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).testTag("topBarTitle"),
            textAlign = TextAlign.Start)

        // Menu icon on the right
        IconButton(
            onClick = { Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.testTag("menuButton")) {
              Icon(
                  painter = painterResource(id = R.drawable.menu_icon),
                  contentDescription = "Menu",
                  modifier = Modifier.size(24.dp),
                  tint = Color.Unspecified)
            }
      }
}

@Composable
fun ProviderHeader(provider: Provider) {
  Box(modifier = Modifier.fillMaxWidth().background(Color.White).testTag("providerHeader")) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      val context = LocalContext.current
      AsyncImage(
          model = if (provider.imageUrl != "") provider.imageUrl else R.drawable.default_pdp,
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
            modifier = Modifier.testTag("providerName"))
        Text(
            text = provider.companyName,
            color = Color.Gray,
            modifier = Modifier.testTag("providerCompanyName"))
      }

      // Share icon on the right
      IconButton(
          onClick = { Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show() },
          modifier = Modifier.testTag("shareButton")) {
            Icon(Icons.Default.Share, contentDescription = "Share")
          }
    }
  }
}

@Composable
fun ProviderTabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
  TabRow(
      selectedTabIndex = selectedTabIndex,
      modifier = Modifier.fillMaxWidth().testTag("providerTabs"),
      containerColor = Color(0xFF0099FF),
      contentColor = Color.White) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.testTag("profileTab")) {
              Text("Profile", modifier = Modifier.padding(16.dp))
            }
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.testTag("reviewsTab")) {
              Text("Reviews", modifier = Modifier.padding(16.dp))
            }
      }
}

@Composable
fun ProviderDetails(provider: Provider, reviews: List<Review>) {
  Column(
      modifier =
          Modifier.padding(16.dp)
              .fillMaxWidth()
              .background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
              .testTag("providerDetails")) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("detailsSection")) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()) {
                    RatingStars(provider.rating.toInt())
                    Text(
                        text = "${reviews.size} Reviews",
                        color = Color.Gray,
                        modifier = Modifier.testTag("reviewsCount"))
                    Text(
                        text = "15 Jobs",
                        color = Color.Gray,
                        modifier = Modifier.testTag("jobsCount")) // Replace with actual job count
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Service description
              Text("Refrigerator repair", fontSize = 18.sp, fontWeight = FontWeight.Bold)
              Text(
                  text = "CHF ${provider.price}/hour",
                  fontSize = 16.sp,
                  color = Color.Gray,
                  modifier = Modifier.padding(vertical = 4.dp).testTag("priceDisplay"))
            }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Descriptions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("descriptionTitle"))

        Spacer(modifier = Modifier.height(8.dp))

        // Description section with white background and border
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("descriptionSection")) {
              Text(
                  text = provider.description,
                  modifier = Modifier.padding(vertical = 4.dp).testTag("descriptionText"),
                  color = Color.Gray,
                  fontWeight = FontWeight.Medium)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Contact",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("contactTitle"))

        Spacer(modifier = Modifier.height(8.dp))

        // Contact section with white background and border
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("contactSection")) {
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
                            "The contractor's contacts are visible only to its customers." +
                                "If you are interested in the services of this contractor" +
                                " - offer him an order.",
                        modifier = Modifier.padding(vertical = 4.dp).testTag("contactText"),
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium)
                  }
            }
      }
}

@Composable
fun ProviderReviews(provider: Provider, reviews: List<Review>) {
  Column(
      modifier =
          Modifier.padding(16.dp)
              .fillMaxWidth()
              .background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
              .testTag("providerReviews")) {
        Column(
            Modifier.fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .border(width = 2.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("reviewsOverview")) {
              Text(
                  "Overall",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Gray,
                  modifier = Modifier.testTag("overallTitle"))

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth().testTag("overallRating")) {
                    Text(provider.rating.toString(), fontSize = 35.sp, fontWeight = FontWeight.Bold)
                    RatingStars(provider.rating.toInt())
                  }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                  "${reviews.size} " + if (reviews.size > 1) "Reviews" else "Review",
                  modifier = Modifier.padding(vertical = 4.dp),
                  color = Color.Gray)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Reviews",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("reviewsTitle"))

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
          if (reviews.isEmpty()) {
            item { Text("No reviews yet", modifier = Modifier.padding(16.dp), color = Color.Gray) }
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
          .background(color = Color.White, shape = RoundedCornerShape(16.dp))
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
            color = Color.Gray)
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
          tint = if (index < rating) Color(0xFF0099FF) else Color.Gray)
    }
  }
}

@Composable
fun BottomBar() {
  val context = LocalContext.current
  Row(
      modifier = Modifier.fillMaxWidth().testTag("bottomBar"),
      horizontalArrangement = Arrangement.Center) {
        Button(
            modifier =
                Modifier.padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0099FF))
                    .size(200.dp, 50.dp)
                    .testTag("bookNowButton"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099FF)),
            onClick = { Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show() }) {
              Text("Book Now", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
      }
}
