package com.android.solvit.seeker.ui.request

import android.webkit.JavascriptInterface

// Define an interface class to communicate with JavaScript
class AIWebInterface(private val onResult: (String) -> Unit) {
    @JavascriptInterface
    fun onAIResult(jsonResult: String) {
        // This method will be called by JavaScript
        onResult(jsonResult)
    }
}
