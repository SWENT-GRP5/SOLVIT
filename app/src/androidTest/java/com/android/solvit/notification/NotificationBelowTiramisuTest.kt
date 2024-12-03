package com.android.solvit.notification

import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.android.solvit.MainActivity
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationBelowTiramisuTest : NotificationBaseTest() {

  @Test
  fun notificationPermission_isNotRequiredBelowTiramisu() {
    // Skip test if running on Android 13+
    assumeFalse(
        "Test only runs below Android 13", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

    // Launch activity
    currentActivity =
        ActivityScenario.launch(MainActivity::class.java).also {
          it.moveToState(Lifecycle.State.RESUMED)
        }

    // No permission request should be shown
    device.wait(Until.gone(By.textContains("notification")), 1000)
  }
}
