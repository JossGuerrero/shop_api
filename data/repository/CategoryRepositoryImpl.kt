package com.shopapp.data.repository

import com.shopapp.data.remote.api.CategoryApi
import com.shopapp.data.remote.dto.toDomain
import com.shopapp.data.remote.dto.toRequest
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.CategoryPayload
import com.shopapp.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val api: CategoryApi
) : CategoryRepository {
    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        val response = api.getCategories()
        if (response.isSuccessful) response.body()!!.map { it.toDomain() }
        else error("Error ${response.code()}")
    }

    override suspend fun getCategory(id: Int): Result<Category> = runCatching {
        val response = api.getCategory(id)
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}")
    }

    override suspend fun createCategory(payload: CategoryPayload): Result<Category> = runCatching {
        val response = api.createCategory(payload.toRequest())
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}")
    }

    override suspend fun updateCategory(id: Int, payload: CategoryPayload): Result<Category> = runCatching {
        val response = api.updateCategory(id, payload.toRequest())
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}")
    }

    override suspend fun deleteCategory(id: Int): Result<Unit> = runCatching {
        val response = api.deleteCategory(id)
        if (!response.isSuccessful) error("Error ${response.code()}")
    }
}
