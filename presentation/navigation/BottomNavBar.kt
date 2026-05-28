package com.shopapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shopapp.theme.*

data class BottomNavItem(
    val screen:      Screen,
    val label:       String,
    val icon:        ImageVector,
    val iconSelected:ImageVector,
    val badgeCount:  Int = 0,
)

@Composable
fun BottomNavBar(
    navController: NavController,
    cartCount:     Int,
    onCartClick:   () -> Unit,
) {
    val items = listOf(
        BottomNavItem(Screen.Home,   "Inicio",   Icons.Outlined.Home,          Icons.Filled.Home),
        BottomNavItem(Screen.Catalog,"Catálogo", Icons.Outlined.GridView,       Icons.Filled.GridView),
        BottomNavItem(Screen.Cart,   "Carrito",  Icons.Outlined.ShoppingCart,   Icons.Filled.ShoppingCart, cartCount),
        BottomNavItem(Screen.Orders, "Pedidos",  Icons.Outlined.ReceiptLong,    Icons.Filled.ReceiptLong),
        BottomNavItem(Screen.Profile,"Perfil",   Icons.Outlined.AccountCircle,  Icons.Filled.AccountCircle),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    if (item.screen == Screen.Cart) {
                        onCartClick()
                    } else {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = Error) {
                                Text(
                                    text  = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }) {
                            Icon(
                                imageVector = if (isSelected) item.iconSelected else item.icon,
                                contentDescription = item.label,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.iconSelected else item.icon,
                            contentDescription = item.label,
                        )
                    }
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor       = Accent,
                    selectedTextColor       = Accent,
                    indicatorColor          = Accent.copy(alpha = 0.12f),
                    unselectedIconColor     = TextSecondary,
                    unselectedTextColor     = TextSecondary,
                ),
            )
        }
    }
}
