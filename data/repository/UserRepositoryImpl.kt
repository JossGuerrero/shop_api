// data/repository/UserRepositoryImpl.kt
package com.shopapp.data.repository

import com.shopapp.data.remote.api.UserApi
import com.shopapp.data.remote.dto.UserRequestDto
import com.shopapp.data.remote.dto.toDomain
import com.shopapp.data.remote.dto.toRequest
import com.shopapp.domain.model.User
import com.shopapp.domain.model.UserPayload
import com.shopapp.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
) : UserRepository {

    override suspend fun getUsers(
        search:   String?,
        isStaff:  Boolean?,
        isActive: Boolean?,
        page:     Int?,
    ): Result<Pair<List<User>, Int>> = runCatching {
        val response = api.getUsers(search, isStaff, isActive, page)
        if (response.isSuccessful) {
            val body = response.body()!!
            Pair(body.results.map { it.toDomain() }, body.count)
        } else error("Error ${response.code()}")
    }

    override suspend fun getUser(id: Int): Result<User> = runCatching {
        val response = api.getUser(id)
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}")
    }

    override suspend fun createUser(payload: UserPayload): Result<User> = runCatching {
        val response = api.createUser(payload.toRequest())
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}: ${response.errorBody()?.string()}")
    }

    override suspend fun updateUser(id: Int, payload: UserPayload): Result<User> = runCatching {
        val response = api.updateUser(id, payload.toRequest())
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}: ${response.errorBody()?.string()}")
    }

    override suspend fun deleteUser(id: Int): Result<Unit> = runCatching {
        val response = api.deleteUser(id)
        if (!response.isSuccessful) error("Error ${response.code()}")
    }

    override suspend fun toggleActive(id: Int): Result<Boolean> = runCatching {
        val response = api.toggleActive(id)
        if (response.isSuccessful) response.body()!!.isActive
        else error("Error ${response.code()}")
    }

    override suspend fun getStats(): Result<Map<String, Int>> = runCatching {
        val response = api.getStats()
        if (response.isSuccessful) {
            val s = response.body()!!
            mapOf(
                "total"    to s.total,
                "active"   to s.active,
                "inactive" to s.inactive,
                "staff"    to s.staff,
            )
        } else error("Error ${response.code()}")
    }
}
