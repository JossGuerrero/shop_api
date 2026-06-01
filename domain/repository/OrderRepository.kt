package com.shopapp.domain.repository

import com.shopapp.domain.model.Order
import com.shopapp.domain.model.OrderStatus

interface OrderRepository {
    suspend fun getOrders(page: Int? = null, status: String? = null): Result<Pair<List<Order>, Int>>
    suspend fun getOrder(id: Int): Result<Order>
    suspend fun createOrder(): Result<Order>
    suspend fun addItem(orderId: Int, productId: Int, quantity: Int): Result<Order>
    suspend fun confirmOrder(orderId: Int): Result<Order>
    suspend fun updateStatus(orderId: Int, status: OrderStatus): Result<Order>
    suspend fun getStats(): Result<Map<String, Any>>
}
