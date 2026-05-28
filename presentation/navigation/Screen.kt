package com.shopapp.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login    : Screen("login")
    data object Register : Screen("register")

    // Public
    data object Home     : Screen("home")
    data object Catalog  : Screen("catalog")
    data class  Product(val id: Int = 0) : Screen("product/{id}") {
        fun createRoute(id: Int) = "product/$id"
    }
    data object Cart     : Screen("cart")

    // Client
    data object Orders       : Screen("orders")
    data class  OrderDetail(val id: Int = 0) : Screen("orders/{id}") {
        fun createRoute(id: Int) = "orders/$id"
    }
    data object Profile : Screen("profile")

    // Admin
    data object AdminDashboard  : Screen("admin")
    data object AdminCategories : Screen("admin/categories")
    data object AdminProducts   : Screen("admin/products")
    data object AdminOrders     : Screen("admin/orders")
    data object AdminUsers      : Screen("admin/users")
}
