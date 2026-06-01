package com.shopapp.presentation.ui.client.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.domain.model.Order
import com.shopapp.domain.model.OrderItem
import com.shopapp.domain.model.OrderStatus
import com.shopapp.presentation.components.ErrorScreen
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.components.StatusBadge
import com.shopapp.presentation.components.orderStatusColor
import com.shopapp.presentation.viewmodel.OrderDetailUiState
import com.shopapp.presentation.viewmodel.OrderDetailViewModel
import com.shopapp.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

private val PROGRESS_STEPS = listOf(
    OrderStatus.PENDING,
    OrderStatus.CONFIRMED,
    OrderStatus.SHIPPED,
    OrderStatus.DELIVERED,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId:   Int,
    onBack:    () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    when (val s = state) {
        is OrderDetailUiState.Loading ->
            LoadingScreen("Cargando pedido...")
        is OrderDetailUiState.Error   ->
            ErrorScreen(s.message, onRetry = { viewModel.load(orderId) })
        is OrderDetailUiState.Success ->
            OrderDetailContent(order = s.order, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailContent(order: Order, onBack: () -> Unit) {
    val inputFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    val outputFmt = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale("es"))
    val dateStr   = runCatching { outputFmt.format(inputFmt.parse(order.createdAt)!!) }
                        .getOrDefault(order.createdAt.take(16))

    val isCancelled = order.status == OrderStatus.CANCELLED
    val currentStep = PROGRESS_STEPS.indexOf(order.status).coerceAtLeast(0)
    val taxAmount   = order.total - order.total / 1.15
    val subtotal    = order.total - taxAmount

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
                        Text(dateStr, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                actions = { StatusBadge(order.status, modifier = Modifier.padding(end = 16.dp)) },
                colors  = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Barra de progreso ──────────────────────────────
            if (!isCancelled) {
                OrderProgressBar(
                    steps       = PROGRESS_STEPS,
                    currentStep = currentStep,
                )
            } else {
                Surface(
                    color    = Error.copy(alpha = 0.08f),
                    shape    = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text     = "⚠️ Este pedido fue cancelado",
                        color    = Error,
                        fontWeight = FontWeight.SemiBold,
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            // ── Ítems ──────────────────────────────────────────
            SectionCard(title = "Productos (${order.numItems})") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    order.items.forEach { item ->
                        OrderItemRow(item = item)
                    }
                }
            }

            // ── Resumen de totales ─────────────────────────────
            SectionCard(title = "Resumen") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TotalLine("Subtotal (sin IVA)", subtotal,  false)
                    TotalLine("IVA (15%)",           taxAmount, false)
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    TotalLine("Total",               order.total, true)
                }
            }

            // Meta info
            Text(
                text  = "Actualizado: $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

// ── Barra de progreso ─────────────────────────────────────────

@Composable
private fun OrderProgressBar(steps: List<OrderStatus>, currentStep: Int) {
    Surface(
        color    = Surface,
        shape    = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text       = "Estado del pedido",
                style      = MaterialTheme.typography.labelSmall,
                color      = TextSecondary,
                letterSpacing = 0.8.sp,
                modifier   = Modifier.padding(bottom = 20.dp),
            )
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { index, step ->
                    val isDone    = index <= currentStep
                    val isCurrent = index == currentStep
                    val color     = if (isDone) Accent else Border

                    // Nodo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(if (isCurrent) 36.dp else 30.dp)
                                .background(
                                    if (isDone) Accent else Surface2,
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = if (isDone) "✓" else "${index + 1}",
                                color      = if (isDone) AccentOnDark else TextFaint,
                                fontSize   = if (isCurrent) 14.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text       = step.label,
                            style      = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color      = if (isDone) Accent else TextFaint,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        )
                    }

                    // Línea conectora
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .padding(bottom = 20.dp)
                                .background(if (index < currentStep) Accent else Border),
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-componentes ───────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelSmall,
                color      = TextSecondary,
                letterSpacing = 0.8.sp,
                modifier   = Modifier.padding(bottom = 14.dp),
            )
            content()
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
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
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = Accent,
        )
    }
}

@Composable
private fun TotalLine(label: String, value: Double, isFinal: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text       = label,
            style      = if (isFinal) MaterialTheme.typography.titleMedium
                         else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.Bold else FontWeight.Normal,
            color      = if (isFinal) TextPrimary else TextSecondary,
        )
        Text(
            text       = "$${"%.2f".format(value)}",
            style      = if (isFinal) MaterialTheme.typography.titleMedium
                         else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isFinal) Accent else TextPrimary,
        )
    }
}
