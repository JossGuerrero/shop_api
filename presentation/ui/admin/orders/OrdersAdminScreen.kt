package com.shopapp.presentation.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.shopapp.domain.model.Order
import com.shopapp.domain.model.OrderStatus
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.ErrorScreen
import com.shopapp.presentation.viewmodel.OrdersAdminViewModel
import com.shopapp.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

private val STATUS_FILTERS = listOf(
    "" to "Todos",
    OrderStatus.PENDING.value   to "Pendientes",
    OrderStatus.CONFIRMED.value to "Confirmados",
    OrderStatus.SHIPPED.value   to "Enviados",
    OrderStatus.DELIVERED.value to "Entregados",
    OrderStatus.CANCELLED.value to "Cancelados",
)

@Composable
fun OrdersAdminScreen(
    onOrderDetail: (Int) -> Unit,
    viewModel:     OrdersAdminViewModel = hiltViewModel(),
) {
    val state     by viewModel.state.collectAsState()
    val listState  = rememberLazyListState()

    // Paginación automática
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 2 && !state.isLoadingMore && state.hasMore
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadMore() }

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
                            "Pedidos",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Text(
                            "${state.total} pedidos",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, null, tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Filtros por estado
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(STATUS_FILTERS) { (value, label) ->
                        FilterChip(
                            selected = state.statusFilter == value,
                            onClick  = { viewModel.setStatusFilter(value) },
                            label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
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
            state.isLoading && state.orders.isEmpty() ->
                LoadingScreen("Cargando pedidos...")

            state.error != null && state.orders.isEmpty() ->
                ErrorScreen(state.error!!, onRetry = viewModel::refresh)

            state.orders.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛍️", fontSize = 52.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text       = "Sin pedidos",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Text(
                            text  = if (state.statusFilter.isBlank()) "Aún no hay pedidos"
                                    else "Sin pedidos con este estado",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    state          = listState,
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        OrderAdminCard(
                            order    = order,
                            onStatus = { newStatus -> viewModel.changeStatus(order.id, newStatus) },
                            onClick  = { onOrderDetail(order.id) },
                        )
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color       = Accent,
                                    modifier    = Modifier.size(28.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── OrderAdminCard ────────────────────────────────────────────

@Composable
private fun OrderAdminCard(
    order:    Order,
    onStatus: (OrderStatus) -> Unit,
    onClick:  () -> Unit,
) {
    val dateFmt   = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val inputFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    val dateStr   = runCatching { dateFmt.format(inputFmt.parse(order.createdAt)!!) }
                        .getOrDefault(order.createdAt.take(10))

    Surface(
        onClick = onClick,
        shape   = MaterialTheme.shapes.large,
        color   = Surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Primera fila: ID + cliente + fecha
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                Column {
                    Text(
                        text       = "Pedido #${order.id}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Text(
                        text  = "${order.username} · $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                // Selector de estado inline
                StatusDropdown(
                    current  = order.status,
                    onChange = onStatus,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Preview de ítems
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.fillMaxWidth(),
            ) {
                order.items.take(2).forEach { item ->
                    Surface(
                        color    = Surface2,
                        shape    = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text     = "${item.quantity}× ${item.productName}",
                            style    = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color    = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                        )
                    }
                }
                if (order.items.size > 2) {
                    Text(
                        text  = "+${order.items.size - 2} más",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // Footer: ítems + total + ver detalle
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "${order.numItems} producto${if (order.numItems != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text       = "$${"%.2f".format(order.total)}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Accent,
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Ver detalle",
                        tint     = TextFaint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
