package com.android.solvit.notification

import android.Manifest
import android.app.Instrumentation
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.android.solvit.MainActivity
import org.junit.After
import org.junit.Before

abstract class NotificationBaseTest {
  protected lateinit var context: Context
  protected lateinit var device: UiDevice
  protected lateinit var instrumentation: Instrumentation
  protected var currentActivity: ActivityScenario<MainActivity>? = null

  @Before
  open fun setUp() {
    instrumentation = InstrumentationRegistry.getInstrumentation()
    context = ApplicationProvider.getApplicationContext()
    device = UiDevice.getInstance(instrumentation)
    device.waitForIdle()
  }

  @After
  open fun tearDown() {
    currentActivity?.close()
    device.waitForIdle()
  }

  protected fun revokeNotificationPermission() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      instrumentation.uiAutomation.revokeRuntimePermission(
          context.packageName, Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  protected fun grantNotificationPermission() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      instrumentation.uiAutomation.grantRuntimePermission(
          context.packageName, Manifest.permission.POST_NOTIFICATIONS)
    }
  }
}
