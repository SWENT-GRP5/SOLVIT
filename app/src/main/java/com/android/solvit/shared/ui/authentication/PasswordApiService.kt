package com.android.solvit.shared.ui.authentication

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * An interface defining API endpoints for password generation. This interface uses Retrofit
 * annotations to specify HTTP requests and query parameters.
 *
 * @param includeDigits Whether to include digits in the generated password(s) (default: true).
 * @param includeLowercase Whether to include lowercase letters (default: true).
 * @param includeUppercase Whether to include uppercase letters (default: false).
 * @param includeSpecialCharacters Whether to include special characters (default: false).
 * @param passwordLength The length of the generated password(s) (default: 12).
 * @param quantity The number of passwords to generate (default: 1).
 * @return A `PasswordResponse` object containing the generated passwords.
 * @GET("create/") Defines an HTTP GET request to the "create/" endpoint.
 */
interface PasswordApiService {
  @GET("create/")
  suspend fun createPassword(
      @Query("include_digits") includeDigits: Boolean = true,
      @Query("include_lowercase") includeLowercase: Boolean = true,
      @Query("include_uppercase") includeUppercase: Boolean = false,
      @Query("include_special_characters") includeSpecialCharacters: Boolean = false,
      @Query("password_length") passwordLength: Int = 12,
      @Query("quantity") quantity: Int = 1
  ): PasswordResponse
}
