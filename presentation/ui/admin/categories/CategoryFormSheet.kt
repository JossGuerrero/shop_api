// presentation/ui/admin/categories/CategoryFormSheet.kt
package com.shopapp.presentation.ui.admin.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shopapp.domain.model.Category
import com.shopapp.domain.model.CategoryPayload
import com.shopapp.presentation.components.ShopButton
import com.shopapp.presentation.components.ShopTextField
import com.shopapp.presentation.viewmodel.CategoryFormState
import com.shopapp.theme.*

fun String.toSlug(): String = this
    .lowercase()
    .replace(Regex("[áàäâã]"), "a")
    .replace(Regex("[éèëê]"), "e")
    .replace(Regex("[íìïî]"), "i")
    .replace(Regex("[óòöôõ]"), "o")
    .replace(Regex("[úùüû]"), "u")
    .replace(Regex("[ñ]"), "n")
    .replace(Regex("[^a-z0-9\\s-]"), "")
    .trim()
    .replace(Regex("\\s+"), "-")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormSheet(
    initial:    Category?,
    formState:  CategoryFormState,
    onSave:     (CategoryPayload) -> Unit,
    onDismiss:  () -> Unit,
) {
    val isEdit = initial != null

    var name        by remember { mutableStateOf(initial?.name        ?: "") }
    var slug        by remember { mutableStateOf(initial?.slug        ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var isActive    by remember { mutableStateOf(initial?.isActive    ?: true) }
    var slugEdited  by remember { mutableStateOf(isEdit) }

    // Auto-generar slug desde el nombre si no se ha editado manualmente
    LaunchedEffect(name) {
        if (!slugEdited) slug = name.toSlug()
    }

    // Cerrar al éxito
    LaunchedEffect(formState) {
        if (formState is CategoryFormState.Success) onDismiss()
    }

    val isSaving = formState is CategoryFormState.Saving
    val nameError = name.isNotEmpty() && name.length < 2
    val slugError = slug.isNotEmpty() && !slug.matches(Regex("[a-z0-9-]+"))
    val canSave   = name.length >= 2 && slug.isNotEmpty() && !nameError && !slugError && !isSaving

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor   = Surface,
        dragHandle       = {
            Box(
                modifier         = Modifier
                    .padding(vertical = 12.dp)
                    .size(40.dp, 4.dp)
                    .then(Modifier)
                    .padding(0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp, 4.dp),
                    color    = Border,
                    shape    = MaterialTheme.shapes.extraSmall,
                ) {}
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Título
            Text(
                text       = if (isEdit) "Editar: ${initial?.name}" else "Nueva categoría",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )

            // Error del formulario
            if (formState is CategoryFormState.Error) {
                Surface(
                    color  = Error.copy(alpha = 0.1f),
                    shape  = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text     = formState.message,
                        color    = Error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            // Nombre
            ShopTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Nombre *",
                placeholder   = "ej. Electrónica",
                isError       = nameError,
                errorMessage  = "Mínimo 2 caracteres",
                enabled       = !isSaving,
            )

            // Slug
            ShopTextField(
                value         = slug,
                onValueChange = { slug = it; slugEdited = true },
                label         = "Slug (URL) *",
                placeholder   = "ej. electronica",
                isError       = slugError,
                errorMessage  = "Solo minúsculas, números y guiones",
                enabled       = !isSaving,
            )
            Text(
                text  = "URL: /catalog?category=$slug",
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
            )

            // Descripción
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Descripción") },
                placeholder   = { Text("Descripción opcional", color = TextFaint) },
                minLines      = 3,
                maxLines      = 5,
                enabled       = !isSaving,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Accent,
                    focusedLabelColor    = Accent,
                    unfocusedBorderColor = Border,
                    unfocusedLabelColor  = TextSecondary,
                ),
            )

            // Toggle activa
            Surface(
                color  = Surface2,
                shape  = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Categoría activa",
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
                            checkedThumbColor       = AccentOnDark,
                            checkedTrackColor       = Accent,
                            uncheckedThumbColor     = TextSecondary,
                            uncheckedTrackColor     = Surface,
                            uncheckedBorderColor    = Border,
                        ),
                    )
                }
            }

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = { if (!isSaving) onDismiss() },
                    enabled  = !isSaving,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Border),
                    ),
                    shape    = MaterialTheme.shapes.medium,
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick  = {
                        onSave(CategoryPayload(name.trim(), slug.trim(), description.trim(), isActive))
                    },
                    enabled  = canSave,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Accent,
                        contentColor           = AccentOnDark,
                        disabledContainerColor = Accent.copy(alpha = 0.4f),
                    ),
                    shape    = MaterialTheme.shapes.medium,
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
                        else "Crear categoría",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
