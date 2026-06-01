// data/remote/dto/CategoryDto.kt
package com.shopapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.CategoryPayload

data class CategoryDto(
    val id:          Int,
    val name:        String,
    val slug:        String,
    val description: String,
    @SerializedName("is_active")      val isActive:      Boolean,
    @SerializedName("total_products") val totalProducts: Int,
    @SerializedName("created_at")     val createdAt:     String,
)

data class CategoryRequestDto(
    val name:        String,
    val slug:        String,
    val description: String,
    @SerializedName("is_active") val isActive: Boolean,
)

data class CategoryResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<CategoryDto>
)

data class CategoryStatsDto(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val detail: List<CategoryDetailDto>
)

data class CategoryDetailDto(
    val id: Int,
    val name: String,
    val num_products: Int,
    val is_active: Boolean
)

// ── Mappers ───────────────────────────────────────────────────

fun CategoryDto.toDomain() = Category(
    id            = id,
    name          = name,
    slug          = slug,
    description   = description,
    isActive      = isActive,
    totalProducts = totalProducts,
    createdAt     = createdAt,
)

fun CategoryPayload.toRequest() = CategoryRequestDto(
    name        = name,
    slug        = slug,
    description = description,
    isActive    = isActive,
)
