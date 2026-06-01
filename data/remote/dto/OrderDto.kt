package com.shopapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.shopapp.domain.model.*

data class OrderDto(
    val id: Int,
    @SerializedName("user_name") val username: String,
    val total: Double,
    val status: String,
    @SerializedName("num_items") val numItems: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val items: List<OrderItemDto> = emptyList()
)

data class OrderItemDto(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    val subtotal: Double
)

data class OrderListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<OrderDto>
)

data class AddItemRequestDto(
    @SerializedName("product_id") val productId: Int,
    val quantity: Int
)

data class UpdateStatusRequestDto(
    val status: String
)

data class OrderStatsDto(
    @SerializedName("total_orders") val totalOrders: Int,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("by_status") val byStatus: Map<String, Int>
)

fun OrderDto.toDomain() = Order(
    id = id,
    username = username,
    status = OrderStatus.fromValue(status),
    total = total,
    numItems = numItems,
    items = items.map { it.toDomain() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun OrderItemDto.toDomain() = OrderItem(
    id = id,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    subtotal = subtotal
)
