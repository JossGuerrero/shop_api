package com.shopapp.domain.model

data class Category(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val isActive: Boolean,
    val totalProducts: Int,
    val createdAt: String,
)

data class CategoryPayload(
    val name: String,
    val slug: String,
    val description: String,
    val isActive: Boolean,
)