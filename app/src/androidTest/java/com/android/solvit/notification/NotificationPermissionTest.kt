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
class NotificationPermissionTest : NotificationBaseTest() {

  companion object {
    private const val PERMISSION_ALLOW_BUTTON_ID =
        "com.android.permissioncontroller:id/permission_allow_button"
    private const val PERMISSION_DENY_BUTTON_ID =
        "com.android.permissioncontroller:id/permission_deny_button"
  }

  @Before
  override fun setUp() {
    super.setUp()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      revokeNotificationPermission()
    }
    device.waitForIdle()
  }

  @Test
  fun whenBelowAndroid13_noPermissionDialogShown() {
    // Skip test if running on Android 13+
    assumeTrue(
        "Test only runs below Android 13", Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)

    // Launch activity
    currentActivity = ActivityScenario.launch(MainActivity::class.java)
    device.waitForIdle()

    // Verify no permission dialog is shown
    val permissionDialog = device.wait(Until.findObject(By.res(PERMISSION_ALLOW_BUTTON_ID)), 1000)
    assertEquals(null, permissionDialog)
  }

  @Test
  fun whenPermissionDenied_permissionRemainsRevoked() {
    // Skip test if not running on Android 13+
    assumeTrue(
        "Test only runs on Android 13+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

    // Launch activity
    currentActivity = ActivityScenario.launch(MainActivity::class.java)
    device.waitForIdle()

    // Wait for permission dialog and click deny
    val denyButton = device.wait(Until.findObject(By.res(PERMISSION_DENY_BUTTON_ID)), 2000)
    assertNotNull("Permission deny button should be present", denyButton)
    denyButton.click()
    device.waitForIdle()

    // Verify permission remains denied
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(PackageManager.PERMISSION_DENIED, permissionStatus)
  }

  @Test
  fun whenPermissionGranted_permissionIsApproved() {
    // Skip test if not running on Android 13+
    assumeTrue(
        "Test only runs on Android 13+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

    // Launch activity
    currentActivity = ActivityScenario.launch(MainActivity::class.java)
    device.waitForIdle()

    // Wait for permission dialog and click allow
    val allowButton = device.wait(Until.findObject(By.res(PERMISSION_ALLOW_BUTTON_ID)), 2000)
    assertNotNull("Permission allow button should be present", allowButton)
    allowButton.click()
    device.waitForIdle()
    Thread.sleep(500) // Give extra time for permission to be granted

    // Verify permission is granted
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(
        "Permission should be granted", PackageManager.PERMISSION_GRANTED, permissionStatus)
  }
}
