package com.android.solvit.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.android.solvit.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "NotificationPermissionTest"

@RunWith(AndroidJUnit4::class)
class NotificationPermissionTest : NotificationBaseTest() {

  companion object {
    private val PERMISSION_ALLOW_BUTTON_IDS =
        arrayOf(
            "com.android.permissioncontroller:id/permission_allow_button",
            "com.android.packageinstaller:id/permission_allow_button",
            "android:id/button1")
    private val PERMISSION_DENY_BUTTON_IDS =
        arrayOf(
            "com.android.permissioncontroller:id/permission_deny_button",
            "com.android.packageinstaller:id/permission_deny_button",
            "android:id/button2")
    private const val PERMISSION_DIALOG_TIMEOUT = 20000L
    private const val BUTTON_RETRY_COUNT = 5
    private const val BUTTON_RETRY_DELAY = 2000L
    private const val IDLE_TIMEOUT = 5000L
  }

  private fun logPermissionState(prefix: String = "") {
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    val statusStr =
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"
    Log.i(TAG, "$prefix Current notification permission status: $statusStr")
  }

  private fun waitForIdle() {
    try {
      Log.d(TAG, "Waiting for device to be idle...")
      device.waitForIdle(IDLE_TIMEOUT)
      Thread.sleep(1000)
    } catch (e: Exception) {
      Log.w(TAG, "Exception while waiting for idle: ${e.message}")
    }
  }

  private fun findButton(buttonIds: Array<String>, buttonType: String): UiObject2? {
    Log.i(TAG, "Attempting to find $buttonType button...")
    waitForIdle()

    for (attempt in 1..BUTTON_RETRY_COUNT) {
      Log.d(TAG, "Attempt $attempt to find $buttonType button")

      for (buttonId in buttonIds) {
        try {
          Log.d(TAG, "Looking for $buttonType button with ID: $buttonId")
          val button = device.wait(Until.findObject(By.res(buttonId)), PERMISSION_DIALOG_TIMEOUT)
          if (button != null && button.isEnabled) {
            Log.i(TAG, "Found enabled $buttonType button with ID: $buttonId")
            return button
          }
        } catch (e: Exception) {
          Log.w(TAG, "Exception while finding button: ${e.message}")
        }
      }

      if (attempt < BUTTON_RETRY_COUNT) {
        Log.d(TAG, "Button not found, waiting ${BUTTON_RETRY_DELAY}ms before retry")
        Thread.sleep(BUTTON_RETRY_DELAY)
      }
    }

    Log.w(TAG, "No enabled $buttonType button found after $BUTTON_RETRY_COUNT attempts")
    return null
  }

  private fun waitForPermissionDialog(): Boolean {
    Log.i(TAG, "Waiting for permission dialog to appear...")
    waitForIdle()

    for (attempt in 1..BUTTON_RETRY_COUNT) {
      for (buttonId in PERMISSION_ALLOW_BUTTON_IDS + PERMISSION_DENY_BUTTON_IDS) {
        try {
          val button = device.wait(Until.findObject(By.res(buttonId)), PERMISSION_DIALOG_TIMEOUT)
          if (button != null && button.isEnabled) {
            Log.i(TAG, "Permission dialog found with enabled button ID: $buttonId")
            return true
          }
        } catch (e: Exception) {
          Log.w(TAG, "Exception while finding dialog: ${e.message}")
        }
      }

      if (attempt < BUTTON_RETRY_COUNT) {
        Log.d(TAG, "Dialog not found, waiting ${BUTTON_RETRY_DELAY}ms before retry")
        Thread.sleep(BUTTON_RETRY_DELAY)
      }
    }

    Log.w(TAG, "Permission dialog not found after $BUTTON_RETRY_COUNT attempts")
    return false
  }

  private fun isPermissionDialogShown(): Boolean {
    for (buttonId in PERMISSION_ALLOW_BUTTON_IDS + PERMISSION_DENY_BUTTON_IDS) {
      try {
        val button = device.findObject(By.res(buttonId))
        if (button != null && button.isEnabled) {
          return true
        }
      } catch (e: Exception) {
        Log.w(TAG, "Exception while checking dialog: ${e.message}")
      }
    }
    return false
  }

  @Before
  override fun setUp() {
    super.setUp()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      Log.i(TAG, "Setting up test - revoking notification permission")
      revokeNotificationPermission()
      waitForIdle()

      val permissionStatus =
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
      assertTrue(
          "Permission should be initially denied",
          permissionStatus == PackageManager.PERMISSION_DENIED)
    }
  }

  @Test
  fun whenBelowAndroid13_noPermissionDialogShown() {
    assumeTrue(
        "Test only runs below Android 13", Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    Log.i(TAG, "Running test for Android version below 13")

    currentActivity = ActivityScenario.launch(MainActivity::class.java)
    waitForIdle()

    val dialogFound = waitForPermissionDialog()
    assertEquals("No permission dialog should be shown", false, dialogFound)
  }

  @Test
  fun whenPermissionDenied_permissionRemainsRevoked() {
    assumeTrue(
        "Test only runs on Android 13+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    Log.i(TAG, "Running permission denial test on Android 13+")
    logPermissionState("Initial state: ")

    // Launch activity and wait for it to be stable
    Log.i(TAG, "Launching activity...")
    currentActivity = ActivityScenario.launch(MainActivity::class.java)
    waitForIdle()
    Thread.sleep(2000) // Give extra time for activity to settle
    Log.i(TAG, "Activity launched and stable")

    // Verify initial permission state
    val initialPermissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    Log.i(
        TAG,
        "Initial permission status: ${if (initialPermissionStatus == PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"}")
    assertTrue(
        "Permission should be initially denied",
        initialPermissionStatus == PackageManager.PERMISSION_DENIED)

    // Wait and check for dialog
    Log.i(TAG, "Waiting for permission dialog...")
    val dialogFound = waitForPermissionDialog()
    Log.i(TAG, "Dialog found: $dialogFound")
    assertTrue("Permission dialog should be shown", dialogFound)

    // Verify dialog is visible
    val dialogVisible = isPermissionDialogShown()
    Log.i(TAG, "Dialog visible: $dialogVisible")
    assertTrue("Permission dialog should be visible", dialogVisible)

    // Find and click deny button
    val denyButton = findButton(PERMISSION_DENY_BUTTON_IDS, "deny")
    Log.i(TAG, "Deny button found: ${denyButton != null}")
    assertNotNull("Permission deny button should be present", denyButton)

    Log.i(TAG, "Clicking deny button")
    denyButton?.click()
    waitForIdle()

    // Verify final state
    logPermissionState("After denial: ")
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(PackageManager.PERMISSION_DENIED, permissionStatus)
  }

  @Test
  fun whenPermissionGranted_permissionIsApproved() {
    assumeTrue(
        "Test only runs on Android 13+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    Log.i(TAG, "Running permission grant test on Android 13+")
    logPermissionState("Initial state: ")

    currentActivity = ActivityScenario.launch(MainActivity::class.java)
    waitForIdle()
    Log.i(TAG, "Activity launched, waiting for permission dialog")

    val initialPermissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertTrue(
        "Permission should be initially denied",
        initialPermissionStatus == PackageManager.PERMISSION_DENIED)

    val dialogFound = waitForPermissionDialog()
    assertTrue("Permission dialog should be shown", dialogFound)
    assertTrue("Permission dialog should be visible", isPermissionDialogShown())

    val allowButton = findButton(PERMISSION_ALLOW_BUTTON_IDS, "allow")
    assertNotNull("Permission allow button should be present", allowButton)

    Log.i(TAG, "Clicking allow button")
    allowButton?.click()
    waitForIdle()

    logPermissionState("After granting: ")
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(
        "Permission should be granted", PackageManager.PERMISSION_GRANTED, permissionStatus)
  }
}
