package com.shopapp.data.remote.api

import com.shopapp.data.remote.dto.UserDto
import com.shopapp.data.remote.dto.UserRequestDto
import com.shopapp.data.remote.dto.UserResponseDto
import com.shopapp.data.remote.dto.UserStatsDto
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("admin/users/")
    suspend fun getUsers(
        @Query("search") search: String?,
        @Query("is_staff") isStaff: Boolean?,
        @Query("is_active") isActive: Boolean?,
        @Query("page") page: Int?,
    ): Response<UserResponseDto>

    @GET("admin/users/{id}/")
    suspend fun getUser(@Path("id") id: Int): Response<UserDto>

    @POST("admin/users/")
    suspend fun createUser(@Body request: UserRequestDto): Response<UserDto>

    @PUT("admin/users/{id}/")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UserRequestDto): Response<UserDto>

    @DELETE("admin/users/{id}/")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    @POST("admin/users/{id}/toggle_active/")
    suspend fun toggleActive(@Path("id") id: Int): Response<UserDto>

    @GET("admin/users/stats/")
    suspend fun getStats(): Response<UserStatsDto>
}
