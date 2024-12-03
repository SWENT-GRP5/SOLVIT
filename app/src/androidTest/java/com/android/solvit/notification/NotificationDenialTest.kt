package com.android.solvit.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.android.solvit.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationDenialTest : NotificationBaseTest() {

  @Before
  override fun setUp() {
    super.setUp()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      revokeNotificationPermission()
    }
  }

  private fun findAndClickDenyButton() {
    // Try different button resource IDs that could represent the deny button
    val possibleButtonIds =
        listOf(
            "com.android.permissioncontroller:id/permission_deny_button", // Primary resource ID
            "android:id/button2" // Generic negative button
            )

    var buttonFound = false
    for (buttonId in possibleButtonIds) {
      val button = device.wait(Until.findObject(By.res(buttonId)), 1000)
      if (button != null) {
        button.click()
        buttonFound = true
        break
      }
    }

    // If no button found by resource ID, fall back to text
    if (!buttonFound) {
      val button = device.wait(Until.findObject(By.text("Don't allow")), 1000)
      assertNotNull("Could not find deny button", button)
      button?.click()
    }

    device.waitForIdle()
  }

  @Test
  fun notificationPermission_canBeDenied() {
    // Skip test if not running on Android 13+
    assumeTrue(
        "Test only runs on Android 13+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

    // Verify the permission is denied
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(PackageManager.PERMISSION_DENIED, permissionStatus)

    // Launch activity without forcing RESUMED state
    currentActivity = ActivityScenario.launch(MainActivity::class.java)

    // Wait for permission dialog and deny
    device.wait(Until.hasObject(By.textContains("notification")), 2000)
    findAndClickDenyButton()

    // Verify permission is still denied after interaction
    val finalPermissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(PackageManager.PERMISSION_DENIED, finalPermissionStatus)
  }
}
