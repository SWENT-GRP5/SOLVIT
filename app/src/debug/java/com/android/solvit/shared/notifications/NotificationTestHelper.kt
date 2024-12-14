package com.android.solvit.shared.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.android.solvit.TestActivity

object NotificationTestHelper {
  fun getTestPendingIntent(context: Context, title: String): PendingIntent? {
    return if (title == "Test Title") {
      val intent =
          Intent(context, TestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification", true)
            putExtra("title", title)
          }
      PendingIntent.getActivity(
          context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    } else {
      null
    }
  }
}
