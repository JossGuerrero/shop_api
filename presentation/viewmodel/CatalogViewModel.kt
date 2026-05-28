package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.Product
import com.shopapp.domain.model.ProductFilters
import com.shopapp.domain.repository.CategoryRepository
import com.shopapp.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CatalogUiState(
    val products:         List<Product> = emptyList(),
    val categories:       List<Category> = emptyList(),
    val isLoading:        Boolean = false,
    val isLoadingMore:    Boolean = false,
    val error:            String? = null,
    val total:            Int     = 0,
    val hasMore:          Boolean = false,
    val search:           String  = "",
    val selectedCategory: Int?    = null,
    val ordering:         String  = "",
    val page:             Int     = 1,
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val productRepository:  ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init { loadCategories(); load() }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().onSuccess { cats ->
                _state.update { it.copy(categories = cats.filter { c -> c.isActive }) }
            }
        }
    }

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
            val filters = ProductFilters(
                search   = current.search.ifBlank { null },
                category = current.selectedCategory,
                ordering = current.ordering.ifBlank { null },
                isActive = true,
                page     = page,
                pageSize = 12,
            )
            productRepository.getProducts(filters)
                .onSuccess { (products, total) ->
                    _state.update { s ->
                        s.copy(
                            products      = if (reset) products else s.products + products,
                            total         = total,
                            hasMore       = (if (reset) products else s.products + products).size < total,
                            isLoading     = false,
                            isLoadingMore = false,
                            page          = page + 1,
                            error         = null,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message) }
                }
        }
    }

    fun setSearch(query: String) {
        _state.update { it.copy(search = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            load(reset = true)
        }
    }

    fun setCategory(id: Int?) {
        _state.update { it.copy(selectedCategory = id) }
        load(reset = true)
    }

    fun setOrdering(ordering: String) {
        _state.update { it.copy(ordering = ordering) }
        load(reset = true)
    }

    fun loadMore() = load(reset = false)
    fun refresh()  = load(reset = true)
}
