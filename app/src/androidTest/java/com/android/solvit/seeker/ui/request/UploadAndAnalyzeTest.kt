package com.android.solvit.seeker.ui.request

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.android.solvit.shared.model.request.analyzer.uploadAndAnalyze
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class UploadAndAnalyzeTest {

  @Test
  fun uploadAndAnalyze_successfullyProcessesImages() = runTest {
    val mockContext = mock(Context::class.java)
    val mockUri1 = mock(Uri::class.java)
    val mockUri2 = mock(Uri::class.java)

    val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    // Mock `loadBitmap` to return bitmaps for provided URIs
    val loadBitmap: (Context, Uri) -> Bitmap? = { _, uri ->
      when (uri) {
        mockUri1 -> testBitmap
        else -> null
      }
    }

    // Mock `analyzeImages` to return a fixed result
    val analyzeImages: suspend (List<Bitmap>) -> Triple<String, String, String> = { bitmaps ->
      assertEquals(1, bitmaps.size)
      assertTrue(bitmaps.contains(testBitmap))
      Triple("Title", "Type", "Description")
    }

    // Call the function
    val result =
        uploadAndAnalyze(mockContext, listOf(mockUri1, mockUri2), loadBitmap, analyzeImages)

    // Verify the result
    assertEquals("Title", result.first)
    assertEquals("Type", result.second)
    assertEquals("Description", result.third)
  }

  @Test
  fun uploadAndAnalyze_handlesMissingBitmaps() = runTest {
    val mockContext = mock(Context::class.java)
    val mockUri1 = mock(Uri::class.java)
    val mockUri2 = mock(Uri::class.java)
    val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    // Mock `loadBitmap` to return a bitmap only for one URI
    val loadBitmap: (Context, Uri) -> Bitmap? = { _, uri ->
      when (uri) {
        mockUri1 -> testBitmap
        else -> null
      }
    }

    // Mock `analyzeImages` to validate and return a result
    val analyzeImages: suspend (List<Bitmap>) -> Triple<String, String, String> = { bitmaps ->
      assertEquals(1, bitmaps.size)
      assertTrue(bitmaps.contains(testBitmap))
      Triple("Title", "Type", "Description")
    }

    // Call the function
    val result =
        uploadAndAnalyze(mockContext, listOf(mockUri1, mockUri2), loadBitmap, analyzeImages)

    // Verify the result
    assertEquals("Title", result.first)
    assertEquals("Type", result.second)
    assertEquals("Description", result.third)
  }

  @Test
  fun uploadAndAnalyze_handlesExceptions() = runTest {
    val mockContext = mock(Context::class.java)
    val mockUri = mock(Uri::class.java)

    // Mock `loadBitmap` to throw an exception
    val loadBitmap: (Context, Uri) -> Bitmap? = { _, _ ->
      throw RuntimeException("Bitmap loading failed")
    }

    val analyzeImages: suspend (List<Bitmap>) -> Triple<String, String, String> = { _ ->
      Triple("Title", "Type", "Description")
    }

    // Call the function and assert exception
    val exception =
        kotlin
            .runCatching {
              uploadAndAnalyze(mockContext, listOf(mockUri), loadBitmap, analyzeImages)
            }
            .exceptionOrNull()

    assertTrue(exception is Exception)
    assertTrue(exception?.message!!.contains("Error during conversion and analysis"))
  }
}
