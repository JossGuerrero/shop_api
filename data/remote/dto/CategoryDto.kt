package com.shopapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.CategoryPayload

data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("total_products") val totalProducts: Int,
    @SerializedName("created_at") val createdAt: String,
)

data class CategoryRequestDto(
    val name: String,
    val slug: String,
    val description: String,
    @SerializedName("is_active") val isActive: Boolean,
)

fun CategoryDto.toDomain() = Category(
    id = id,
    name = name,
    slug = slug,
    description = description,
    isActive = isActive,
    totalProducts = totalProducts,
    createdAt = createdAt
)

fun CategoryPayload.toRequest() = CategoryRequestDto(
    name = name,
    slug = slug,
    description = description,
    isActive = isActive
)
