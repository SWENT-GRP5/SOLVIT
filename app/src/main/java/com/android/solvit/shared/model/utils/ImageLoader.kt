package com.android.solvit.shared.model.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

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

fun uploadImageToStorage(
    storage: FirebaseStorage,
    path: String,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
  val uniqueFileName = "${UUID.randomUUID()}.jpg"
  val imageRef: StorageReference = storage.reference.child("$path$uniqueFileName")

  imageRef
      .putFile(imageUri)
      .addOnSuccessListener {
        // Construct clean URL
        imageRef.downloadUrl
            .addOnSuccessListener { uri -> onSuccess((uri.toString())) }
            .addOnFailureListener { exception -> onFailure(exception) }
        /*val cleanUrl =
            "https://firebasestorage.googleapis.com/v0/b/${storage.app.options.storageBucket}/o/${Uri.encode("$path$uniqueFileName")}"
        onSuccess(cleanUrl)*/
      }
      .addOnFailureListener { exception -> onFailure(exception) }
}
