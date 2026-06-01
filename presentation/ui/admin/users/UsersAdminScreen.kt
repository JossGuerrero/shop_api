package com.shopapp.presentation.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.domain.model.User
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.ErrorScreen
import com.shopapp.presentation.viewmodel.UserRoleFilter
import com.shopapp.presentation.viewmodel.UsersAdminViewModel
import com.shopapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersAdminScreen(
    viewModel: UsersAdminViewModel = hiltViewModel(),
) {
    val state     by viewModel.state.collectAsState()
    val filtered  by viewModel.filtered.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showForm    by remember { mutableStateOf(false) }
    var editTarget  by remember { mutableStateOf<User?>(null) }
    var deleteTarget by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // ── Header ────────────────────────────────────────────
        Surface(color = Surface, tonalElevation = 0.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Usuarios",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Text(
                            "${state.total} usuarios",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = viewModel::load) {
                            Icon(Icons.Default.Refresh, null, tint = TextSecondary)
                        }
                        Button(
                            onClick = { editTarget = null; showForm = true },
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Accent, contentColor = AccentOnDark,
                            ),
                            shape          = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Nuevo", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Búsqueda
                OutlinedTextField(
                    value         = state.search,
                    onValueChange = viewModel::setSearch,
                    placeholder   = { Text("Buscar usuario o email...", color = TextFaint) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = MaterialTheme.shapes.medium,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = Border,
                        cursorColor          = Accent,
                    ),
                )

                Spacer(Modifier.height(10.dp))

                // Filtros de rol
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(UserRoleFilter.entries) { filter ->
                        FilterChip(
                            selected = state.roleFilter == filter,
                            onClick  = { viewModel.setRoleFilter(filter) },
                            label    = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Accent,
                                selectedLabelColor     = AccentOnDark,
                                containerColor         = Surface2,
                                labelColor             = TextSecondary,
                            ),
                        )
                    }
                }
            }
        }

        // ── Contenido ─────────────────────────────────────────
        when {
            state.isLoading -> LoadingScreen("Cargando usuarios...")
            state.error != null -> ErrorScreen(state.error!!, onRetry = viewModel::load)
            filtered.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (state.search.isBlank() && state.roleFilter == UserRoleFilter.ALL)
                                "Sin usuarios" else "Sin resultados",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { user ->
                        UserAdminCard(
                            user         = user,
                            onToggleStaff  = { viewModel.toggleStaff(user.id, !user.isStaff) },
                            onToggleActive = { viewModel.toggleActive(user.id) },
                            onEdit         = { editTarget = user; showForm = true },
                            onDelete       = { deleteTarget = user },
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet formulario
    if (showForm) {
        UserFormSheet(
            initial   = editTarget,
            formState = formState,
            onSave    = { payload ->
                if (editTarget != null) viewModel.updateUser(editTarget!!.id, payload)
                else viewModel.createUser(payload)
            },
            onDismiss = {
                showForm   = false
                editTarget = null
                viewModel.resetFormState()
            },
        )
    }

    // Diálogo de eliminación
    deleteTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = Surface,
            shape            = MaterialTheme.shapes.large,
            title            = { Text("¿Eliminar usuario?", color = TextPrimary) },
            text             = {
                Text(
                    "\"${user.username}\" se eliminará permanentemente. Esta acción no se puede deshacer.",
                    color = TextSecondary,
                )
            },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.deleteUser(user.id)
                    deleteTarget = null
                }) { Text("Eliminar", color = Error, fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
        )
    }
}

// ── UserAdminCard ─────────────────────────────────────────────

@Composable
private fun UserAdminCard(
    user:           User,
    onToggleStaff:  () -> Unit,
    onToggleActive: () -> Unit,
    onEdit:         () -> Unit,
    onDelete:       () -> Unit,
) {
    Surface(
        shape  = MaterialTheme.shapes.large,
        color  = if (user.isActive) Surface else Surface.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .background(
                        brush = if (user.isStaff)
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Accent, AccentLight)
                            )
                        else
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Surface2, Border)
                            ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color      = if (user.isStaff) AccentOnDark else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text       = user.username,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                    // Badge staff
                    if (user.isStaff) {
                        Surface(
                            color  = Accent.copy(alpha = 0.15f),
                            shape  = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                "Staff",
                                color      = Accent,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    // Badge inactivo
                    if (!user.isActive) {
                        Surface(
                            color  = Error.copy(alpha = 0.12f),
                            shape  = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                "Inactivo",
                                color      = Error,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    text  = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    text  = "${user.numOrders} pedido${if (user.numOrders != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Acciones
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Toggle staff
                    IconButton(onClick = onToggleStaff, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (user.isStaff) Icons.Default.AdminPanelSettings
                                          else Icons.Default.PersonOutline,
                            contentDescription = if (user.isStaff) "Quitar staff" else "Dar staff",
                            tint     = if (user.isStaff) Accent else TextFaint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    // Toggle activo
                    IconButton(onClick = onToggleActive, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (user.isActive) Icons.Default.ToggleOn
                                          else Icons.Default.ToggleOff,
                            contentDescription = if (user.isActive) "Desactivar" else "Activar",
                            tint     = if (user.isActive) Success else TextFaint,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    // Editar
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit, contentDescription = "Editar",
                            tint = TextSecondary, modifier = Modifier.size(18.dp),
                        )
                    }
                    // Eliminar
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.PersonRemove, contentDescription = "Eliminar",
                            tint = Error, modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
