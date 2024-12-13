package com.android.solvit.shared.ui.utils

/**
 * Object containing validation regex patterns for various types of user inputs. These patterns are
 * designed to validate input formats for fields such as phone numbers, email addresses, full names,
 * starting prices, general names, and descriptions.
 *
 * Properties:
 * - PHONE_REGEX: Validates international phone numbers with an optional leading '+'.
 *     - Format: Must contain 6 to 15 digits, e.g., "+123456789" or "123456".
 * - EMAIL_REGEX: Validates email addresses with standard email format.
 *     - Format: Alphanumeric characters and special symbols (`.`, `-`, `_`) before the `@`,
 *       followed by a domain name and a valid TLD, e.g., "example@domain.com".
 * - FULL_NAME_REGEX: Validates a person's full name, requiring at least one space separating the
 *   first and last name. It allows hyphens, apostrophes, and spaces within names.
 *     - Format: "John Doe", "Anne-Marie O'Connor".
 * - STARTING_PRICE_REGEX: Validates numeric values with up to two decimal places, representing
 *   prices.
 *     - Format: Non-negative numbers, optionally with a decimal point, e.g., "10", "10.99", "0.5".
 * - NAME_REGEX: Validates general names, allowing letters (including accents), spaces, hyphens, and
 *   apostrophes. The name must be 2 to 50 characters long.
 *     - Format: "Jean-Luc", "Marie O'Connor", "José".
 * - DESCRIPTION_REGEX: Validates general descriptions, allowing alphanumeric characters, accented
 *   letters, spaces, and basic punctuation marks (`,`, `.`, `!`, `?`, `-`). The description can be
 *   1 to 500 characters long.
 *     - Format: "This is a great product!", "L'art de vivre, 100% naturel!".
 */
object ValidationRegex {
  val PHONE_REGEX = Regex("^[+]?[0-9]{6,15}$")
  val EMAIL_REGEX = Regex("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")
  val FULL_NAME_REGEX = Regex("^[a-zA-Z]+(?:[-' ][a-zA-Z]+)* [a-zA-Z]+$")
  val STARTING_PRICE_REGEX = Regex("^(0|[1-9]\\d*)(\\.\\d{1,2})?\$")
  val NAME_REGEX = Regex("^[a-zA-ZÀ-ÿ '-]{2,50}$")
  val DESCRIPTION_REGEX = Regex("^[a-zA-ZÀ-ÿ0-9 ,.!?-]{1,500}$")
}
