package com.android.solvit.kaspresso.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class SignInScreenObject(semanticProvider: SemanticsNodeInteractionsProvider) :
ComposeScreen<SignInScreenObject>(
    semanticsProvider = semanticProvider,
    viewBuilderAction = { hasTestTag("")}
){
}