package com.android.solvit.shared.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest2 : TestCase(
    kaspressoBuilder = Kaspresso.Builder.withComposeSupport()
){
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAllSeeker() = run {
    }
}