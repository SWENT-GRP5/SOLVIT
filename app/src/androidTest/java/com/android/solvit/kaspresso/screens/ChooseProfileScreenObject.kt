package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class ChooseProfileScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ChooseProfileScreenObject>(semanticsProvider = semanticProvider) {

  val customerButton: KNode = child {
    hasTestTag("customerButton")
    useUnmergedTree = true
  }

  val providerButton: KNode = child {
    hasTestTag("professionalButton")
    useUnmergedTree = true
  }

  val learnMoreLink: KNode = child {
    hasTestTag("learnMoreLink")
    useUnmergedTree = true
  }
}
