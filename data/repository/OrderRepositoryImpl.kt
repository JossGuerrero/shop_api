package com.shopapp.data.repository

import com.shopapp.data.remote.api.OrderApi
import com.shopapp.data.remote.dto.AddItemRequestDto
import com.shopapp.data.remote.dto.UpdateStatusRequestDto
import com.shopapp.data.remote.dto.toDomain
import com.shopapp.domain.model.Order
import com.shopapp.domain.model.OrderStatus
import com.shopapp.domain.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi,
) : OrderRepository {

    override suspend fun getOrders(page: Int?, status: String?): Result<Pair<List<Order>, Int>> =
        runCatching {
            val response = api.getOrders(page = page, status = status)
            if (response.isSuccessful) {
                val body = response.body()!!
                Pair(body.results.map { it.toDomain() }, body.count)
            } else error("Error ${response.code()}")
        }

    override suspend fun getOrder(id: Int): Result<Order> = runCatching {
        val response = api.getOrder(id)
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}")
    }

    override suspend fun createOrder(): Result<Order> = runCatching {
        val response = api.createOrder()
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}: ${response.errorBody()?.string()}")
    }

    override suspend fun addItem(orderId: Int, productId: Int, quantity: Int): Result<Order> =
        runCatching {
            val response = api.addItem(orderId, AddItemRequestDto(productId, quantity))
            if (response.isSuccessful) response.body()!!.toDomain()
            else error("Error ${response.code()}: ${response.errorBody()?.string()}")
        }

    override suspend fun confirmOrder(orderId: Int): Result<Order> = runCatching {
        val response = api.confirmOrder(orderId)
        if (response.isSuccessful) response.body()!!.toDomain()
        else error("Error ${response.code()}: ${response.errorBody()?.string()}")
    }

    override suspend fun updateStatus(orderId: Int, status: OrderStatus): Result<Order> =
        runCatching {
            val response = api.updateStatus(orderId, UpdateStatusRequestDto(status.value))
            if (response.isSuccessful) response.body()!!.toDomain()
            else error("Error ${response.code()}")
        }

    override suspend fun getStats(): Result<Map<String, Any>> = runCatching {
        val response = api.getStats()
        if (response.isSuccessful) {
            val s = response.body()!!
            mapOf(
                "total_orders"  to s.totalOrders,
                "total_revenue" to s.totalRevenue,
                "by_status"     to s.byStatus,
            )
        } else error("Error ${response.code()}")
    }
}
