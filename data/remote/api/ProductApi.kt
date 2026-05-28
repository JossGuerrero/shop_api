package com.shopapp.data.remote.api

import com.shopapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("products/")
    suspend fun getProducts(@QueryMap filters: Map<String, String>): Response<ProductListResponse>

    @GET("products/{id}/")
    suspend fun getProduct(@Path("id") id: Int): Response<ProductDto>

    @POST("products/")
    suspend fun createProduct(@Body request: ProductRequestDto): Response<ProductDto>

    @PUT("products/{id}/")
    suspend fun updateProduct(@Path("id") id: Int, @Body request: ProductRequestDto): Response<ProductDto>

    @DELETE("products/{id}/")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    @POST("products/{id}/restock/")
    suspend fun restock(@Path("id") id: Int, @Body request: RestockRequestDto): Response<RestockResponseDto>

    @GET("products/stats/")
    suspend fun getStats(): Response<ProductStatsDto>
}
