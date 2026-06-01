// presentation/ui/admin/products/ProductFormSheet.kt
package com.shopapp.presentation.ui.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.Product
import com.shopapp.domain.model.ProductPayload
import com.shopapp.presentation.components.ShopTextField
import com.shopapp.presentation.viewmodel.ProductFormState
import com.shopapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormSheet(
    initial:    Product?,
    categories: List<Category>,
    formState:  ProductFormState,
    onSave:     (ProductPayload) -> Unit,
    onDismiss:  () -> Unit,
) {
    val isEdit = initial != null

    var name        by remember { mutableStateOf(initial?.name        ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var price       by remember { mutableStateOf(if (initial != null) "%.2f".format(initial.price) else "") }
    var stock       by remember { mutableStateOf(initial?.stock?.toString() ?: "") }
    var isActive    by remember { mutableStateOf(initial?.isActive ?: true) }
    var selectedCat by remember { mutableStateOf(initial?.categoryId) }
    var catExpanded by remember { mutableStateOf(false) }

    val isSaving   = formState is ProductFormState.Saving
    val priceVal   = price.toDoubleOrNull()
    val stockVal   = stock.toIntOrNull()
    val nameError  = name.isNotEmpty() && name.length < 2
    val priceError = price.isNotEmpty() && (priceVal == null || priceVal <= 0)
    val stockError = stock.isNotEmpty() && (stockVal == null || stockVal < 0)
    val canSave    = name.length >= 2 && priceVal != null && priceVal > 0 &&
                     stockVal != null && stockVal >= 0 &&
                     selectedCat != null && !isSaving

    LaunchedEffect(formState) {
        if (formState is ProductFormState.Success) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor   = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text       = if (isEdit) "Editar: ${initial?.name}" else "Nuevo producto",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )

            if (formState is ProductFormState.Error) {
                Surface(
                    color    = Error.copy(alpha = 0.1f),
                    shape    = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        formState.message, color = Error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            ShopTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Nombre *",
                placeholder   = "ej. Laptop HP 15",
                isError       = nameError,
                errorMessage  = "Mínimo 2 caracteres",
                enabled       = !isSaving,
            )

            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Descripción") },
                minLines      = 3, maxLines = 4,
                enabled       = !isSaving,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Accent,
                    unfocusedBorderColor = Border,
                    focusedLabelColor    = Accent,
                    unfocusedLabelColor  = TextSecondary,
                ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShopTextField(
                    value         = price,
                    onValueChange = { price = it },
                    label         = "Precio $ *",
                    placeholder   = "0.00",
                    isError       = priceError,
                    errorMessage  = "Precio inválido",
                    enabled       = !isSaving,
                    keyboardType  = KeyboardType.Decimal,
                    modifier      = Modifier.weight(1f),
                )
                ShopTextField(
                    value         = stock,
                    onValueChange = { stock = it },
                    label         = "Stock *",
                    placeholder   = "0",
                    isError       = stockError,
                    errorMessage  = "Stock inválido",
                    enabled       = !isSaving,
                    keyboardType  = KeyboardType.Number,
                    modifier      = Modifier.weight(1f),
                )
            }

            ExposedDropdownMenuBox(
                expanded         = catExpanded,
                onExpandedChange = { catExpanded = !catExpanded },
            ) {
                OutlinedTextField(
                    value         = categories.find { it.id == selectedCat }?.name ?: "— Seleccionar —",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Categoría *") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = if (selectedCat == null) Error else Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded         = catExpanded,
                    onDismissRequest = { catExpanded = false },
                ) {
                    categories.filter { it.isActive }.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    cat.name,
                                    color      = if (selectedCat == cat.id) Accent else TextPrimary,
                                    fontWeight = if (selectedCat == cat.id) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = { selectedCat = cat.id; catExpanded = false },
                        )
                    }
                }
            }

            Surface(
                color    = Surface2,
                shape    = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Producto activo",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary,
                        )
                        Text(
                            "Visible en el catálogo público",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked         = isActive,
                        onCheckedChange = { isActive = it },
                        enabled         = !isSaving,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor    = AccentOnDark,
                            checkedTrackColor    = Accent,
                            uncheckedTrackColor  = Surface,
                            uncheckedBorderColor = Border,
                        ),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = { if (!isSaving) onDismiss() },
                    enabled  = !isSaving,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Border),
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        onSave(ProductPayload(
                            name        = name.trim(),
                            description = description.trim(),
                            price       = priceVal!!,
                            stock       = stockVal!!,
                            isActive    = isActive,
                            categoryId  = selectedCat!!,
                        ))
                    },
                    enabled  = canSave,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Accent,
                        contentColor           = AccentOnDark,
                        disabledContainerColor = Accent.copy(alpha = 0.4f),
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color       = AccentOnDark,
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (isSaving) "Guardando..."
                        else if (isEdit) "Guardar cambios"
                        else "Crear producto",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
