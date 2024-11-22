package com.android.solvit.seeker.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class EditPreferencesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var userRepository: UserRepositoryFirestore
    private lateinit var navigationActions: NavigationActions
    private lateinit var seekerViewModel: SeekerProfileViewModel
    private val userId = "12345"

    @Before
    fun setUp() {
        userRepository = Mockito.mock(UserRepositoryFirestore::class.java)
        navigationActions = Mockito.mock(NavigationActions::class.java)
        seekerViewModel = SeekerProfileViewModel(userRepository)

        `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_PREFERENCES)
        val selectedPreferences = listOf(
            "🔧 Plumbing", "⚡ Electrical Work", "📚 Tutoring", "🎉 Event Planning", "💇 Hair Styling"
        )
        `when`(userRepository.getUserPreferences(eq(userId),any(),any()))
            .thenAnswer { invocation ->
                // Get the callback for onSuccess (assuming getUserPreferences uses a callback or a similar approach)
                val onSuccess = invocation.getArgument<(List<String>) -> Unit>(1)
                onSuccess(selectedPreferences) // Return the mocked preferences
            }
    }

    @Test
    fun testDisplayAllComponents() {
        composeTestRule.setContent { EditPreferences(userId, seekerViewModel, navigationActions) }

        // Test that all components are displayed with correct tags
        composeTestRule.onNodeWithTag("edit_preferences_title").assertIsDisplayed() // Title
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed() // Back button
        composeTestRule.onNodeWithTag("preferencesIllustration").assertIsDisplayed() // Image

        // Check if the selected preferences are displayed correctly
        composeTestRule.onNodeWithTag("selected_preferences_title")
            .assertTextEquals("Selected preferences:")
        composeTestRule.onNodeWithTag("available_preferences_title")
            .assertTextEquals("You might like:")
        // Test if the "Update Preferences" button is displayed
        composeTestRule.onNodeWithTag("updatePreferencesButton").assertIsDisplayed()


    }

}
