package com.android.solvit.shared.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorTest {
  @Test
  fun `verify primary colors`() {
    assertEquals(Color(0xFF0099FF).toArgb(), Primary.toArgb())
    assertEquals(Color(0xFFFFFFFF).toArgb(), OnPrimary.toArgb())
    assertEquals(Color(0xFFB3E5FC).toArgb(), PrimaryContainer.toArgb())
    assertEquals(Color(0xFF001E2F).toArgb(), OnPrimaryContainer.toArgb())
  }

  @Test
  fun `verify secondary colors`() {
    assertEquals(Color(0xFF00C853).toArgb(), Secondary.toArgb())
    assertEquals(Color(0xFFFFFFFF).toArgb(), OnSecondary.toArgb())
    assertEquals(Color(0xFFB9F6CA).toArgb(), SecondaryContainer.toArgb())
    assertEquals(Color(0xFF00210B).toArgb(), OnSecondaryContainer.toArgb())
  }

  @Test
  fun `verify status colors`() {
    assertEquals(Color(0xFFE5A800).toArgb(), PENDING_color.toArgb())
    assertEquals(Color(0xFF00A3FF).toArgb(), ACCEPTED_color.toArgb())
    assertEquals(Color(0xFF00BFA5).toArgb(), STARTED_color.toArgb())
    assertEquals(Color(0xFF02F135).toArgb(), ENDED_color.toArgb())
    assertEquals(Color(0xFF000000).toArgb(), ARCHIVED_color.toArgb())
  }

  @Test
  fun `verify gradient colors`() {
    assertEquals(Color(0xFF2A5A52).toArgb(), Gradient1.toArgb())
    assertEquals(Color(0xFFDBD1B9).toArgb(), Gradient2.toArgb())
    assertEquals(Color(0xFFEFEBDE).toArgb(), Gradient3.toArgb())
    assertEquals(Color(0xFF4D5652).toArgb(), Gradient4.toArgb())
  }
}
