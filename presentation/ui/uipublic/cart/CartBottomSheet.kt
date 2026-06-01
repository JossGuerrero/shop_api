package com.shopapp.presentation.ui.uipublic.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shopapp.presentation.viewmodel.CartItem
import com.shopapp.presentation.viewmodel.CartViewModel
import com.shopapp.presentation.viewmodel.CheckoutState
import com.shopapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cartViewModel:   CartViewModel,
    isAuthenticated: Boolean,
    onDismiss:       () -> Unit,
    onLoginRequired: () -> Unit,
    onOrderSuccess:  (Int) -> Unit,
) {
    val items         by cartViewModel.items.collectAsState()
    val subtotal      by cartViewModel.subtotal.collectAsState()
    val totalWithTax  by cartViewModel.totalWithTax.collectAsState()
    val checkoutState by cartViewModel.checkoutState.collectAsState()

    // Navegar al éxito cuando el checkout termina
    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            onOrderSuccess((checkoutState as CheckoutState.Success).orderId)
            cartViewModel.resetCheckout()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Surface,
        dragHandle       = {
            Box(
                modifier         = Modifier
                    .padding(vertical = 12.dp)
                    .size(40.dp, 4.dp)
                    .background(Border, RoundedCornerShape(2.dp)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            // ── Header ────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text       = "Mi carrito",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    if (items.isNotEmpty()) {
                        Text(
                            text  = "${items.sumOf { it.quantity }} producto${if (items.sumOf { it.quantity } != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                if (items.isNotEmpty()) {
                    IconButton(onClick = cartViewModel::clearCart) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Vaciar", tint = Error)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Border, thickness = 0.5.dp)

            // ── Estado vacío ──────────────────────────────────
            if (items.isEmpty() && checkoutState !is CheckoutState.Success) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🛒", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tu carrito está vacío",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Text(
                        "Añade productos desde el catálogo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            // ── Lista de ítems ────────────────────────────────
            if (items.isNotEmpty()) {
                LazyColumn(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    contentPadding    = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items, key = { it.product.id }) { item ->
                        CartItemRow(
                            item          = item,
                            onIncrease    = { cartViewModel.updateQuantity(item.product.id, item.quantity + 1) },
                            onDecrease    = { cartViewModel.updateQuantity(item.product.id, item.quantity - 1) },
                            onRemove      = { cartViewModel.removeItem(item.product.id) },
                        )
                    }
                }

                HorizontalDivider(color = Border, thickness = 0.5.dp)

                // Totales
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    TotalRow("Subtotal", "$${"%.2f".format(subtotal)}", false)
                    Spacer(Modifier.height(4.dp))
                    TotalRow("IVA (15%)", "$${"%.2f".format(totalWithTax - subtotal)}", false)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    Spacer(Modifier.height(8.dp))
                    TotalRow("Total", "$${"%.2f".format(totalWithTax)}", true)
                }

                // Error de checkout
                if (checkoutState is CheckoutState.Error) {
                    Surface(
                        color    = Error.copy(alpha = 0.1f),
                        shape    = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text     = (checkoutState as CheckoutState.Error).message,
                            color    = Error,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Auth notice
                if (!isAuthenticated) {
                    Surface(
                        color    = Accent.copy(alpha = 0.08f),
                        shape    = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text     = "💡 Inicia sesión para confirmar el pedido",
                            color    = Accent,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Botón checkout
                val isLoading = checkoutState is CheckoutState.Loading
                Button(
                    onClick = {
                        if (!isAuthenticated) onLoginRequired()
                        else cartViewModel.checkout()
                    },
                    enabled  = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 24.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Accent,
                        contentColor           = AccentOnDark,
                        disabledContainerColor = Accent.copy(alpha = 0.5f),
                    ),
                    shape    = MaterialTheme.shapes.medium,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = AccentOnDark,
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Procesando...", fontWeight = FontWeight.Bold)
                    } else if (!isAuthenticated) {
                        Text("Iniciar sesión para comprar", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            "Confirmar — $${"%.2f".format(totalWithTax)}",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item:       CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove:   () -> Unit,
) {
    Surface(
        color  = Surface2,
        shape  = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Imagen
            Box(
                modifier         = Modifier
                    .size(58.dp)
                    .background(Surface, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (item.product.imageUrl != null) {
                    AsyncImage(
                        model              = item.product.imageUrl,
                        contentDescription = item.product.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Text("📦", fontSize = 24.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = item.product.name,
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color    = TextPrimary,
                    maxLines = 2,
                )
                Text(
                    text  = "$${"%.2f".format(item.product.price)} / ud.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.width(8.dp))

            // Controles de cantidad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Remove, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text       = item.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(horizontal = 8.dp),
                    )
                    IconButton(
                        onClick  = onIncrease,
                        enabled  = item.quantity < item.product.stock,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.Add, null,
                            tint     = if (item.quantity < item.product.stock) TextSecondary else TextFaint,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Text(
                    text       = "$${"%.2f".format(item.product.price * item.quantity)}",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = Accent,
                )
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = TextFaint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, isFinal: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            style      = if (isFinal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.Bold else FontWeight.Normal,
            color      = if (isFinal) TextPrimary else TextSecondary,
        )
        Text(
            text       = value,
            style      = if (isFinal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isFinal) Accent else TextPrimary,
        )
    }
}
