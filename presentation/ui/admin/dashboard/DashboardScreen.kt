// presentation/ui/admin/dashboard/DashboardScreen.kt
package com.shopapp.presentation.ui.admin.dashboard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.domain.model.OrderStatus
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.orderStatusColor
import com.shopapp.presentation.viewmodel.DashboardUiState
import com.shopapp.presentation.viewmodel.DashboardViewModel
import com.shopapp.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel:  DashboardViewModel = hiltViewModel(),
) {
    val state       by viewModel.state.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()

    when (val s = state) {
        is DashboardUiState.Loading ->
            LoadingScreen("Cargando dashboard...")
        is DashboardUiState.Error   -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️ ${s.message}", color = Error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                        Text("Reintentar", color = AccentOnDark)
                    }
                }
            }
        }
        is DashboardUiState.Success ->
            DashboardContent(
                stats       = s.stats,
                lastUpdated = lastUpdated,
                onNavigate  = onNavigate,
                onRefresh   = viewModel::load,
            )
    }
}

@Composable
private fun DashboardContent(
    stats:       com.shopapp.presentation.viewmodel.DashboardStats,
    lastUpdated: Long,
    onNavigate:  (String) -> Unit,
    onRefresh:   () -> Unit,
) {
    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = if (lastUpdated > 0) timeFmt.format(Date(lastUpdated)) else "—"

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text       = "Dashboard",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Text(
                        text  = "Actualizado: $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint,
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Accent)
                }
            }
        }

        // ── KPIs — fila 1 ─────────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title    = "Productos activos",
                    value    = stats.totalActiveProducts.toString(),
                    subtitle = if (stats.outOfStockProducts > 0)
                               "${stats.outOfStockProducts} sin stock" else null,
                    icon     = Icons.Default.Inventory,
                    color    = Accent,
                    hasAlert = stats.outOfStockProducts > 0,
                    onClick  = { onNavigate("admin/products") },
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title   = "Categorías activas",
                    value   = stats.activeCategories.toString(),
                    subtitle = "${stats.totalCategories} total",
                    icon    = Icons.Default.Category,
                    color   = Info,
                    onClick = { onNavigate("admin/categories") },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── KPIs — fila 2 ─────────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title    = "Pedidos totales",
                    value    = stats.totalOrders.toString(),
                    subtitle = if (stats.pendingOrders > 0)
                               "${stats.pendingOrders} pendientes" else null,
                    icon     = Icons.Default.ShoppingBag,
                    color    = Success,
                    hasAlert = stats.pendingOrders > 0,
                    onClick  = { onNavigate("admin/orders") },
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title    = "Usuarios activos",
                    value    = stats.activeUsers.toString(),
                    subtitle = "${stats.totalUsers} registrados",
                    icon     = Icons.Default.People,
                    color    = Warning,
                    onClick  = { onNavigate("admin/users") },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── KPIs financieros ──────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title    = "Facturación total",
                    value    = "$${"%.0f".format(stats.totalRevenue)}",
                    icon     = Icons.Default.TrendingUp,
                    color    = Accent,
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title    = "Precio medio",
                    value    = "$${"%.2f".format(stats.avgPrice)}",
                    subtitle = "por producto",
                    icon     = Icons.Default.Sell,
                    color    = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Pedidos por estado — gráfica de barras ─────────────
        if (stats.ordersByStatus.isNotEmpty()) {
            item {
                Surface(
                    color    = Surface,
                    shape    = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text       = "Pedidos por estado",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            TextButton(onClick = { onNavigate("admin/orders") }) {
                                Text("Ver todos", color = Accent,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        val total = stats.totalOrders.coerceAtLeast(1)
                        stats.ordersByStatus.entries.forEach { (statusValue, count) ->
                            val status = OrderStatus.fromValue(statusValue)
                            val color  = orderStatusColor(status)
                            val pct    = (count.toFloat() / total).coerceIn(0.02f, 1f)

                            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text  = status.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                    Text(
                                        text       = count.toString(),
                                        style      = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color      = color,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .background(Surface2, MaterialTheme.shapes.extraSmall),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(pct)
                                            .fillMaxHeight()
                                            .background(color, MaterialTheme.shapes.extraSmall),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Productos con stock bajo ───────────────────────────
        item {
            Surface(
                color    = Surface,
                shape    = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint    = Warning,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Stock bajo",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                        }
                        TextButton(onClick = { onNavigate("admin/products") }) {
                            Text("Gestionar", color = Accent,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (stats.lowStockProducts.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✅ Sin alertas de stock", color = Success,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        stats.lowStockProducts.forEach { product ->
                            Surface(
                                onClick  = { onNavigate("admin/products") },
                                color    = Surface2,
                                shape    = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            ) {
                                Row(
                                    modifier              = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text     = product.name,
                                        style    = MaterialTheme.typography.bodySmall,
                                        color    = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                    )
                                    Surface(
                                        color = if (product.stock == 0)
                                                    Error.copy(alpha = 0.15f)
                                                else Warning.copy(alpha = 0.15f),
                                        shape = MaterialTheme.shapes.extraSmall,
                                    ) {
                                        Text(
                                            text       = if (product.stock == 0) "Agotado"
                                                         else "${product.stock} uds.",
                                            color      = if (product.stock == 0) Error else Warning,
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Acciones rápidas ──────────────────────────────────
        item {
            Surface(
                color    = Surface,
                shape    = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "⚡ Acciones rápidas",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(bottom = 12.dp),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            Triple("+ Categoría", Info,    "admin/categories"),
                            Triple("+ Producto",  Accent,  "admin/products"),
                            Triple("Ver pedidos", Success, "admin/orders"),
                            Triple("Usuarios",    Warning, "admin/users"),
                        )) { (label, color, route) ->
                            Surface(
                                onClick  = { onNavigate(route) },
                                color    = color.copy(alpha = 0.1f),
                                shape    = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text       = label,
                                    color      = color,
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.bodySmall,
                                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
