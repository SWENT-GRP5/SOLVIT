package com.android.solvit.shared.ui.authentication

import retrofit2.http.GET
import retrofit2.http.Query

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
