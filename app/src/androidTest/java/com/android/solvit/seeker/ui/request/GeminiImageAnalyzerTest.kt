package com.android.solvit.seeker.ui.request

import android.graphics.Bitmap
import com.android.solvit.shared.model.request.analyzer.GeminiImageAnalyzer
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GeminiImageAnalyzerTest {

  @Test
  fun analyzeImages_integrationTest() = runTest {
    val bitmaps = listOf(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))

    // Use the real GeminiImageAnalyzer
    val analyzer = GeminiImageAnalyzer()

    val result = analyzer.analyzeImages(bitmaps)

    assertNotNull(result.first)
    assertNotNull(result.second)
    assertNotNull(result.third)
  }
}
