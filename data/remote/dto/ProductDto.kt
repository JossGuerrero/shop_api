package com.shopapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.shopapp.domain.model.Product
import com.shopapp.domain.model.ProductPayload

data class ProductDto(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    @SerializedName("price_with_tax") val priceWithTax: Double,
    val stock: Int,
    @SerializedName("in_stock") val inStock: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("category") val categoryId: Int?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class ProductRequestDto(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("category") val categoryId: Int,
)

data class ProductListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ProductDto>
)

data class RestockRequestDto(
    val quantity: Int
)

data class RestockResponseDto(
    @SerializedName("new_stock") val newStock: Int
)

data class ProductStatsDto(
    @SerializedName("total_active") val totalActive: Int,
    @SerializedName("total_inactive") val totalInactive: Int,
    @SerializedName("avg_price") val avgPrice: Double?,
    @SerializedName("total_stock") val totalStock: Int?,
    @SerializedName("out_of_stock") val outOfStock: Int
)

fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    description = description,
    price = price,
    priceWithTax = priceWithTax,
    stock = stock,
    inStock = inStock,
    isActive = isActive,
    imageUrl = imageUrl,
    categoryId = categoryId,
    categoryName = categoryName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProductPayload.toRequest() = ProductRequestDto(
    name = name,
    description = description,
    price = price,
    stock = stock,
    isActive = isActive,
    categoryId = categoryId
)
