package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.Product
import com.shopapp.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product:  Product,
    val quantity: Int,
)

sealed interface CheckoutState {
    data object Idle                          : CheckoutState
    data object Loading                       : CheckoutState
    data class  Success(val orderId: Int)     : CheckoutState
    data class  Error(val message: String)   : CheckoutState
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _items         = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val totalItems: StateFlow<Int> = _items
        .map { it.sumOf { i -> i.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val subtotal: StateFlow<Double> = _items
        .map { it.sumOf { i -> i.product.price * i.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalWithTax: StateFlow<Double> = _items
        .map { it.sumOf { i -> i.product.priceWithTax * i.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    // ── CRUD del carrito ──────────────────────────────────────

    fun addItem(product: Product, quantity: Int = 1) {
        _items.update { list ->
            val existing = list.find { it.product.id == product.id }
            if (existing != null) {
                list.map {
                    if (it.product.id == product.id)
                        it.copy(quantity = minOf(it.quantity + quantity, product.stock))
                    else it
                }
            } else {
                list + CartItem(product, quantity)
            }
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) removeItem(productId)
        else _items.update { list ->
            list.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
        }
    }

    fun removeItem(productId: Int) {
        _items.update { it.filter { i -> i.product.id != productId } }
    }

    fun clearCart() { _items.value = emptyList() }

    fun resetCheckout() { _checkoutState.value = CheckoutState.Idle }

    // ── Checkout — 3 pasos ────────────────────────────────────

    fun checkout() {
        val currentItems = _items.value
        if (currentItems.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("El carrito está vacío")
            return
        }
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading

            // 1. Crear pedido vacío
            val order = orderRepository.createOrder().getOrElse {
                _checkoutState.value = CheckoutState.Error(it.message ?: "Error al crear pedido")
                return@launch
            }

            // 2. Añadir cada ítem
            for (item in currentItems) {
                orderRepository.addItem(order.id, item.product.id, item.quantity).getOrElse {
                    _checkoutState.value = CheckoutState.Error("Error al añadir ${item.product.name}")
                    return@launch
                }
            }

            // 3. Confirmar
            val confirmed = orderRepository.confirmOrder(order.id).getOrElse {
                _checkoutState.value = CheckoutState.Error(it.message ?: "Error al confirmar")
                return@launch
            }

            clearCart()
            _checkoutState.value = CheckoutState.Success(confirmed.id)
        }
    }
}
