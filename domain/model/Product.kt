package com.shopapp.domain.model

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val priceWithTax: Double,
    val stock: Int,
    val inStock: Boolean,
    val isActive: Boolean,
    val imageUrl: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class ProductPayload(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val isActive: Boolean,
    val categoryId: Int,
)

data class ProductFilters(
    val search: String? = null,
    val category: Int? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val stockMin: Int? = null,
    val isActive: Boolean? = null,
    val ordering: String? = null,
    val page: Int = 1,
    val pageSize: Int = 12,
)