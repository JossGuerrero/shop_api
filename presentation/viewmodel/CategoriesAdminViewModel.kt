// presentation/viewmodel/CategoriesAdminViewModel.kt
package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.CategoryPayload
import com.shopapp.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesAdminUiState(
    val categories: List<Category> = emptyList(),
    val isLoading:  Boolean        = false,
    val error:      String?        = null,
    val search:     String         = "",
)

sealed interface CategoryFormState {
    data object Idle                       : CategoryFormState
    data object Saving                     : CategoryFormState
    data class  Success(val msg: String)   : CategoryFormState
    data class  Error(val message: String) : CategoryFormState
}

@HiltViewModel
class CategoriesAdminViewModel @Inject constructor(
    private val repository: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesAdminUiState())
    val state: StateFlow<CategoriesAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<CategoryFormState>(CategoryFormState.Idle)
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    val filtered: StateFlow<List<Category>> = _state
        .map { s ->
            if (s.search.isBlank()) s.categories
            else s.categories.filter {
                it.name.contains(s.search, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getCategories()
                .onSuccess { cats ->
                    _state.update { it.copy(categories = cats, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun setSearch(query: String) {
        _state.update { it.copy(search = query) }
    }

    // Toggle optimista
    fun toggleActive(id: Int, isActive: Boolean) {
        _state.update { s ->
            s.copy(categories = s.categories.map {
                if (it.id == id) it.copy(isActive = isActive) else it
            })
        }
        viewModelScope.launch {
            repository.updateCategory(id, getCategoryPayload(id, isActive))
                .onFailure {
                    // Revertir
                    _state.update { s ->
                        s.copy(categories = s.categories.map { cat ->
                            if (cat.id == id) cat.copy(isActive = !isActive) else cat
                        })
                    }
                }
        }
    }

    private fun getCategoryPayload(id: Int, isActive: Boolean): CategoryPayload {
        val cat = _state.value.categories.first { it.id == id }
        return CategoryPayload(cat.name, cat.slug, cat.description, isActive)
    }

    fun createCategory(payload: CategoryPayload) {
        _formState.value = CategoryFormState.Saving
        viewModelScope.launch {
            repository.createCategory(payload)
                .onSuccess { created ->
                    _state.update { s ->
                        s.copy(categories = listOf(created) + s.categories)
                    }
                    _formState.value = CategoryFormState.Success("Categoría creada")
                }
                .onFailure { e ->
                    _formState.value = CategoryFormState.Error(e.message ?: "Error al crear")
                }
        }
    }

    fun updateCategory(id: Int, payload: CategoryPayload) {
        _formState.value = CategoryFormState.Saving
        viewModelScope.launch {
            repository.updateCategory(id, payload)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(categories = s.categories.map {
                            if (it.id == id) updated else it
                        })
                    }
                    _formState.value = CategoryFormState.Success("Categoría actualizada")
                }
                .onFailure { e ->
                    _formState.value = CategoryFormState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
                .onSuccess {
                    _state.update { s ->
                        s.copy(categories = s.categories.filter { it.id != id })
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun resetFormState() { _formState.value = CategoryFormState.Idle }
}
