package com.android.solvit.shared.ui.map

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.solvit.shared.model.map.Location

@Composable
fun GetDirectionsBubble(location: Location, onDismiss: () -> Unit) {
  val context = LocalContext.current
  Dialog(onDismissRequest = onDismiss) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.padding(16.dp)) {
          Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = location.name)
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                      val gmmIntentUri =
                          Uri.parse(
                              "google.navigation:q=${location.latitude},${location.longitude}")
                      val mapIntent =
                          Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                          }

                      if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                      } else {
                        // Fallback to a browser if Google Maps is not available
                        val browserIntent =
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"))
                        if (browserIntent.resolveActivity(context.packageManager) != null) {
                          context.startActivity(browserIntent)
                        } else {
                          Toast.makeText(context, "No navigation app available", Toast.LENGTH_SHORT)
                              .show()
                        }
                      }

                      // Dismiss the bubble after clicking "Get Directions"
                      onDismiss()
                    }) {
                      Text("Get Directions")
                    }
              }
        }
  }
}
