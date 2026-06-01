// presentation/ui/admin/products/ProductsAdminScreen.kt
package com.shopapp.presentation.ui.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.shopapp.domain.model.Product
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.ErrorScreen
import com.shopapp.presentation.viewmodel.ProductStockFilter
import com.shopapp.presentation.viewmodel.ProductsAdminViewModel
import com.shopapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsAdminScreen(
    viewModel: ProductsAdminViewModel = hiltViewModel(),
) {
    val state      by viewModel.state.collectAsState()
    val filtered   by viewModel.filtered.collectAsState()
    val formState  by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showForm      by remember { mutableStateOf(false) }
    var editTarget    by remember { mutableStateOf<Product?>(null) }
    var restockTarget by remember { mutableStateOf<Product?>(null) }
    var deleteTarget  by remember { mutableStateOf<Product?>(null) }
    var snackMsg      by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            snackMsg = null
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
        ) {
            Surface(color = Surface, tonalElevation = 0.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "Productos",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Text(
                                "${state.total} productos",
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
                                    containerColor = Accent,
                                    contentColor   = AccentOnDark,
                                ),
                                shape          = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Nuevo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value         = state.search,
                        onValueChange = viewModel::setSearch,
                        placeholder   = { Text("Buscar producto...", color = TextFaint) },
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

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ProductStockFilter.entries) { filter ->
                            FilterChip(
                                selected = state.stockFilter == filter,
                                onClick  = { viewModel.setStockFilter(filter) },
                                label    = {
                                    Text(filter.label, style = MaterialTheme.typography.labelSmall)
                                },
                                colors = FilterChipDefaults.filterChipColors(
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

            when {
                state.isLoading     -> LoadingScreen("Cargando productos...")
                state.error != null -> ErrorScreen(state.error!!, onRetry = viewModel::load)
                filtered.isEmpty()  -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (state.search.isBlank() && state.stockFilter == ProductStockFilter.ALL)
                                    "Sin productos"
                                else
                                    "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filtered, key = { it.id }) { product ->
                            ProductAdminCard(
                                product   = product,
                                onToggle  = { viewModel.toggleActive(product.id, !product.isActive) },
                                onEdit    = { editTarget = product; showForm = true },
                                onRestock = { restockTarget = product },
                                onDelete  = { deleteTarget = product },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        ProductFormSheet(
            initial    = editTarget,
            categories = categories,
            formState  = formState,
            onSave     = { payload ->
                if (editTarget != null) viewModel.updateProduct(editTarget!!.id, payload)
                else viewModel.createProduct(payload)
            },
            onDismiss = {
                showForm   = false
                editTarget = null
                viewModel.resetFormState()
            },
        )
    }

    restockTarget?.let { product ->
        RestockDialog(
            product   = product,
            onRestock = { qty ->
                viewModel.restock(product.id, qty) { msg ->
                    snackMsg      = msg
                    restockTarget = null
                }
            },
            onDismiss = { restockTarget = null },
        )
    }

    deleteTarget?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = Surface,
            shape            = MaterialTheme.shapes.large,
            title            = { Text("¿Eliminar producto?", color = TextPrimary) },
            text             = {
                Text(
                    "\"${product.name}\" se eliminará permanentemente.",
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(product.id)
                    deleteTarget = null
                }) {
                    Text("Eliminar", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun ProductAdminCard(
    product:   Product,
    onToggle:  () -> Unit,
    onEdit:    () -> Unit,
    onRestock: () -> Unit,
    onDelete:  () -> Unit,
) {
    Surface(
        shape    = MaterialTheme.shapes.large,
        color    = if (product.isActive) Surface else Surface.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .background(Surface2, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model              = product.imageUrl,
                        contentDescription = product.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Text("📦", fontSize = 22.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text  = product.categoryName ?: "Sin categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        "$${"%.2f".format(product.price)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Accent,
                    )
                    Surface(
                        color = when {
                            product.stock == 0 -> Error.copy(alpha = 0.15f)
                            product.stock < 5  -> Warning.copy(alpha = 0.15f)
                            else               -> Success.copy(alpha = 0.12f)
                        },
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = when {
                                product.stock == 0 -> "Agotado"
                                else               -> "${product.stock} uds."
                            },
                            color = when {
                                product.stock == 0 -> Error
                                product.stock < 5  -> Warning
                                else               -> Success
                            },
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked         = product.isActive,
                    onCheckedChange = { onToggle() },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor    = AccentOnDark,
                        checkedTrackColor    = Accent,
                        uncheckedTrackColor  = Surface2,
                        uncheckedBorderColor = Border,
                    ),
                    modifier = Modifier.size(width = 48.dp, height = 28.dp),
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    IconButton(onClick = onRestock, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Inventory, null,
                            tint     = Accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit, null,
                            tint     = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete, null,
                            tint     = Error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
