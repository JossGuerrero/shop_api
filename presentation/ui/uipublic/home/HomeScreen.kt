package com.shopapp.presentation.ui.uipublic.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.shopapp.domain.model.Product
import com.shopapp.presentation.viewmodel.CatalogViewModel
import com.shopapp.theme.*

@Composable
fun HomeScreen(
    onProductClick:  (Int) -> Unit,
    onCatalogClick:  () -> Unit,
    viewModel:       CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding      = PaddingValues(bottom = 24.dp),
    ) {
        // ── Hero ──────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Surface2, Background),
                        ),
                    )
                    .padding(horizontal = 24.dp, vertical = 48.dp),
            ) {
                Column {
                    Text(
                        text       = "Descubre lo",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color      = TextSecondary,
                    )
                    Text(
                        text       = "extraordinario",
                        fontSize   = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Accent,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text  = "Los mejores productos seleccionados para ti.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onCatalogClick,
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor   = AccentOnDark,
                        ),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("Ver catálogo", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ── Categorías ────────────────────────────────────────
        if (state.categories.isNotEmpty()) {
            item {
                SectionHeader(title = "Categorías", onSeeAll = onCatalogClick)
            }
            item {
                LazyRow(
                    contentPadding      = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.categories.take(6)) { cat ->
                        CategoryChip(
                            name     = cat.name,
                            count    = cat.totalProducts,
                            onClick  = { onCatalogClick() },
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Novedades ─────────────────────────────────────────
        item {
            SectionHeader(title = "Novedades", onSeeAll = onCatalogClick)
        }

        if (state.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }
        } else {
            // Grid 2 columnas con LazyVerticalGrid simulado con filas de LazyColumn
            val chunked = state.products.take(4).chunked(2)
            items(chunked) { row ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { product ->
                        ProductCard(
                            product  = product,
                            onClick  = { onProductClick(product.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Celda vacía si el row tiene solo 1 elemento
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ── Composables locales ───────────────────────────────────────

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
        )
        TextButton(onClick = onSeeAll) {
            Text("Ver todos", color = Accent, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CategoryChip(name: String, count: Int, onClick: () -> Unit) {
    Surface(
        onClick        = onClick,
        shape          = MaterialTheme.shapes.medium,
        color          = Surface2,
        tonalElevation = 0.dp,
        modifier       = Modifier.width(120.dp),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🏷️", fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text       = name,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
            )
            Text(
                text  = "$count productos",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextSecondary,
            )
        }
    }
}

@Composable
fun ProductCard(
    product:  Product,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick        = onClick,
        shape          = MaterialTheme.shapes.large,
        color          = Surface,
        tonalElevation = 0.dp,
        modifier       = modifier,
    ) {
        Column {
            // Imagen
            Box(
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Surface2),
                contentAlignment  = Alignment.Center,
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model             = product.imageUrl,
                        contentDescription = product.name,
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize(),
                    )
                } else {
                    Text("📦", fontSize = 36.sp)
                }
                if (!product.inStock) {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Error.copy(alpha = 0.85f))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = "Sin stock",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text     = product.categoryName ?: "Sin categoría",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Accent,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 2,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text       = "$${"%.2f".format(product.price)}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Accent,
                )
                Text(
                    text  = "$${"%.2f".format(product.priceWithTax)} con IVA",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
