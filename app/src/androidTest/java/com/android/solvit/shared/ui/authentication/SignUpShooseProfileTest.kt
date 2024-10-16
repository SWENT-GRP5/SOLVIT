package com.android.solvit.shared.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SignUpChooseProfileTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val mockNavigationActions = mock(NavigationActions::class.java)
    private val mockAuthRepository = mock(AuthRepository::class.java)
    private val authViewModel = AuthViewModel(mockAuthRepository)

    @Test
    fun signUpChooseProfile_displaysAllComponents() {
        composeTestRule.setContent { SignUpChooseProfile(mockNavigationActions, authViewModel) }

        composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("roleIllustration").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signUpAsTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("customerButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("professionalButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("learnMoreLink").assertIsDisplayed()
    }

    @Test
    fun signUpChooseProfile_performClick() {
        composeTestRule.setContent { SignUpChooseProfile(mockNavigationActions, authViewModel) }

        composeTestRule.onNodeWithTag("backButton").performClick()
        composeTestRule.onNodeWithTag("customerButton").performClick()
        composeTestRule.onNodeWithTag("professionalButton").performClick()
        composeTestRule.onNodeWithTag("learnMoreLink").performClick()

        verify(mockNavigationActions).goBack()
    }
}