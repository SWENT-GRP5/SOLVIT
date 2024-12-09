package com.android.solvit.provider.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.solvit.provider.model.profile.ProviderViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class ProviderProfileScreenTest {

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var providerRepository: ProviderRepository
  private lateinit var providerViewModel: ProviderViewModel
  private lateinit var authRep: AuthRep
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel

  private val provider =
      Provider(
          uid = "user123",
          name = "John Doe",
          companyName = "Company",
          phone = "1234567890",
          location = Location(0.0, 0.0, "Chemin des Triaudes"),
          description = "Description",
          rating = 4.5,
          price = 50.0,
          languages = listOf(Language.ENGLISH, Language.FRENCH))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    providerRepository = mock(ProviderRepository::class.java)
    authRep = mock(AuthRep::class.java)
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    providerViewModel = ProviderViewModel(providerRepository)
    authViewModel = AuthViewModel(authRep)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)

    composeTestRule.setContent {
      ProviderProfileScreen(
          providerViewModel, authViewModel, serviceRequestViewModel, mockNavigationActions)
    }
  }

  @Test
  fun providerProfileScreen_profileHeader_displaysCorrectly() {
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("professionalName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyNameTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("serviceTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("service").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneNumber").assertIsDisplayed()
    composeTestRule.onNodeWithTag("locationTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("location").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed()
  }

  @Test
  fun providerProfileScreen_description_displaysCorrectly() {
    composeTestRule.onNodeWithTag("descriptionSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("descriptionTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("descriptionText").assertIsDisplayed()
  }

  @Test
  fun providerProfileScreen_profileHeader_performClick() {
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(mockNavigationActions).goBack()
  }

  @Test
  fun providerProfileScreen_logoutButton_performClick() {
    composeTestRule.onNodeWithTag("logoutButton").performClick()
  }

  @Test
  fun providerProfileScreen_StatsSection_displaysCorrectly() {
    composeTestRule.onNodeWithTag("statsSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ratingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ratingLabel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("popularityText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("popularityLabel").assertIsDisplayed()

    composeTestRule.onNodeWithTag("earningsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("earningsLabel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languagesLabel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pendingTasksText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptedTasksText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scheduledTasksText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("completedTasksText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("canceledTasksText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("archivedTasksText").assertIsDisplayed()
  }

  @Test
  fun languageList_displaysNotProvidedWhenEmpty() {
    val provider = Provider(languages = emptyList()) // A provider with no languages
    composeTestRule.setContent { LanguageList(provider = provider) }
    composeTestRule.onNodeWithTag("noLanguagesText").assertIsDisplayed()
  }

  @Test
  fun languageList_displaysThreeLanguagesInitially() {
    val provider =
        Provider(
            languages = listOf(Language.ENGLISH, Language.FRENCH, Language.ARABIC, Language.GERMAN))
    composeTestRule.setContent { LanguageList(provider = provider) }
    composeTestRule.onNodeWithTag("languageItem_ENGLISH").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageItem_FRENCH").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageItem_ARABIC").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageItem_GERMAN").assertIsNotDisplayed()
  }

  @Test
  fun languageList_displaysAllLanguagesAfterViewMoreClicked() {
    val provider =
        Provider(
            languages = listOf(Language.ENGLISH, Language.FRENCH, Language.ARABIC, Language.GERMAN))
    composeTestRule.setContent { LanguageList(provider = provider) }

    // Click the "View more" button
    composeTestRule.onNodeWithTag("viewMoreButton").performClick()

    // Assert all languages are displayed
    composeTestRule.onNodeWithTag("languageItem_ENGLISH").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageItem_FRENCH").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageItem_ARABIC").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageItem_GERMAN").assertIsDisplayed()
  }
}
