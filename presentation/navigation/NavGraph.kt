package com.shopapp.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.shopapp.presentation.components.LoadingScreen
import com.shopapp.presentation.ui.auth.LoginScreen
import com.shopapp.presentation.ui.auth.RegisterScreen
import com.shopapp.presentation.ui.uipublic.catalog.CatalogScreen
import com.shopapp.presentation.ui.uipublic.home.HomeScreen
import com.shopapp.presentation.viewmodel.AuthViewModel
import com.shopapp.presentation.viewmodel.CartViewModel
import com.shopapp.theme.Surface

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel = hiltViewModel(),
) {
    val isCheckingSession by authViewModel.isCheckingSession.collectAsState()

    if (isCheckingSession) {
        LoadingScreen("Iniciando ShopApp...")
        return
    }

    // Extraemos el contenido en un composable separado para que remember
    // y LaunchedEffect no queden condicionados por el early-return anterior.
    NavGraphContent(
        authViewModel = authViewModel,
        cartViewModel = cartViewModel,
    )
}

@Composable
private fun NavGraphContent(
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
) {
    val navController   = rememberNavController()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isStaff         by authViewModel.isStaff.collectAsState()
    val cartCount       by cartViewModel.totalItems.collectAsState()

    // startDestination se fija UNA SOLA VEZ según el estado inicial de la sesión.
    // Si cambiara dinámicamente, NavHost recrearía el grafo y causaría el parpadeo.
    val startDestination = remember {
        when {
            !isAuthenticated -> Screen.Login.route
            isStaff          -> Screen.AdminDashboard.route
            else             -> Screen.Home.route
        }
    }

    // Cambios de auth POSTERIORES a la composición inicial se manejan aquí,
    // dentro de un efecto, nunca en el cuerpo de un composable.
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Catalog.route,
        Screen.Orders.route,
        Screen.Profile.route,
    )

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    cartCount     = cartCount,
                    onCartClick   = { navController.navigate(Screen.Cart.route) },
                )
            }
        },
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(innerPadding),
        ) {

            // ── LOGIN ───────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { staff ->
                        val dest = if (staff) Screen.AdminDashboard.route else Screen.Home.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    viewModel            = authViewModel,
                )
            }

            // ── REGISTER ────────────────────────────
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { staff ->
                        val dest = if (staff) Screen.AdminDashboard.route else Screen.Home.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                    viewModel         = authViewModel,
                )
            }

            // ── HOME ───────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onProductClick = { id -> navController.navigate("product/$id") },
                    onCatalogClick = { navController.navigate(Screen.Catalog.route) },
                )
            }

            // ── CATALOGO ───────────────────────────
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    onProductClick = { id -> navController.navigate("product/$id") },
                )
            }

            // ── DETALLE PRODUCTO ───────────────────
            composable(
                route     = "product/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) {
                LoadingScreen("Detalle de producto — M5")
            }

            // ── CARRITO ────────────────────────────
            composable(Screen.Cart.route) {
                ScreenWithLogout(
                    title    = "Carrito — M5",
                    onLogout = { authViewModel.logout() },
                )
            }

            // ── PEDIDOS ────────────────────────────
            composable(Screen.Orders.route) {
                ScreenWithLogout(
                    title    = "Mis pedidos — M6",
                    onLogout = { authViewModel.logout() },
                )
            }

            // ── PERFIL ─────────────────────────────
            composable(Screen.Profile.route) {
                ScreenWithLogout(
                    title    = "Mi perfil — M6",
                    onLogout = { authViewModel.logout() },
                ) {
                    LoadingScreen("Mi perfil — M6")
                }
            }

            // ── ADMIN ──────────────────────────────
            composable(Screen.AdminDashboard.route) {
                ScreenWithLogout(
                    title    = "Admin Dashboard — M8",
                    onLogout = { authViewModel.logout() },
                )
            }
        }
    }
}

@Composable
fun ScreenWithLogout(
    title: String,
    onLogout: () -> Unit,
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        content()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}
