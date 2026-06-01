// domain/repository/CategoryRepository.kt
package com.shopapp.domain.repository

import com.shopapp.domain.model.Category
import com.shopapp.domain.model.CategoryPayload

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getCategory(id: Int): Result<Category>
    suspend fun createCategory(payload: CategoryPayload): Result<Category>
    suspend fun updateCategory(id: Int, payload: CategoryPayload): Result<Category>
    suspend fun deleteCategory(id: Int): Result<Unit>
    suspend fun getStats(): Result<Map<String, Any>>
}
