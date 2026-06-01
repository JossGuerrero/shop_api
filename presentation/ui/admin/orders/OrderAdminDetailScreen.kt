package com.shopapp.presentation.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.shopapp.domain.model.OrderItem
import com.shopapp.domain.model.OrderStatus
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.ErrorScreen
import com.shopapp.presentation.viewmodel.OrderDetailUiState
import com.shopapp.presentation.viewmodel.OrderDetailViewModel
import com.shopapp.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAdminDetailScreen(
    orderId:    Int,
    onBack:     () -> Unit,
    onStatusChange: (Int, OrderStatus) -> Unit,
    viewModel:  OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    when (val s = state) {
        is OrderDetailUiState.Loading ->
            LoadingScreen("Cargando pedido #$orderId...")
        is OrderDetailUiState.Error   ->
            ErrorScreen(s.message, onRetry = { viewModel.load(orderId) })
        is OrderDetailUiState.Success ->
            AdminOrderDetailContent(
                order         = s.order,
                onBack        = onBack,
                onStatusChange = { newStatus ->
                    onStatusChange(orderId, newStatus)
                    viewModel.load(orderId)
                },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOrderDetailContent(
    order:         Order,
    onBack:        () -> Unit,
    onStatusChange:(OrderStatus) -> Unit,
) {
    val inputFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    val outputFmt = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale("es"))
    val dateStr   = runCatching { outputFmt.format(inputFmt.parse(order.createdAt)!!) }
                        .getOrDefault(order.createdAt.take(16))
    val updatedStr = runCatching { outputFmt.format(inputFmt.parse(order.updatedAt)!!) }
                        .getOrDefault(order.updatedAt.take(16))

    val taxAmount = order.total - order.total / 1.15
    val subtotal  = order.total - taxAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Pedido #${order.id}",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Text(
                            order.username,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    // Selector de estado en la TopAppBar
                    StatusDropdown(
                        current  = order.status,
                        onChange = onStatusChange,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            // ── Info general ──────────────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Información del pedido")
                    Spacer(Modifier.height(12.dp))
                    InfoGrid(listOf(
                        "Cliente"      to order.username,
                        "Fecha"        to dateStr,
                        "Actualizado"  to updatedStr,
                        "Total ítems"  to "${order.numItems}",
                    ))
                }
            }

            // ── Ítems ──────────────────────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Productos (${order.numItems})")
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        order.items.forEach { item ->
                            AdminOrderItemRow(item = item)
                        }
                    }
                }
            }

            // ── Totales ────────────────────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Resumen financiero")
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FinancialRow("Subtotal (sin IVA)", subtotal, false)
                        FinancialRow("IVA (15%)",          taxAmount, false)
                        HorizontalDivider(color = Border, thickness = 0.5.dp)
                        FinancialRow("Total",              order.total, true)
                    }
                }
            }

            // ── Cambio rápido de estado ────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Cambiar estado")
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OrderStatus.entries
                            .filter { it != order.status }
                            .forEach { status ->
                                Surface(
                                    onClick   = { onStatusChange(status) },
                                    shape     = MaterialTheme.shapes.small,
                                    color     = com.shopapp.presentation.components
                                        .orderStatusColor(status).copy(alpha = 0.1f),
                                    modifier  = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text     = status.label,
                                        color    = com.shopapp.presentation.components
                                            .orderStatusColor(status),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        style         = MaterialTheme.typography.labelSmall,
        color         = TextSecondary,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun InfoGrid(items: List<Pair<String, String>>) {
    items.chunked(2).forEach { row ->
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { (label, value) ->
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        text       = value,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            // Celda vacía si el row tiene 1 elemento
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun AdminOrderItemRow(item: OrderItem) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.weight(1f),
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(Surface2, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Text("📦", fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text       = item.productName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 2,
                )
                Text(
                    text  = "$${"%.2f".format(item.unitPrice)} × ${item.quantity} ud.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text       = "$${"%.2f".format(item.subtotal)}",
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = Accent,
        )
    }
}

@Composable
private fun FinancialRow(label: String, value: Double, isFinal: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text       = label,
            style      = if (isFinal) MaterialTheme.typography.titleSmall
                         else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.Bold else FontWeight.Normal,
            color      = if (isFinal) TextPrimary else TextSecondary,
        )
        Text(
            text       = "$${"%.2f".format(value)}",
            style      = if (isFinal) MaterialTheme.typography.titleSmall
                         else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isFinal) Accent else TextPrimary,
        )
    }
}
