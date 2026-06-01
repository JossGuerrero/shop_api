package com.shopapp.data.repository

import com.shopapp.data.local.TokenDataStore
import com.shopapp.domain.model.*
import com.shopapp.domain.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : AuthRepository {
    private var mockUser: LoggedUser? = null

    override suspend fun login(username: String, password: String): Result<LoggedUser> {
        delay(1000)
        val user = LoggedUser(1, username, "$username@example.com", isStaff = username.lowercase() == "admin")
        mockUser = user
        tokenDataStore.saveTokens("mock_access", "mock_refresh")
        tokenDataStore.saveUser(user.id, user.username, user.email, user.isStaff)
        return Result.success(user)
    }

    override suspend fun register(username: String, email: String, password: String, password2: String): Result<LoggedUser> {
        return login(username, password)
    }

    override suspend fun logout(): Result<Unit> {
        tokenDataStore.clearSession()
        mockUser = null
        return Result.success(Unit)
    }

    override suspend fun getStoredUser(): TokenDataStore.UserSnapshot? {
        return tokenDataStore.userSnapshot.firstOrNull() ?: mockUser?.let { 
            TokenDataStore.UserSnapshot(it.id, it.username, it.email, it.isStaff)
        }
    }

    override suspend fun isLoggedIn(): Boolean = !tokenDataStore.getAccessToken().isNullOrBlank()
}

@Singleton
class MockCategoryRepository @Inject constructor() : CategoryRepository {
    private val categories = listOf(
        Category(1, "Electrónica", "electronica", "Gadgets y más", true, 10, "2024-01-01"),
        Category(2, "Ropa", "ropa", "Moda actual", true, 25, "2024-01-01"),
        Category(3, "Hogar", "hogar", "Todo para tu casa", true, 15, "2024-01-01"),
        Category(4, "Deportes", "deportes", "Equipamiento pro", true, 8, "2024-01-01")
    )

    override suspend fun getCategories(): Result<List<Category>> = Result.success(categories)
    override suspend fun getCategory(id: Int): Result<Category> = Result.success(categories.first { it.id == id })
    override suspend fun createCategory(payload: CategoryPayload): Result<Category> = Result.success(categories[0])
    override suspend fun updateCategory(id: Int, payload: CategoryPayload): Result<Category> = Result.success(categories[0])
    override suspend fun deleteCategory(id: Int): Result<Unit> = Result.success(Unit)
    override suspend fun getStats(): Result<Map<String, Any>> = Result.success(emptyMap())
}

@Singleton
class MockProductRepository @Inject constructor() : ProductRepository {
    private val products = listOf(
        Product(1, "Smartphone Pro", "El mejor teléfono", 999.99, 1209.99, 10, true, true, "https://picsum.photos/400/400?random=1", 1, "Electrónica", "", ""),
        Product(2, "Camiseta Algodón", "100% orgánica", 19.99, 24.19, 50, true, true, "https://picsum.photos/400/400?random=2", 2, "Ropa", "", ""),
        Product(3, "Laptop Gaming", "Potencia sin límites", 1499.99, 1814.99, 5, true, true, "https://picsum.photos/400/400?random=3", 1, "Electrónica", "", ""),
        Product(4, "Silla Ergonómica", "Comodidad total", 199.99, 241.99, 15, true, true, "https://picsum.photos/400/400?random=4", 3, "Hogar", "", ""),
        Product(5, "Balón de Fútbol", "FIFA Quality", 29.99, 36.29, 0, false, true, "https://picsum.photos/400/400?random=5", 4, "Deportes", "", ""),
        Product(6, "Auriculares ANC", "Silencio absoluto", 299.99, 362.99, 20, true, true, "https://picsum.photos/400/400?random=6", 1, "Electrónica", "", "")
    )

    override suspend fun getProducts(filters: ProductFilters): Result<Pair<List<Product>, Int>> {
        delay(500)
        var filtered = products
        filters.search?.let { query ->
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) }
        }
        filters.category?.let { catId ->
            filtered = filtered.filter { it.categoryId == catId }
        }
        return Result.success(Pair(filtered, filtered.size))
    }

    override suspend fun getProduct(id: Int): Result<Product> = Result.success(products.first { it.id == id })
    override suspend fun createProduct(payload: ProductPayload): Result<Product> = Result.success(products[0])
    override suspend fun updateProduct(id: Int, payload: ProductPayload): Result<Product> = Result.success(products[0])
    override suspend fun deleteProduct(id: Int): Result<Unit> = Result.success(Unit)
    override suspend fun restock(id: Int, quantity: Int): Result<Int> = Result.success(100)
    override suspend fun getStats(): Result<Map<String, Any>> = Result.success(emptyMap())
}

@Singleton
class MockOrderRepository @Inject constructor() : OrderRepository {
    private val orders = mutableListOf<Order>()

    override suspend fun getOrders(page: Int?, status: String?): Result<Pair<List<Order>, Int>> {
        delay(500)
        return Result.success(Pair(orders.toList(), orders.size))
    }

    override suspend fun getOrder(id: Int): Result<Order> {
        val order = orders.find { it.id == id } ?: return Result.failure(Exception("Order not found"))
        return Result.success(order)
    }

    override suspend fun createOrder(): Result<Order> {
        val newOrder = Order(
            id = (orders.maxOfOrNull { it.id } ?: 0) + 1,
            username = "mock_user",
            status = OrderStatus.PENDING,
            total = 0.0,
            numItems = 0,
            items = emptyList(),
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )
        orders.add(newOrder)
        return Result.success(newOrder)
    }

    override suspend fun addItem(orderId: Int, productId: Int, quantity: Int): Result<Order> {
        val orderIndex = orders.indexOfFirst { it.id == orderId }
        if (orderIndex == -1) return Result.failure(Exception("Order not found"))
        
        val order = orders[orderIndex]
        val newItem = OrderItem(
            id = (order.items.maxOfOrNull { it.id } ?: 0) + 1,
            productId = productId,
            productName = "Product $productId",
            quantity = quantity,
            unitPrice = 100.0,
            subtotal = 100.0 * quantity
        )
        
        val updatedOrder = order.copy(
            items = order.items + newItem,
            numItems = order.numItems + quantity,
            total = order.total + newItem.subtotal
        )
        orders[orderIndex] = updatedOrder
        return Result.success(updatedOrder)
    }

    override suspend fun confirmOrder(orderId: Int): Result<Order> {
        val orderIndex = orders.indexOfFirst { it.id == orderId }
        if (orderIndex == -1) return Result.failure(Exception("Order not found"))
        
        val updatedOrder = orders[orderIndex].copy(status = OrderStatus.CONFIRMED)
        orders[orderIndex] = updatedOrder
        delay(1000)
        return Result.success(updatedOrder)
    }

    override suspend fun updateStatus(orderId: Int, status: OrderStatus): Result<Order> {
        val orderIndex = orders.indexOfFirst { it.id == orderId }
        if (orderIndex == -1) return Result.failure(Exception("Order not found"))
        
        val updatedOrder = orders[orderIndex].copy(status = status)
        orders[orderIndex] = updatedOrder
        return Result.success(updatedOrder)
    }

    override suspend fun getStats(): Result<Map<String, Any>> = Result.success(emptyMap())
}
