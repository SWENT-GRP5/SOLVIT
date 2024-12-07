package com.android.solvit.seeker.ui.provider

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.Review
import com.android.solvit.shared.model.review.ReviewRepository
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ProviderInfoTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var providerViewModel: ListProviderViewModel
  private lateinit var requestRepository: ServiceRequestRepository
  private lateinit var requestViewModel: ServiceRequestViewModel
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  private val provider =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F37a07762-d4ec-45ae-8c18-e74777d8a53b.jpg?alt=media&token=534578d5-9dad-404f-b129-9a3052331bc8",
          "",
          "",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          Timestamp.now(),
          listOf(Language.ENGLISH, Language.FRENCH))

  private val reviews =
      listOf(
          Review("1", "1", "1", "1", 5, "Very good tutor"),
          Review("2", "1", "1", "1", 4, "Good tutor"),
          Review("3", "1", "1", "1", 3, "Average tutor"))

  private val packageProposals =
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
      )

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    providerRepository = mock(ProviderRepository::class.java)
    providerViewModel = ListProviderViewModel(providerRepository)
    reviewRepository = mock(ReviewRepository::class.java)
    reviewViewModel = ReviewViewModel(reviewRepository)
    navController = mock(NavController::class.java)
    navigationActions = mock(NavigationActions::class.java)
    requestRepository = mock(ServiceRequestRepository::class.java)
    requestViewModel = ServiceRequestViewModel(requestRepository)
    providerViewModel.selectProvider(provider)
  }

  @Test
  fun providerHeaderDisplaysCorrectly() {
    composeTestRule.setContent { ProviderHeader(provider) }

    // Assert
    composeTestRule.onNodeWithTag("providerHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providerImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providerName").assertTextEquals("Hassan")
    composeTestRule.onNodeWithTag("providerCompanyName").assertTextEquals("")
  }

  @Test
  fun providerTabsSwitchCorrectly() {
    // Arrange
    var selectedTab by mutableStateOf(ProviderTab.DETAILS)

    // Act
    composeTestRule.setContent {
      ProviderTabs(
          selectedTab = ProviderTab.DETAILS, onTabSelected = { newTab -> selectedTab = newTab })
    }

    // Assert
    composeTestRule.onNodeWithTag("providerTabs").assertIsDisplayed()
    composeTestRule.onNodeWithTag("detailsTab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reviewsTab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packagesTab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("detailsTab").performClick()
    composeTestRule.onNodeWithTag("packagesTab").assertIsDisplayed()
    assertEquals(ProviderTab.DETAILS, selectedTab)
    composeTestRule.onNodeWithTag("packagesTab").performClick()
    assertEquals(ProviderTab.PACKAGES, selectedTab)
    composeTestRule.onNodeWithTag("reviewsTab").performClick()
    assertEquals(ProviderTab.REVIEWS, selectedTab)
  }

  @Test
  fun providerDetailsDisplayCorrectly() {
    // Act
    composeTestRule.setContent {
      ProviderDetails(
          provider = provider,
          reviews = reviews,
          showDialog = mutableStateOf(false),
          requestViewModel = requestViewModel,
          userId = "1",
          navigationActions = navigationActions,
          selectedPackage = mutableStateOf(null),
      )
    }

    // Assert
    composeTestRule.onNodeWithTag("providerDetails").assertIsDisplayed()
    composeTestRule.onNodeWithTag("detailsSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reviewsCount").assertTextEquals("3 Reviews")
    composeTestRule.onNodeWithTag("priceDisplay").assertTextEquals("CHF 25.0/hour")
    composeTestRule
        .onNodeWithTag("descriptionText")
        .assertTextEquals("Serious tutor giving courses in MATHS")
  }

  @Test
  fun providerReviewsDisplayCorrectly() {
    // Act
    composeTestRule.setContent {
      ProviderReviews(
          provider = provider,
          reviews = reviews,
          showDialog = mutableStateOf(false),
          requestViewModel = requestViewModel,
          userId = "1",
          navigationActions = navigationActions,
          selectedPackage = mutableStateOf(null),
      )
    }

    // Assert
    composeTestRule.onNodeWithTag("providerReviews").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reviewsOverview").assertIsDisplayed()
    composeTestRule.onNodeWithTag("overallTitle").assertTextEquals("Overall")
    composeTestRule.onNodeWithTag("overallRating").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reviewsTitle").assertTextEquals("Reviews")
    composeTestRule.onAllNodesWithTag("reviewRow")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("reviewRow")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("reviewRow")[2].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("reviewComment")[0].assertTextEquals("Very good tutor")
    composeTestRule.onAllNodesWithTag("reviewComment")[1].assertTextEquals("Good tutor")
    composeTestRule.onAllNodesWithTag("reviewComment")[2].assertTextEquals("Average tutor")
  }

  @Test
  fun providerPackagesDisplayCorrectly() {
    composeTestRule.setContent {
      ProviderPackages(
          provider,
          packages = packageProposals,
          selectedPackage = mutableStateOf(null),
          showDialog = mutableStateOf(false),
          requestViewModel = requestViewModel,
          userId = "1",
          navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithTag("packagesScrollableList").assertIsDisplayed()
    assertEquals(
        composeTestRule.onAllNodesWithTag("PackageCard").fetchSemanticsNodes().isNotEmpty(), true)
    composeTestRule.onAllNodesWithTag("PackageCard")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("PackageCard")[1].assertIsDisplayed()
  }

  @Test
  fun providerTopBarDisplaysCorrectly() {
    composeTestRule.setContent { ProviderTopBar(onBackClick = {}) }

    composeTestRule.onNodeWithTag("ProviderTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBarTitle").assertTextEquals("Provider")
    composeTestRule.onNodeWithTag("menuButton").assertIsDisplayed()
  }

  @Test
  fun backButtonTriggersCallback() {
    var backClicked = false
    composeTestRule.setContent { ProviderTopBar(onBackClick = { backClicked = true }) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    assertTrue(backClicked)
  }

  @Test
  fun bottomBarDisplaysCorrectly() {
    composeTestRule.setContent {
      BottomBar(
          showDialog = mutableStateOf(false),
      )
    }

    composeTestRule.onNodeWithTag("bottomBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bookNowButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bookNowButton").assertTextEquals("Book Now")
    composeTestRule.onNodeWithTag("bookNowButton").performClick()
  }

  @Test
  fun providerInfoScreenDisplaysCorrectly() {
    composeTestRule.setContent {
      ProviderInfoScreen(navigationActions, providerViewModel, reviewViewModel)
    }

    composeTestRule.onNodeWithTag("providerHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providerTabs").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bookNowButton").performClick()
  }

  @Test
  fun providerInfoScreenSwitchesTabsCorrectly() {
    composeTestRule.setContent {
      ProviderInfoScreen(navigationActions, providerViewModel, reviewViewModel)
    }

    composeTestRule.onNodeWithTag("detailsTab").performClick()
    composeTestRule.onNodeWithTag("providerDetails").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reviewsTab").performClick()
    composeTestRule.onNodeWithTag("providerReviews").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packagesTab").performClick()
    composeTestRule.onNodeWithTag("packagesScrollableList").assertIsDisplayed()
  }

  @Test
  fun selectRequestDialogDisplaysCorrectly() {
    composeTestRule.setContent {
      SelectRequestDialog(
          showDialog = mutableStateOf(true),
          selectedPackage = mutableStateOf(null),
          requestViewModel = requestViewModel,
          userId = "1",
          providerId = "1",
          providerType = Services.PLUMBER,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("dialog_card").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("dialog_title")
        .assertTextEquals("Choose the concerned service request:")
    composeTestRule.onNodeWithTag("requests_column").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismiss_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismiss_button").performClick()
    composeTestRule.onNodeWithTag("confirm_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirm_button").performClick()
  }
}
