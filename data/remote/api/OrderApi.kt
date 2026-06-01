package com.shopapp.data.remote.api

import com.shopapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @GET("orders/")
    suspend fun getOrders(
        @Query("page") page: Int? = null,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>

    @GET("orders/{id}/")
    suspend fun getOrder(@Path("id") id: Int): Response<OrderDto>

    @POST("orders/")
    suspend fun createOrder(): Response<OrderDto>

    @POST("orders/{id}/add_item/")
    suspend fun addItem(@Path("id") id: Int, @Body request: AddItemRequestDto): Response<OrderDto>

    @POST("orders/{id}/confirm/")
    suspend fun confirmOrder(@Path("id") id: Int): Response<OrderDto>

    @PATCH("orders/{id}/status/")
    suspend fun updateStatus(@Path("id") id: Int, @Body request: UpdateStatusRequestDto): Response<OrderDto>

    @GET("orders/stats/")
    suspend fun getStats(): Response<OrderStatsDto>
}
