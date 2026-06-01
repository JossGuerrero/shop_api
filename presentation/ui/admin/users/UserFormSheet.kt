package com.shopapp.presentation.ui.admin.users

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
import com.shopapp.domain.model.User
import com.shopapp.domain.model.UserPayload
import com.shopapp.presentation.components.ShopTextField
import com.shopapp.presentation.viewmodel.UserFormState
import com.shopapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormSheet(
    initial:   User?,
    formState: UserFormState,
    onSave:    (UserPayload) -> Unit,
    onDismiss: () -> Unit,
) {
    val isEdit = initial != null

    var username  by remember { mutableStateOf(initial?.username  ?: "") }
    var email     by remember { mutableStateOf(initial?.email     ?: "") }
    var firstName by remember { mutableStateOf(initial?.firstName ?: "") }
    var lastName  by remember { mutableStateOf(initial?.lastName  ?: "") }
    var password  by remember { mutableStateOf("") }
    var isStaff   by remember { mutableStateOf(initial?.isStaff  ?: false) }
    var isActive  by remember { mutableStateOf(initial?.isActive  ?: true) }

    val isSaving     = formState is UserFormState.Saving
    val usernameError= username.isNotEmpty() && username.length < 3
    val emailError   = email.isNotEmpty() && !email.contains("@")
    val passwordError= !isEdit && password.isNotEmpty() && password.length < 8
    val canSave      = username.length >= 3 && email.contains("@") &&
                       (isEdit || password.length >= 8) && !isSaving

    LaunchedEffect(formState) {
        if (formState is UserFormState.Success) onDismiss()
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
                text       = if (isEdit) "Editar: ${initial?.username}" else "Nuevo usuario",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )

            // Error global
            if (formState is UserFormState.Error) {
                Surface(
                    color    = Error.copy(alpha = 0.1f),
                    shape    = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        formState.message,
                        color    = Error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            // Usuario y Email en fila
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShopTextField(
                    value         = username,
                    onValueChange = { username = it },
                    label         = "Usuario *",
                    placeholder   = "mínimo 3 caracteres",
                    isError       = usernameError,
                    errorMessage  = "Mínimo 3 caracteres",
                    enabled       = !isSaving,
                    modifier      = Modifier.weight(1f),
                )
                ShopTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = "Email *",
                    placeholder   = "tu@email.com",
                    isError       = emailError,
                    errorMessage  = "Email inválido",
                    enabled       = !isSaving,
                    keyboardType  = KeyboardType.Email,
                    modifier      = Modifier.weight(1f),
                )
            }

            // Nombre y Apellido en fila
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShopTextField(
                    value         = firstName,
                    onValueChange = { firstName = it },
                    label         = "Nombre",
                    placeholder   = "Juan",
                    enabled       = !isSaving,
                    modifier      = Modifier.weight(1f),
                )
                ShopTextField(
                    value         = lastName,
                    onValueChange = { lastName = it },
                    label         = "Apellido",
                    placeholder   = "Pérez",
                    enabled       = !isSaving,
                    modifier      = Modifier.weight(1f),
                )
            }

            // Contraseña
            ShopTextField(
                value         = password,
                onValueChange = { password = it },
                label         = if (isEdit) "Nueva contraseña (vacío = no cambiar)" else "Contraseña *",
                placeholder   = "mínimo 8 caracteres",
                isPassword    = true,
                isError       = passwordError,
                errorMessage  = "Mínimo 8 caracteres",
                enabled       = !isSaving,
                keyboardType  = KeyboardType.Password,
            )

            // Toggles Staff y Activo
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleCard(
                    label       = "Rol Staff",
                    description = "Acceso al admin",
                    checked     = isStaff,
                    onChanged   = { isStaff = it },
                    enabled     = !isSaving,
                    modifier    = Modifier.weight(1f),
                )
                ToggleCard(
                    label       = "Activo",
                    description = "Puede iniciar sesión",
                    checked     = isActive,
                    onChanged   = { isActive = it },
                    enabled     = !isSaving,
                    modifier    = Modifier.weight(1f),
                )
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
                ) { Text("Cancelar") }

                Button(
                    onClick  = {
                        onSave(UserPayload(
                            username  = username.trim(),
                            email     = email.trim(),
                            firstName = firstName.trim(),
                            lastName  = lastName.trim(),
                            isStaff   = isStaff,
                            isActive  = isActive,
                            password  = password.ifBlank { null },
                        ))
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
                        else "Crear usuario",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleCard(
    label:       String,
    description: String,
    checked:     Boolean,
    onChanged:   (Boolean) -> Unit,
    enabled:     Boolean,
    modifier:    Modifier = Modifier,
) {
    Surface(
        color    = Surface2,
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = androidx.compose.ui.unit.TextUnit(
                            10f,
                            androidx.compose.ui.unit.TextUnitType.Sp,
                        )
                    ),
                    color = TextSecondary,
                )
            }
            Switch(
                checked         = checked,
                onCheckedChange = onChanged,
                enabled         = enabled,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor    = AccentOnDark,
                    checkedTrackColor    = Accent,
                    uncheckedTrackColor  = Surface,
                    uncheckedBorderColor = Border,
                ),
            )
        }
    }
}
