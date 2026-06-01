package com.shopapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.domain.model.User
import com.shopapp.domain.model.UserPayload
import com.shopapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class UserRoleFilter(val label: String) {
    ALL("Todos"),
    CLIENTS("Clientes"),
    STAFF("Staff"),
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
}

data class UsersAdminUiState(
    val users:      List<User>     = emptyList(),
    val isLoading:  Boolean        = false,
    val error:      String?        = null,
    val total:      Int            = 0,
    val search:     String         = "",
    val roleFilter: UserRoleFilter = UserRoleFilter.ALL,
)

sealed interface UserFormState {
    data object Idle                       : UserFormState
    data object Saving                     : UserFormState
    data class  Success(val msg: String)   : UserFormState
    data class  Error(val message: String) : UserFormState
}

@HiltViewModel
class UsersAdminViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersAdminUiState())
    val state: StateFlow<UsersAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<UserFormState>(UserFormState.Idle)
    val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    // Filtrado local combinado
    val filtered: StateFlow<List<User>> = _state
        .map { s ->
            s.users
                .filter { u ->
                    s.search.isBlank() ||
                    u.username.contains(s.search, ignoreCase = true) ||
                    u.email.contains(s.search, ignoreCase = true)
                }
                .filter { u ->
                    when (s.roleFilter) {
                        UserRoleFilter.ALL      -> true
                        UserRoleFilter.CLIENTS  -> !u.isStaff
                        UserRoleFilter.STAFF    -> u.isStaff
                        UserRoleFilter.ACTIVE   -> u.isActive
                        UserRoleFilter.INACTIVE -> !u.isActive
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var searchJob: Job? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getUsers()
                .onSuccess { (users, total) ->
                    _state.update { it.copy(users = users, total = total, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun setSearch(query: String) {
        _state.update { it.copy(search = query) }
        // Debounce para búsqueda local (ya es instantánea, pero útil si se cambia a API)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            // Búsqueda ya aplicada por el filtered StateFlow
        }
    }

    fun setRoleFilter(filter: UserRoleFilter) {
        _state.update { it.copy(roleFilter = filter) }
    }

    // Toggle staff — optimista
    fun toggleStaff(id: Int, isStaff: Boolean) {
        _state.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == id) u.copy(isStaff = isStaff) else u
            })
        }
        viewModelScope.launch {
            val user = _state.value.users.first { it.id == id }
            repository.updateUser(id, UserPayload(
                username  = user.username,
                email     = user.email,
                firstName = user.firstName,
                lastName  = user.lastName,
                isStaff   = isStaff,
                isActive  = user.isActive,
            )).onFailure {
                // Revertir
                _state.update { s ->
                    s.copy(users = s.users.map { u ->
                        if (u.id == id) u.copy(isStaff = !isStaff) else u
                    })
                }
            }
        }
    }

    // Toggle activo — optimista
    fun toggleActive(id: Int) {
        val user = _state.value.users.find { it.id == id } ?: return
        val next = !user.isActive
        _state.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == id) u.copy(isActive = next) else u
            })
        }
        viewModelScope.launch {
            repository.toggleActive(id)
                .onSuccess { serverActive ->
                    _state.update { s ->
                        s.copy(users = s.users.map { u ->
                            if (u.id == id) u.copy(isActive = serverActive) else u
                        })
                    }
                }
                .onFailure {
                    // Revertir
                    _state.update { s ->
                        s.copy(users = s.users.map { u ->
                            if (u.id == id) u.copy(isActive = !next) else u
                        })
                    }
                }
        }
    }

    fun createUser(payload: UserPayload) {
        _formState.value = UserFormState.Saving
        viewModelScope.launch {
            repository.createUser(payload)
                .onSuccess { created ->
                    _state.update { s ->
                        s.copy(users = listOf(created) + s.users, total = s.total + 1)
                    }
                    _formState.value = UserFormState.Success("Usuario creado")
                }
                .onFailure { e ->
                    _formState.value = UserFormState.Error(e.message ?: "Error al crear")
                }
        }
    }

    fun updateUser(id: Int, payload: UserPayload) {
        _formState.value = UserFormState.Saving
        viewModelScope.launch {
            repository.updateUser(id, payload)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(users = s.users.map { if (it.id == id) updated else it })
                    }
                    _formState.value = UserFormState.Success("Usuario actualizado")
                }
                .onFailure { e ->
                    _formState.value = UserFormState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            repository.deleteUser(id)
                .onSuccess {
                    _state.update { s ->
                        s.copy(users = s.users.filter { it.id != id }, total = s.total - 1)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun resetFormState() { _formState.value = UserFormState.Idle }
}
