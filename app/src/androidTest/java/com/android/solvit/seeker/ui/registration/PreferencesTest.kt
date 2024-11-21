package com.android.solvit.seeker.ui.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.ui.profile.Preferences
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PreferencesTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var navigationActions: NavigationActions
  private lateinit var seekerViewModel: SeekerProfileViewModel
  private val userId = "12345"

  @Before
  fun setUp() {
    userRepository = mock(UserRepositoryFirestore::class.java)
    navigationActions = mock(NavigationActions::class.java)
    seekerViewModel = SeekerProfileViewModel(userRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.PREFERENCES)
  }

  @Test
  fun displayAllcomponents() {
    composeTestRule.setContent { Preferences(userId, seekerViewModel) }
    composeTestRule.onNodeWithTag("preferences_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("preference_button").assertIsDisplayed()
    val suggestionText = "🔧 Plumbing"
    val plumbingButton = composeTestRule.onNodeWithText(suggestionText)
    // Perform a click to select the item
    plumbingButton.performClick()
  }
}
