// presentation/ui/admin/products/RestockDialog.kt
package com.shopapp.presentation.ui.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shopapp.domain.model.Product
import com.shopapp.presentation.components.ShopTextField
import com.shopapp.theme.*

@Composable
fun RestockDialog(
    product:   Product,
    onRestock: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var qty       by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var feedback  by remember { mutableStateOf<String?>(null) }

    val qtyVal     = qty.toIntOrNull()
    val qtyError   = qty.isNotEmpty() && (qtyVal == null || qtyVal <= 0)
    val canRestock = qtyVal != null && qtyVal > 0 && !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor   = Surface,
        shape            = MaterialTheme.shapes.large,
        title = {
            Column {
                Text(
                    "Restock: ${product.name}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Stock actual: ${product.stock} unidades",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stock == 0) Error else TextSecondary,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShopTextField(
                    value         = qty,
                    onValueChange = { qty = it; feedback = null },
                    label         = "Cantidad a añadir *",
                    placeholder   = "ej. 50",
                    isError       = qtyError,
                    errorMessage  = "Debe ser mayor que 0",
                    enabled       = !isLoading,
                    keyboardType  = KeyboardType.Number,
                )
                if (qtyVal != null && qtyVal > 0) {
                    Text(
                        "Nuevo stock: ${product.stock + qtyVal} unidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                feedback?.let {
                    Text(
                        it,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = if (it.startsWith("Error")) Error else Success,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { isLoading = true; onRestock(qtyVal!!) },
                enabled = canRestock,
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = Accent,
                    contentColor           = AccentOnDark,
                    disabledContainerColor = Accent.copy(alpha = 0.4f),
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = AccentOnDark,
                        modifier    = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(if (isLoading) "Guardando..." else "Añadir stock", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text("Cancelar", color = TextSecondary)
            }
        },
    )
}
