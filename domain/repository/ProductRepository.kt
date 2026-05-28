package com.shopapp.domain.repository

import com.shopapp.domain.model.Product
import com.shopapp.domain.model.ProductFilters
import com.shopapp.domain.model.ProductPayload

interface ProductRepository {
    suspend fun getProducts(filters: ProductFilters): Result<Pair<List<Product>, Int>>
    suspend fun getProduct(id: Int): Result<Product>
    suspend fun createProduct(payload: ProductPayload): Result<Product>
    suspend fun updateProduct(id: Int, payload: ProductPayload): Result<Product>
    suspend fun deleteProduct(id: Int): Result<Unit>
    suspend fun restock(id: Int, quantity: Int): Result<Int>
    suspend fun getStats(): Result<Map<String, Any>>
}
