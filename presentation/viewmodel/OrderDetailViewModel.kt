package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.Order
import com.shopapp.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OrderDetailUiState {
    data object Loading                    : OrderDetailUiState
    data class  Success(val order: Order)  : OrderDetailUiState
    data class  Error(val message: String) : OrderDetailUiState
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val repository: OrderRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    fun load(id: Int) {
        viewModelScope.launch {
            _state.value = OrderDetailUiState.Loading
            repository.getOrder(id)
                .onSuccess { _state.value = OrderDetailUiState.Success(it) }
                .onFailure { _state.value = OrderDetailUiState.Error(it.message ?: "Error") }
        }
    }
}
