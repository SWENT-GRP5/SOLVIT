package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class ServicesScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ServicesScreenObject>(semanticsProvider = semanticProvider) {

  val searchBar: KNode = child {
    hasTestTag("searchBar")
    useUnmergedTree = true
  }

  val servicesGrid: KNode = child {
    hasTestTag("servicesGrid")
    useUnmergedTree = true
  }
}
