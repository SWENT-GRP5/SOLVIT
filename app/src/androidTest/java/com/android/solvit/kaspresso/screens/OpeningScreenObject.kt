package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class OpeningScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<OpeningScreenObject>(semanticsProvider = semanticProvider) {

  val ctaButton: KNode = child {
    hasTestTag("ctaButton")
    useUnmergedTree = true
  }
}
