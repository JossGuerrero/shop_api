package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.Order
import com.shopapp.domain.model.OrderStatus
import com.shopapp.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersAdminUiState(
    val orders:       List<Order> = emptyList(),
    val isLoading:    Boolean     = false,
    val isLoadingMore:Boolean     = false,
    val error:        String?     = null,
    val total:        Int         = 0,
    val hasMore:      Boolean     = false,
    val statusFilter: String      = "",
    val page:         Int         = 1,
)

@HiltViewModel
class OrdersAdminViewModel @Inject constructor(
    private val repository: OrderRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersAdminUiState())
    val state: StateFlow<OrdersAdminUiState> = _state.asStateFlow()

    init { load() }

    fun load(reset: Boolean = true) {
        val current = _state.value
        val page    = if (reset) 1 else current.page

        if (reset) {
            _state.update { it.copy(isLoading = true, error = null, page = 1) }
        } else {
            if (current.isLoadingMore || !current.hasMore) return
            _state.update { it.copy(isLoadingMore = true) }
        }

        viewModelScope.launch {
            repository.getOrders(
                page   = page,
                status = current.statusFilter.ifBlank { null },
            ).onSuccess { (orders, total) ->
                _state.update { s ->
                    s.copy(
                        orders        = if (reset) orders else s.orders + orders,
                        total         = total,
                        hasMore       = (if (reset) orders else s.orders + orders).size < total,
                        isLoading     = false,
                        isLoadingMore = false,
                        page          = page + 1,
                        error         = null,
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message) }
            }
        }
    }

    fun setStatusFilter(status: String) {
        _state.update { it.copy(statusFilter = status) }
        load(reset = true)
    }

    fun loadMore() = load(reset = false)
    fun refresh()  = load(reset = true)

    // Cambio de estado con actualización optimista
    fun changeStatus(orderId: Int, newStatus: OrderStatus) {
        val prevStatus = _state.value.orders.find { it.id == orderId }?.status ?: return

        // Actualizar optimistamente
        _state.update { s ->
            s.copy(orders = s.orders.map { o ->
                if (o.id == orderId) o.copy(status = newStatus) else o
            })
        }

        viewModelScope.launch {
            repository.updateStatus(orderId, newStatus)
                .onFailure {
                    // Revertir si falla
                    _state.update { s ->
                        s.copy(orders = s.orders.map { o ->
                            if (o.id == orderId) o.copy(status = prevStatus) else o
                        })
                    }
                }
        }
    }
}
