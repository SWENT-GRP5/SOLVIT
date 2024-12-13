package com.android.solvit.seeker.ui.service

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import kotlinx.coroutines.flow.take
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class ServicesScreenTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var userRepository: UserRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var navController: NavController
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel

  private val provider1 =
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
          4.0,
          25.0,
          20.0,
          listOf(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.SPANISH))

  private val provider2 =
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
          3.0,
          25.0,
          20.0,
          listOf(Language.SPANISH))

  private val testSeekerProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "1234567890",
          address = "Chemin des Triaudes")

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    providerRepository = Mockito.mock(ProviderRepository::class.java)
    navController = Mockito.mock(NavController::class.java)
    navigationActions = Mockito.mock(NavigationActions::class.java)
    `when`(navigationActions.currentRoute()).thenReturn(Route.SEEKER_OVERVIEW)
    composeTestRule.setContent { ServicesScreen(navigationActions, listProviderViewModel) }
    listProviderViewModel = ListProviderViewModel(providerRepository)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)

    // Mock successful data loading
    `when`(providerRepository.getProviders(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(1)
      onSuccess(listOf(provider1, provider2)) // Simulate success
    }

    // Trigger Data Loading
    listProviderViewModel.getProviders()

    `when`(userRepository.getUserProfile(eq("1234"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(SeekerProfile) -> Unit>(1)
      onSuccess(testSeekerProfile) // Simulate success
    }
    // Call the ViewModel method
    seekerProfileViewModel.getUserProfile("1234")
    `when`(navigationActions.currentRoute()).thenReturn(Route.SERVICES)

    composeTestRule.setContent {
      ServicesScreen(navigationActions, seekerProfileViewModel, listProviderViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenTopSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenDiscount").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenShortcuts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCategories").assertIsDisplayed()
  }

  @Test
  fun topSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenTopSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenLocationIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCurrentLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenUserName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenSearchBar").assertIsDisplayed()
  }

  @Test
  fun profileImageNavigatesToProfileScreen() {
    composeTestRule.onNodeWithTag("servicesScreenProfileImage").performClick()
    verify(navigationActions).navigateTo(Route.SEEKER_PROFILE)
  }

  @Test
  fun discountButtonNavigatesToDiscountScreen() {
    val service = SERVICES_LIST[0]
    composeTestRule.onNodeWithTag("servicesScreenDiscount").performClick()
    assert(service.service == listProviderViewModel.selectedService.value)
    verify(navigationActions).navigateTo(Route.PROVIDERS)
  }

  @Test
  fun discountSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenDiscount").assertIsDisplayed()
  }

  @Test
  fun shortcutsSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenShortcuts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("solveItWithAi").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenOrdersShortcut").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenMapShortcut").assertIsDisplayed()
  }

  @Test
  fun providersShortcutsNavigateToProvidersScreen() {
    composeTestRule.onNodeWithTag("solveItWithAi").performClick()
    verify(navigationActions).navigateTo(Route.AI_SOLVER)
  }

  @Test
  fun ordersShortcutsNavigateToOrdersScreen() {
    composeTestRule.onNodeWithTag("servicesScreenOrdersShortcut").performClick()
    verify(navigationActions).navigateTo(Route.REQUESTS_OVERVIEW)
  }

  @Test
  fun mapShortcutsNavigateToMapScreen() {
    composeTestRule.onNodeWithTag("servicesScreenMapShortcut").performClick()
    verify(navigationActions).navigateTo(Route.MAP_OF_SEEKER)
  }

  @Test
  fun categoriesSectionHasRequiredComponents() {
    listProviderViewModel.getProviders()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("servicesScreenCategories").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCategoriesTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCategoriesList").assertIsDisplayed()
  }

  @Test
  fun servicesItemsAreDisplayed() {
    for (service in SERVICES_LIST.take(3)) {
      composeTestRule.onNodeWithTag(service.service.toString() + "Item").assertIsDisplayed()
    }
  }

  @Test
  fun providersItemsAreDisplayed() {
    `when`(providerRepository.getProviders(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(1)
      onSuccess(listOf(provider1, provider2)) // Simulate success
    }

    listProviderViewModel.getProviders()

    for (provider in listProviderViewModel.providersList.value.take(3)) {
      composeTestRule.onNodeWithTag(provider.name + "Item").assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(Services.getProfileImage(provider.service).toString() + "Image")
          .assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(Services.getIcon(provider.service).toString() + "Icon")
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag(provider.rating.toString() + "Rating").assertIsDisplayed()
      composeTestRule.onNodeWithTag(provider.name + "Name").assertIsDisplayed()
      composeTestRule.onNodeWithTag(provider.service.toString() + "Service").assertIsDisplayed()
    }
  }

  @Test
  fun clickServiceItemNavigatesToProvidersScreen() {
    val service = SERVICES_LIST[0]
    composeTestRule.onNodeWithTag(service.service.toString() + "Item").performClick()
    assert(service.service == listProviderViewModel.selectedService.value)
    verify(navigationActions).navigateTo(Route.PROVIDERS_LIST)
  }
}
