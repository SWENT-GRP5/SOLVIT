package com.android.solvit.kaspresso.end2end

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()


  @Before
  fun setUp(){
      val firestore = FirebaseFirestore.getInstance()
      firestore.useEmulator("127.0.0.1", 8080)
      FirebaseAuth.getInstance().useEmulator("127.0.0.1", 9099)

      firestore.firestoreSettings =
         FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
  }
  @Test
  fun endToEndTest() {
      composeTestRule.onNodeWithTag("ctaButton").performClick()
      composeTestRule.onNodeWithTag("signUpLink").performClick()
      composeTestRule.onNodeWithTag("emailInputField").performTextInput("test@test.com")
      composeTestRule.onNodeWithTag("passwordInput").performTextInput("password")
      composeTestRule.onNodeWithTag("confirmPasswordInput").performTextInput("password")
      composeTestRule.onNodeWithTag("signUpButton").performClick()
      composeTestRule.onNodeWithTag("customerButton").performClick()
      composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
      composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
      composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
      composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")
      composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()
      composeTestRule.onNodeWithTag("savePreferencesButton").performClick()
  }

}
