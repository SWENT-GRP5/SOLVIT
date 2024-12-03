package com.android.solvit.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.android.solvit.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationGrantTest : NotificationBaseTest() {

  @Before
  override fun setUp() {
    super.setUp()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      grantNotificationPermission()
    }
  }

  @Test
  fun notificationPermission_isGrantedOnTiramisuAndAbove() {
    // Skip test if not running on Android 13+
    assumeTrue(
        "Test only runs on Android 13+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

    // Verify permission is granted
    val permissionStatus =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    assertEquals(PackageManager.PERMISSION_GRANTED, permissionStatus)

    // Launch activity
    currentActivity =
        ActivityScenario.launch(MainActivity::class.java).also {
          it.moveToState(Lifecycle.State.RESUMED)
        }

    // Verify no permission dialog shows
    device.wait(Until.gone(By.textContains("notification")), 1000)
  }
}
