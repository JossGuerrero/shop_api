package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.shopapp.domain.model.Product
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CartItem(
    val product:  Product,
    val quantity: Int,
)

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val totalItems: StateFlow<Int> = _items
        .map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val subtotal: StateFlow<Double> = _items
        .map { list -> list.sumOf { it.product.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalWithTax: StateFlow<Double> = _items
        .map { list -> list.sumOf { it.product.priceWithTax * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

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
        _items.update { list -> list.filter { it.product.id != productId } }
    }

    fun clearCart() { _items.value = emptyList() }
}
