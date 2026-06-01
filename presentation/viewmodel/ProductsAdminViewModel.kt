// presentation/viewmodel/ProductsAdminViewModel.kt
package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.Product
import com.shopapp.domain.model.ProductFilters
import com.shopapp.domain.model.ProductPayload
import com.shopapp.domain.repository.CategoryRepository
import com.shopapp.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductStockFilter(val label: String) {
    ALL("Todos"),
    IN_STOCK("Con stock"),
    OUT_OF_STOCK("Sin stock"),
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
}

data class ProductsAdminUiState(
    val products:    List<Product>      = emptyList(),
    val isLoading:   Boolean            = false,
    val error:       String?            = null,
    val total:       Int                = 0,
    val search:      String             = "",
    val stockFilter: ProductStockFilter = ProductStockFilter.ALL,
)

sealed interface ProductFormState {
    data object Idle                       : ProductFormState
    data object Saving                     : ProductFormState
    data class  Success(val msg: String)   : ProductFormState
    data class  Error(val message: String) : ProductFormState
}

@HiltViewModel
class ProductsAdminViewModel @Inject constructor(
    private val repository:         ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsAdminUiState())
    val state: StateFlow<ProductsAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<ProductFormState>(ProductFormState.Idle)
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val filtered: StateFlow<List<Product>> = _state
        .map { s ->
            s.products
                .filter { p ->
                    s.search.isBlank() || p.name.contains(s.search, ignoreCase = true)
                }
                .filter { p ->
                    when (s.stockFilter) {
                        ProductStockFilter.ALL          -> true
                        ProductStockFilter.IN_STOCK     -> p.stock > 0
                        ProductStockFilter.OUT_OF_STOCK -> p.stock == 0
                        ProductStockFilter.ACTIVE       -> p.isActive
                        ProductStockFilter.INACTIVE     -> !p.isActive
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        load()
        loadCategories()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getProducts(ProductFilters(pageSize = 50))
                .onSuccess { (products, total) ->
                    _state.update { it.copy(products = products, total = total, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories()
                .onSuccess { _categories.value = it }
        }
    }

    fun setSearch(query: String)                   = _state.update { it.copy(search = query) }
    fun setStockFilter(filter: ProductStockFilter) = _state.update { it.copy(stockFilter = filter) }

    fun toggleActive(id: Int, isActive: Boolean) {
        _state.update { s ->
            s.copy(products = s.products.map {
                if (it.id == id) it.copy(isActive = isActive) else it
            })
        }
        viewModelScope.launch {
            val product = _state.value.products.firstOrNull { it.id == id } ?: return@launch
            repository.updateProduct(
                id, ProductPayload(
                    name        = product.name,
                    description = product.description,
                    price       = product.price,
                    stock       = product.stock,
                    isActive    = isActive,
                    categoryId  = product.categoryId ?: 0,
                )
            ).onFailure {
                _state.update { s ->
                    s.copy(products = s.products.map { p ->
                        if (p.id == id) p.copy(isActive = !isActive) else p
                    })
                }
            }
        }
    }

    fun createProduct(payload: ProductPayload) {
        _formState.value = ProductFormState.Saving
        viewModelScope.launch {
            repository.createProduct(payload)
                .onSuccess { created ->
                    _state.update { s ->
                        s.copy(products = listOf(created) + s.products, total = s.total + 1)
                    }
                    _formState.value = ProductFormState.Success("Producto creado")
                }
                .onFailure { e ->
                    _formState.value = ProductFormState.Error(e.message ?: "Error al crear")
                }
        }
    }

    fun updateProduct(id: Int, payload: ProductPayload) {
        _formState.value = ProductFormState.Saving
        viewModelScope.launch {
            repository.updateProduct(id, payload)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(products = s.products.map { if (it.id == id) updated else it })
                    }
                    _formState.value = ProductFormState.Success("Producto actualizado")
                }
                .onFailure { e ->
                    _formState.value = ProductFormState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun restock(id: Int, quantity: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            repository.restock(id, quantity)
                .onSuccess { newStock ->
                    _state.update { s ->
                        s.copy(products = s.products.map { p ->
                            if (p.id == id) p.copy(stock = newStock, inStock = newStock > 0) else p
                        })
                    }
                    onResult("Stock actualizado: $newStock unidades")
                }
                .onFailure { e ->
                    onResult("Error: ${e.message}")
                }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            products = s.products.filter { it.id != id },
                            total    = s.total - 1,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun resetFormState() {
        _formState.value = ProductFormState.Idle
    }
}
