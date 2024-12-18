package com.android.solvit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple activity for testing notifications. This avoids interfering with MainActivity state in
 * tests.
 */
class TestActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val fromNotification = intent.getBooleanExtra("notification", false)
    val title = intent.getStringExtra("title") ?: ""

    setContent {
      Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Test Activity")
            if (fromNotification) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = "From Notification: $title")
            }
          }
    }
  }
}
