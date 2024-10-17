package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class SignUpScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SignUpScreenObject>(semanticsProvider = semanticProvider) {
  val googleSignUpButton: KNode = child {
    hasTestTag("googleSignUpButton")
    useUnmergedTree = true
  }

  val emailInput: KNode = child {
    hasTestTag("emailInputField")
    useUnmergedTree = true
  }

  val passwordInput: KNode = child {
    hasTestTag("passwordInput")
    useUnmergedTree = true
  }

  val confirmPasswordInput: KNode = child {
    hasTestTag("confirmPasswordInput")
    useUnmergedTree = true
  }

  val signUpButton: KNode = child {
    hasTestTag("signUpButton")
    useUnmergedTree = true
  }
}
