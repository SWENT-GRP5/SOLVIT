package com.android.solvit.provider.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
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
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
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

    `when`(providerRepository.getProvider(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      onSuccess(provider)
    }
    providerViewModel.getProvider("user123")

    composeTestRule.setContent {
      ProviderProfileScreen(
          providerViewModel, authViewModel, serviceRequestViewModel, mockNavigationActions)
    }
  }

  @Test
  fun providerProfileScreen_profileTopBar_displaysCorrectly() {
    composeTestRule.onNodeWithTag("ProfileTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileTopBar").assertIsDisplayed()
  }

  @Test
  fun providerProfileScreen_profileInfoCard_displaysCorrectly() {
    composeTestRule.onNodeWithTag("ProfileInfoCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditProfileButton").assertIsDisplayed()
  }

  @Test
  fun providerProfileScreen_profileInfoCard_performClick() {
    composeTestRule.onNodeWithTag("EditProfileButton").performClick()
    verify(mockNavigationActions).navigateTo(Screen.EDIT_PROVIDER_PROFILE)
  }

  @Test
  fun providerProfileScreen_additionalInfoCard_displaysCorrectly() {
    composeTestRule.onNodeWithTag("AdditionalInfosCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ServiceItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("LocationItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DescriptionItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("LanguagesItem").assertIsDisplayed()
  }

  @Test
  fun providerProfileScreen_insightsCard_displaysCorrectly() {
    composeTestRule.onNodeWithTag("InsightsCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("InsightsTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RatingItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PopularityItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EarningsItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TasksColumn").assertIsDisplayed()
    for (status in ServiceRequestStatus.entries) {
      composeTestRule.onNodeWithTag("${status.name}Tasks").assertIsDisplayed()
    }
  }

  @Test
  fun providerProfileScreen_aboutAndSupport_displaysCorrectly() {
    composeTestRule.onNodeWithTag("SecondGroupCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HelpSupportOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AboutAppOption").assertIsDisplayed()
  }
}
