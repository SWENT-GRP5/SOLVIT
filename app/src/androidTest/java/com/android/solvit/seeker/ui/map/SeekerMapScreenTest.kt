package com.android.solvit.seeker.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any

class SeekerMapScreenTest {
    private lateinit var providerRepository: ProviderRepository
    private lateinit var listProviderViewModel: ListProviderViewModel
    private lateinit var navController: NavController
    private lateinit var navigationActions: NavigationActions

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testProviders =
        listOf(
            Provider(
                uid = "1",
                name = "Test Provider 1",
                service = Services.WRITER,
                imageUrl = "https://example",
                location = Location(0.0, 0.0, "Test Location 1"),
                rating = 4.5,
                price = 10.0,
                description = "Test Description 1",
                languages = listOf(Language.ARABIC, Language.ENGLISH),
                deliveryTime = Timestamp(0, 0),
                popular = true
            ),
            Provider(
                uid = "2",
                name = "Test Provider 2",
                service = Services.WRITER,
                imageUrl = "https://example",
                location = Location(10.0, 10.0, "Test Location 2"),
                rating = 4.5,
                price = 10.0,
                description = "Test Description 2",
                languages = listOf(Language.ARABIC, Language.ENGLISH),
                deliveryTime = Timestamp(0, 0),
                popular = true
            )
        )

    @Before
    fun setUp() {
        providerRepository = Mockito.mock(ProviderRepository::class.java)
        listProviderViewModel = ListProviderViewModel(providerRepository)
        navController = Mockito.mock(NavController::class.java)
        navigationActions = NavigationActions(navController)

        Mockito.`when`(providerRepository.getProviders(any(), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<(List<Provider>) -> Unit>(0)
                onSuccess(testProviders)
            }
    }

    @Test
    fun hasRequiredElements() {
        composeTestRule.setContent { SeekerMapScreen(listProviderViewModel, navigationActions) }

        composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("googleMap").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    }

}
