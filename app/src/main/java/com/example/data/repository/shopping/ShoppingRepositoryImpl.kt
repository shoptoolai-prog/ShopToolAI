package com.example.data.repository.shopping

import android.content.Context
import android.util.Log
import com.example.data.integration.NetworkClient
import com.example.data.local.ShoppingProductDao
import com.example.data.local.ShoppingProductEntity
import com.example.data.util.NetworkUtils
import com.example.domain.model.ShoppingProduct
import com.example.domain.repository.shopping.ShoppingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ShoppingRepositoryImpl(
    private val context: Context,
    private val productDao: ShoppingProductDao,
    private val networkClient: NetworkClient = NetworkClient(context)
) : ShoppingRepository {

    private val tag = "ShoppingRepositoryImpl"
    private val memoryCache = ConcurrentHashMap<String, ShoppingProduct>()

    private fun ShoppingProduct.toEntity() = ShoppingProductEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        color = color,
        images = images,
        price = price,
        discount = discount,
        currency = currency,
        storeName = storeName,
        productUrl = productUrl,
        rating = rating,
        reviewCount = reviewCount,
        aiConfidence = aiConfidence,
        aiRecommendationScore = aiRecommendationScore,
        deliveryTime = deliveryTime,
        stockStatus = stockStatus
    )

    private fun ShoppingProductEntity.toModel() = ShoppingProduct(
        id = id,
        name = name,
        brand = brand,
        category = category,
        color = color,
        images = images,
        price = price,
        discount = discount,
        currency = currency,
        storeName = storeName,
        productUrl = productUrl,
        rating = rating,
        reviewCount = reviewCount,
        aiConfidence = aiConfidence,
        aiRecommendationScore = aiRecommendationScore,
        deliveryTime = deliveryTime,
        stockStatus = stockStatus
    )

    override suspend fun getProductById(id: String): ShoppingProduct? = withContext(Dispatchers.IO) {
        // Memory cache check
        memoryCache[id]?.let {
            Log.d(tag, "Memory cache hit for product ID: $id")
            return@withContext it
        }

        // Local DB check
        try {
            val entity = productDao.getProductById(id)
            if (entity != null) {
                val model = entity.toModel()
                memoryCache[id] = model
                Log.d(tag, "Local DB hit for product ID: $id")
                return@withContext model
            }
        } catch (e: Exception) {
            Log.e(tag, "Error reading from local DB for ID $id: ${e.message}")
        }

        // Remote fetch (simulate with NetworkClient)
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                networkClient.executeSecureCall(
                    serviceName = "ShoppingService",
                    apiPath = "products/$id"
                ) {
                    // Simulate API parse response
                    val fetched = ShoppingProduct(
                        id = id,
                        name = "Enriched Premium Mechanical Keyboard",
                        brand = "NuPhy",
                        category = "Tech & Electronics",
                        color = "Silver",
                        images = listOf("https://images.unsplash.com/photo-1587829741301-dc798b83add3"),
                        price = 159.99,
                        discount = 10.00,
                        currency = "USD",
                        storeName = "Official NuPhy Store",
                        productUrl = "https://nuphy.com",
                        rating = 4.8f,
                        reviewCount = 128,
                        aiConfidence = 0.96f,
                        aiRecommendationScore = 0.94f,
                        deliveryTime = "2-3 business days",
                        stockStatus = "In Stock"
                    )
                    saveProduct(fetched)
                    fetched
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch remote product for ID $id: ${e.message}")
                null
            }
        } else {
            Log.w(tag, "Network unavailable, cannot fetch product $id from remote source")
            null
        }
    }

    override suspend fun saveProduct(product: ShoppingProduct) {
        withContext(Dispatchers.IO) {
            memoryCache[product.id] = product
            try {
                productDao.insertProduct(product.toEntity())
                Log.d(tag, "Successfully saved product ${product.id} to Memory Cache and Local DB")
            } catch (e: Exception) {
                Log.e(tag, "Failed to insert product ${product.id} to local DB: ${e.message}")
            }
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(Dispatchers.IO) {
            memoryCache.remove(id)
            try {
                productDao.deleteProductById(id)
                Log.d(tag, "Successfully deleted product $id from Memory Cache and Local DB")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete product $id from local DB: ${e.message}")
            }
        }
    }

    override suspend fun getProducts(limit: Int, offset: Int): List<ShoppingProduct> = withContext(Dispatchers.IO) {
        try {
            val entities = productDao.getProducts(limit, offset)
            if (entities.isNotEmpty()) {
                Log.d(tag, "Loaded ${entities.size} products from local DB (limit=$limit, offset=$offset)")
                return@withContext entities.map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to read products list from DB: ${e.message}")
        }

        // If DB is empty, preload mock products and save them
        if (offset == 0) {
            val mockList = createMockProducts()
            mockList.forEach { saveProduct(it) }
            return@withContext mockList.take(limit)
        }

        emptyList()
    }

    override suspend fun searchProducts(query: String, limit: Int, offset: Int): List<ShoppingProduct> = withContext(Dispatchers.IO) {
        try {
            val dbQuery = "%$query%"
            val entities = productDao.searchProducts(dbQuery, limit, offset)
            if (entities.isNotEmpty()) {
                return@withContext entities.map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to search products in local DB: ${e.message}")
        }

        // Fallback to fuzzy memory search
        val q = query.lowercase().trim()
        if (q.isEmpty()) return@withContext getProducts(limit, offset)

        val memoryMatches = memoryCache.values.filter {
            it.name.lowercase().contains(q) || it.brand.lowercase().contains(q) || it.category.lowercase().contains(q)
        }.drop(offset).take(limit)

        if (memoryMatches.isNotEmpty()) {
            return@withContext memoryMatches
        }

        // Remote secure API query
        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                networkClient.executeSecureCall(
                    serviceName = "ShoppingService",
                    apiPath = "search?q=$query&limit=$limit&offset=$offset"
                ) {
                    val searchResults = createMockProducts().filter {
                        it.name.lowercase().contains(q) || it.brand.lowercase().contains(q) || it.category.lowercase().contains(q)
                    }
                    searchResults.forEach { saveProduct(it) }
                    searchResults
                }
            } catch (e: Exception) {
                Log.e(tag, "Error performing secure remote search: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun syncProducts(): Boolean = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.w(tag, "Offline sync skipped: no network")
            return@withContext false
        }

        try {
            Log.d(tag, "Executing background sync policy...")
            // Perform background sync call through secure API client
            networkClient.executeSecureCall(
                serviceName = "ShoppingSyncService",
                apiPath = "sync"
            ) {
                // Preload / Refresh DB with fresh items
                val refreshedItems = createMockProducts()
                refreshedItems.forEach { saveProduct(it) }
                Log.d(tag, "Background synchronization finished successfully")
                true
            }
        } catch (e: Exception) {
            Log.e(tag, "Background sync failed: ${e.message}")
            false
        }
    }

    private fun createMockProducts(): List<ShoppingProduct> {
        return listOf(
            ShoppingProduct(
                id = "shop_nuphy_75",
                name = "NuPhy Air75 V2 Wireless Mechanical Keyboard",
                brand = "NuPhy",
                category = "Tech & Electronics",
                color = "Gray",
                images = listOf("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80"),
                price = 139.99,
                discount = 15.00,
                currency = "USD",
                storeName = "NuPhy Official",
                productUrl = "https://nuphy.com",
                rating = 4.7f,
                reviewCount = 234,
                aiConfidence = 0.95f,
                aiRecommendationScore = 0.93f,
                deliveryTime = "3-5 business days",
                stockStatus = "In Stock"
            ),
            ShoppingProduct(
                id = "shop_keychron_q1",
                name = "Keychron Q1 Pro QMK Custom Keyboard",
                brand = "Keychron",
                category = "Tech & Electronics",
                color = "Black",
                images = listOf("https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=600&q=80"),
                price = 199.00,
                discount = 0.0,
                currency = "USD",
                storeName = "MechanicalKeyboards Store",
                productUrl = "https://keychron.com",
                rating = 4.9f,
                reviewCount = 98,
                aiConfidence = 0.98f,
                aiRecommendationScore = 0.96f,
                deliveryTime = "2-4 business days",
                stockStatus = "In Stock"
            ),
            ShoppingProduct(
                id = "shop_logitech_mx",
                name = "Logitech MX Master 3S Wireless Mouse",
                brand = "Logitech",
                category = "Tech & Electronics",
                color = "Graphite",
                images = listOf("https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=600&q=80"),
                price = 99.99,
                discount = 5.00,
                currency = "USD",
                storeName = "BestBuy",
                productUrl = "https://bestbuy.com",
                rating = 4.6f,
                reviewCount = 1420,
                aiConfidence = 0.92f,
                aiRecommendationScore = 0.89f,
                deliveryTime = "1-2 business days",
                stockStatus = "In Stock"
            )
        )
    }
}
