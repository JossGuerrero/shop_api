package com.shopapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirm") val passwordConfirm: String
)

data class LogoutRequest(
    val refresh: String
)

data class AuthResponse(
    val access: String,
    val refresh: String,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val email: String,
    @SerializedName("is_staff") val isStaff: Boolean
)
