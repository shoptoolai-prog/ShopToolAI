package com.example.data.repository.shopping

import android.content.Context
import android.util.Log
import com.example.data.integration.NetworkClient
import com.example.data.util.NetworkUtils
import com.example.domain.model.ShoppingProduct
import com.example.domain.repository.shopping.PriceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class PriceRepositoryImpl(
    private val context: Context,
    private val networkClient: NetworkClient = NetworkClient(context)
) : PriceRepository {

    private val tag = "PriceRepositoryImpl"
    private val priceCache = ConcurrentHashMap<String, List<ShoppingProduct>>()
    private val historicalCache = ConcurrentHashMap<String, List<Pair<Long, Double>>>()

    override suspend fun getPriceComparison(productId: String): List<ShoppingProduct> = withContext(Dispatchers.IO) {
        priceCache[productId]?.let {
            Log.d(tag, "Memory cache hit for price comparison of $productId")
            return@withContext it
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                networkClient.executeSecureCall(
                    serviceName = "PriceComparisonService",
                    apiPath = "prices/$productId"
                ) {
                    val result = listOf(
                        ShoppingProduct(
                            id = "${productId}_store1",
                            name = "NuPhy Air75 V2",
                            brand = "NuPhy",
                            category = "Tech",
                            color = "Gray",
                            images = emptyList(),
                            price = 139.99,
                            discount = 15.0,
                            currency = "USD",
                            storeName = "NuPhy Official Store",
                            productUrl = "https://nuphy.com",
                            rating = 4.7f,
                            reviewCount = 200,
                            aiConfidence = 0.95f,
                            aiRecommendationScore = 0.93f,
                            deliveryTime = "3-5 days",
                            stockStatus = "In Stock"
                        ),
                        ShoppingProduct(
                            id = "${productId}_store2",
                            name = "NuPhy Air75 V2 Keyboard",
                            brand = "NuPhy",
                            category = "Tech",
                            color = "Gray",
                            images = emptyList(),
                            price = 149.00,
                            discount = 0.0,
                            currency = "USD",
                            storeName = "Amazon",
                            productUrl = "https://amazon.com",
                            rating = 4.5f,
                            reviewCount = 50,
                            aiConfidence = 0.91f,
                            aiRecommendationScore = 0.88f,
                            deliveryTime = "2 days",
                            stockStatus = "In Stock"
                        )
                    )
                    priceCache[productId] = result
                    result
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to fetch price comparison for $productId: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getHistoricalPrices(productId: String): List<Pair<Long, Double>> = withContext(Dispatchers.IO) {
        historicalCache[productId]?.let {
            return@withContext it
        }

        val currentTime = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val mockHistory = listOf(
            Pair(currentTime - 30 * dayMs, 159.99),
            Pair(currentTime - 15 * dayMs, 149.99),
            Pair(currentTime, 139.99)
        )
        historicalCache[productId] = mockHistory
        mockHistory
    }

    override suspend fun updatePrice(productId: String, storeName: String, price: Double) {
        withContext(Dispatchers.IO) {
            Log.d(tag, "Updated price for $productId at $storeName: $price")
        }
    }
}
