package com.shopapp.presentation.ui.uipublic.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.shopapp.domain.model.Product
import com.shopapp.domain.repository.ProductRepository
import com.shopapp.presentation.components.*
import com.shopapp.presentation.viewmodel.CartViewModel
import com.shopapp.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel de detalle ──────────────────────────────────────

sealed interface ProductDetailUiState {
    data object Loading                     : ProductDetailUiState
    data class  Success(val product: Product) : ProductDetailUiState
    data class  Error(val message: String)  : ProductDetailUiState
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    fun load(id: Int) {
        viewModelScope.launch {
            _state.value = ProductDetailUiState.Loading
            repository.getProduct(id)
                .onSuccess { _state.value = ProductDetailUiState.Success(it) }
                .onFailure { _state.value = ProductDetailUiState.Error(it.message ?: "Error") }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId:   Int,
    onBack:      () -> Unit,
    cartViewModel: CartViewModel,
    viewModel:   ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(productId) { viewModel.load(productId) }

    when (val s = state) {
        is ProductDetailUiState.Loading -> LoadingScreen("Cargando producto...")
        is ProductDetailUiState.Error   -> ErrorScreen(s.message, onRetry = { viewModel.load(productId) })
        is ProductDetailUiState.Success -> ProductDetailContent(
            product       = s.product,
            onBack        = onBack,
            cartViewModel = cartViewModel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailContent(
    product:       Product,
    onBack:        () -> Unit,
    cartViewModel: CartViewModel,
) {
    var quantity by remember { mutableIntStateOf(1) }
    var added    by remember { mutableStateOf(false) }

    val subtotal = product.price * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Imagen con TopBar flotante ─────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model              = product.imageUrl,
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier         = Modifier.fillMaxSize().background(Surface2),
                    contentAlignment = Alignment.Center,
                ) { Text("📦", fontSize = 72.sp) }
            }

            // Sin stock banner
            if (!product.inStock) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Error.copy(alpha = 0.9f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "PRODUCTO AGOTADO",
                        color      = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            // Botón volver
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Background.copy(alpha = 0.7f), RoundedCornerShape(50)),
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
        }

        // ── Info ──────────────────────────────────────────────
        Column(modifier = Modifier.padding(24.dp)) {

            // Categoría
            product.categoryName?.let {
                Text(
                    text       = it.uppercase(),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = Accent,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
            }

            // Nombre
            Text(
                text       = product.name,
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            // Precios
            Text(
                text       = "$${"%.2f".format(product.price)}",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = Accent,
            )
            Text(
                text  = "$${"%.2f".format(product.priceWithTax)} con IVA (15%)",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Spacer(Modifier.height(16.dp))

            // Stock
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (product.inStock) Success else Error,
                            RoundedCornerShape(50),
                        ),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = if (product.inStock) "${product.stock} unidades disponibles"
                            else "Producto agotado",
                    color = if (product.inStock) TextSecondary else Error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(16.dp))

            // Descripción
            if (product.description.isNotBlank()) {
                HorizontalDivider(color = Border, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text  = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(16.dp))
            }

            // Selector de cantidad
            if (product.inStock) {
                HorizontalDivider(color = Border, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text       = "Cantidad",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick  = { if (quantity > 1) quantity-- },
                            enabled  = quantity > 1,
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Menos",
                                tint = if (quantity > 1) TextPrimary else TextFaint,
                            )
                        }
                        Text(
                            text       = quantity.toString(),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                            modifier   = Modifier.padding(horizontal = 16.dp),
                        )
                        IconButton(
                            onClick = { if (quantity < product.stock) quantity++ },
                            enabled = quantity < product.stock,
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Más",
                                tint = if (quantity < product.stock) TextPrimary else TextFaint,
                            )
                        }
                    }
                }

                // Subtotal
                Spacer(Modifier.height(12.dp))
                Surface(
                    color  = Surface2,
                    shape  = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier              = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text("Subtotal", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text       = "$${"%.2f".format(subtotal)}",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Accent,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Botón añadir al carrito
                Button(
                    onClick = {
                        cartViewModel.addItem(product, quantity)
                        added = true
                    },
                    enabled  = !added,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = if (added) Success else Accent,
                        contentColor           = AccentOnDark,
                        disabledContainerColor = Success,
                        disabledContentColor   = AccentOnDark,
                    ),
                    shape    = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector        = if (added) Icons.Default.Check else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = if (added) "¡Añadido al carrito!" else
                            "Añadir${if (quantity > 1) " $quantity×" else ""} al carrito",
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.labelLarge,
                    )
                }

                // Resetear el estado "añadido"
                LaunchedEffect(added) {
                    if (added) {
                        kotlinx.coroutines.delay(2_000)
                        added = false
                    }
                }
            } else {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick  = {},
                    enabled  = false,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(
                        disabledContainerColor = Surface2,
                        disabledContentColor   = TextFaint,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Producto agotado", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
