package com.android.solvit.shared.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {
  @Test
  fun `verify light color scheme values`() {
    with(LightColorScheme) {
      assertEquals(Primary, primary)
      assertEquals(OnPrimary, onPrimary)
      assertEquals(Secondary, secondary)
      assertEquals(Background, background)
      assertEquals(Surface, surface)
    }
  }

  @Test
  fun `verify dark color scheme values`() {
    with(DarkColorScheme) {
      assertEquals(Primary, primary)
      assertEquals(OnPrimary, onPrimary)
      assertEquals(Secondary, secondary)
      assertEquals(OnBackground, background)
      assertEquals(OnSurface, surface)
    }
  }
}
