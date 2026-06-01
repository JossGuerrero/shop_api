// presentation/ui/admin/categories/CategoriesAdminScreen.kt
package com.shopapp.presentation.ui.admin.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.shopapp.domain.model.Category
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.ErrorScreen
import com.shopapp.presentation.viewmodel.CategoriesAdminViewModel
import com.shopapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesAdminScreen(
    viewModel: CategoriesAdminViewModel = hiltViewModel(),
) {
    val state     by viewModel.state.collectAsState()
    val filtered  by viewModel.filtered.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showForm    by remember { mutableStateOf(false) }
    var editTarget  by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

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
                            text       = "Categorías",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Text(
                            text  = "${state.categories.size} categorías",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Button(
                        onClick = { editTarget = null; showForm = true },
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor   = AccentOnDark,
                        ),
                        shape    = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Nueva", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Búsqueda
                OutlinedTextField(
                    value         = state.search,
                    onValueChange = viewModel::setSearch,
                    placeholder   = { Text("Buscar categoría...", color = TextFaint) },
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
            }
        }

        // ── Contenido ─────────────────────────────────────────
        when {
            state.isLoading -> LoadingScreen("Cargando categorías...")
            state.error != null -> ErrorScreen(state.error!!, onRetry = viewModel::load)
            filtered.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏷️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (state.search.isBlank()) "Sin categorías" else "Sin resultados",
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
                    items(filtered, key = { it.id }) { category ->
                        CategoryAdminCard(
                            category  = category,
                            onToggle  = { viewModel.toggleActive(category.id, !category.isActive) },
                            onEdit    = { editTarget = category; showForm = true },
                            onDelete  = { deleteTarget = category },
                        )
                    }
                }
            }
        }
    }

    // ── Bottom Sheet de formulario ────────────────────────────
    if (showForm) {
        CategoryFormSheet(
            initial   = editTarget,
            formState = formState,
            onSave    = { payload ->
                if (editTarget != null) viewModel.updateCategory(editTarget!!.id, payload)
                else viewModel.createCategory(payload)
            },
            onDismiss = {
                showForm   = false
                editTarget = null
                viewModel.resetFormState()
            },
        )
    }

    // ── Diálogo de confirmación de eliminación ─────────────────
    deleteTarget?.let { cat ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title            = {
                Text(
                    if (cat.totalProducts > 0) "¿Desactivar categoría?"
                    else "¿Eliminar categoría?",
                    color = TextPrimary,
                )
            },
            text             = {
                Text(
                    if (cat.totalProducts > 0)
                        "\"${cat.name}\" tiene ${cat.totalProducts} producto(s). Se desactivará en lugar de eliminarse."
                    else
                        "\"${cat.name}\" se eliminará permanentemente.",
                    color = TextSecondary,
                )
            },
            confirmButton    = {
                TextButton(onClick = {
                    if (cat.totalProducts > 0) viewModel.toggleActive(cat.id, false)
                    else viewModel.deleteCategory(cat.id)
                    deleteTarget = null
                }) {
                    Text(
                        if (cat.totalProducts > 0) "Desactivar" else "Eliminar",
                        color      = Error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton    = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor   = Surface,
            shape            = MaterialTheme.shapes.large,
        )
    }
}

// ── CategoryAdminCard ─────────────────────────────────────────

@Composable
private fun CategoryAdminCard(
    category: Category,
    onToggle: () -> Unit,
    onEdit:   () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape  = MaterialTheme.shapes.large,
        color  = Surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Toggle
            Switch(
                checked         = category.isActive,
                onCheckedChange = { onToggle() },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor    = AccentOnDark,
                    checkedTrackColor    = Accent,
                    uncheckedTrackColor  = Surface2,
                    uncheckedBorderColor = Border,
                ),
            )

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text       = category.name,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                    if (!category.isActive) {
                        Surface(
                            color  = Error.copy(alpha = 0.12f),
                            shape  = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                "Inactiva",
                                color    = Error,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    text  = "/${category.slug}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextFaint,
                )
                Text(
                    text  = "${category.totalProducts} productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Acciones
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
