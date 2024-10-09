package com.android.solvit.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.solvit.model.Language
import com.android.solvit.model.ListProviderViewModel
import com.android.solvit.model.Provider
import com.android.solvit.model.ProviderRepository
import com.android.solvit.model.Services
import com.android.solvit.model.map.Location
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ListProviderScreenTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val provider =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "image",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          Timestamp.now(),
          listOf(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.SPANISH))

  @Before
  fun setUp() {
    providerRepository = mock(ProviderRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    `when`(listProviderViewModel.getProviders()).then {
      it.getArgument<(List<Provider>) -> Unit>(0)(listOf(provider))
    }
  }

  @Test
  fun hasRequiredElements() {
    composeTestRule.setContent { selectProviderScreen(listProviderViewModel) }
    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterBar").assertIsDisplayed()
    /*composeTestRule.waitUntil (
        timeoutMillis =10000L,
        condition = {
            composeTestRule.onNodeWithTag("popularProviders").isDisplayed()
            composeTestRule.onNodeWithTag("popularProviders").isDisplayed()

        }
    )
    composeTestRule.onNodeWithTag("popularProviders").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providersList").assertIsDisplayed()*/
  }

  @Test
  fun filterProviderCallsFilterScreen() {
    composeTestRule.setContent { selectProviderScreen(listProviderViewModel) }
    composeTestRule.onNodeWithTag("filterOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterOption").performClick()
    composeTestRule.onNodeWithTag("filterSheet").assertIsDisplayed()
  }
}
