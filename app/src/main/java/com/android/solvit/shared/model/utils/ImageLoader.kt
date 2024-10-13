package com.android.solvit.shared.model.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri

// Helper function to load a Bitmap from a URI
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
  return try {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    ImageDecoder.decodeBitmap(source)
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}
