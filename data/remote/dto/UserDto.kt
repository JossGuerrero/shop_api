package com.shopapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.shopapp.domain.model.User
import com.shopapp.domain.model.UserPayload

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("is_staff") val isStaff: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("date_joined") val dateJoined: String,
    @SerializedName("num_orders") val numOrders: Int,
)

data class UserRequestDto(
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("is_staff") val isStaff: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    val password: String? = null,
)

data class UserResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<UserDto>
)

data class UserStatsDto(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val staff: Int
)

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    isStaff = isStaff,
    isActive = isActive,
    dateJoined = dateJoined,
    numOrders = numOrders
)

fun UserPayload.toRequest() = UserRequestDto(
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    isStaff = isStaff,
    isActive = isActive,
    password = password
)
