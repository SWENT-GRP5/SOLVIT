package com.android.solvit.shared.ui.utils

object ValidationRegex {
    val PHONE_REGEX = Regex("^[+]?[0-9]{6,15}$")
    val EMAIL_REGEX = Regex("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")
    val FULL_NAME_REGEX = Regex("^[a-zA-Z]+(?:[-' ][a-zA-Z]+)* [a-zA-Z]+$")
    val STARTING_PRICE_REGEX = Regex("^(0|[1-9]\\d*)(\\.\\d{1,2})?\$")
    val NAME_REGEX = Regex("^[a-zA-ZÀ-ÿ '-]{2,50}$")
    val DESCRIPTION_REGEX = Regex("^[a-zA-ZÀ-ÿ0-9 ,.!?-]{1,500}$")
}