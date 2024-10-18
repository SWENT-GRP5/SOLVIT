package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class EditRequestScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<EditRequestScreenObject>(semanticsProvider = semanticProvider) {
  val inputRequestTitle: KNode = child {
    hasTestTag("inputRequestTitle")
    useUnmergedTree = true
  }

  val inputRequestDescription: KNode = child {
    hasTestTag("inputRequestDescription")
    useUnmergedTree = true
  }

  val inputRequestType: KNode = child {
    hasTestTag("inputServiceType")
    useUnmergedTree = true
  }

  val serviceTypeResult: KNode = child {
    hasTestTag("serviceTypeResult")
    useUnmergedTree = true
  }

  val inputRequestAddress: KNode = child {
    hasTestTag("inputRequestAddress")
    useUnmergedTree = true
  }

  val inputRequestDate: KNode = child {
    hasTestTag("inputRequestDate")
    useUnmergedTree = true
  }

  val imagePickerButton: KNode = child {
    hasTestTag("imagePickerButton")
    useUnmergedTree = true
  }

  val submitButton: KNode = child {
    hasTestTag("requestSubmit")
    useUnmergedTree = true
  }

  val deleteButton: KNode = child {
    hasTestTag("deleteRequestButton")
    useUnmergedTree = true
  }
}
