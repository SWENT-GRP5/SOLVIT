package com.android.solvit.kaspresso.end2end

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun endToEndTest() =
      run {
        /*step("OpeningScreen Test"){
            OpeningScreenTest().openingScreenTest()
        }
        step("Sign In Screen Test") {
            SignInScreenTest().signInScreenTest()
        }
        step("Sign Up Screen Test") {
            SignUpScreenTest().signUpScreenTest()
        }
        step("Choose Profile Screen Test") {
            ChooseProfileScreenTest().chooseProfileScreenTest()
        }
        step("Services Screen Test") {
            ServicesScreenTest().servicesScreenTest()
        }
        step("Create Request Screen Test") {
            CreateRequestScreenTest().createRequestScreenTest()
        }
        step("Edit Request Screen Test") {
            EditRequestScreenTest().editRequestScreenTest()
        }*/
      }
}
