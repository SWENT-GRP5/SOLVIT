package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class SignInScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SignInScreenObject>(semanticsProvider = semanticProvider) {

  val emailInput: KNode = child {
    hasTestTag("emailInput")
    useUnmergedTree = true
  }

  val passwordInput: KNode = child {
    hasTestTag("password")
    useUnmergedTree = true
  }

  val signInButton: KNode = child {
    hasTestTag("signInButton")
    useUnmergedTree = true
  }

  val googleSignInButton: KNode = child {
    hasTestTag("googleSignInButton")
    useUnmergedTree = true
  }

  val signUpLink: KNode = child {
    hasTestTag("signUpLink")
    useUnmergedTree = true
  }
}
