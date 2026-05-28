package com.shopapp.domain.model

enum class OrderStatus(val value: String, val label: String) {
    PENDING("pending", "Pendiente"),
    CONFIRMED("confirmed", "Confirmado"),
    SHIPPED("shipped", "Enviado"),
    DELIVERED("delivered", "Entregado"),
    CANCELLED("cancelled", "Cancelado");

    companion object {
        fun fromValue(value: String): OrderStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}

data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
)

data class Order(
    val id: Int,
    val username: String,
    val status: OrderStatus,
    val total: Double,
    val numItems: Int,
    val items: List<OrderItem>,
    val createdAt: String,
    val updatedAt: String,
)