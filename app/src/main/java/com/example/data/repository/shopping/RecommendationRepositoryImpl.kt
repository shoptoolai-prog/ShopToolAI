package com.example.data.repository.shopping

import android.content.Context
import android.util.Log
import com.example.data.integration.NetworkClient
import com.example.data.util.NetworkUtils
import com.example.domain.model.ShoppingProduct
import com.example.domain.repository.shopping.RecommendationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class RecommendationRepositoryImpl(
    private val context: Context,
    private val networkClient: NetworkClient = NetworkClient(context)
) : RecommendationRepository {

    private val tag = "ShoppingRecommendationRepo"
    private val memoryCache = ConcurrentHashMap<String, List<ShoppingProduct>>()

    override suspend fun getRecommendations(productId: String, limit: Int): List<ShoppingProduct> = withContext(Dispatchers.IO) {
        memoryCache[productId]?.let {
            Log.d(tag, "Memory cache hit for recommendations of $productId")
            return@withContext it.take(limit)
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                networkClient.executeSecureCall(
                    serviceName = "ShoppingRecommendationService",
                    apiPath = "recommendations/$productId"
                ) {
                    val list = listOf(
                        ShoppingProduct(
                            id = "rec_nuphy_halo75",
                            name = "NuPhy Halo75 Wireless Mechanical Keyboard",
                            brand = "NuPhy",
                            category = "Tech & Electronics",
                            color = "White",
                            images = listOf("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80"),
                            price = 129.99,
                            discount = 10.00,
                            currency = "USD",
                            storeName = "NuPhy Official Store",
                            productUrl = "https://nuphy.com",
                            rating = 4.8f,
                            reviewCount = 145,
                            aiConfidence = 0.94f,
                            aiRecommendationScore = 0.92f,
                            deliveryTime = "3-5 business days",
                            stockStatus = "In Stock"
                        ),
                        ShoppingProduct(
                            id = "rec_keychron_k2",
                            name = "Keychron K2 Version 2 Wireless Keyboard",
                            brand = "Keychron",
                            category = "Tech & Electronics",
                            color = "Gray",
                            images = listOf("https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=600&q=80"),
                            price = 79.99,
                            discount = 0.0,
                            currency = "USD",
                            storeName = "Keychron Direct",
                            productUrl = "https://keychron.com",
                            rating = 4.6f,
                            reviewCount = 412,
                            aiConfidence = 0.90f,
                            aiRecommendationScore = 0.88f,
                            deliveryTime = "2-4 business days",
                            stockStatus = "In Stock"
                        )
                    )
                    memoryCache[productId] = list
                    list.take(limit)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to get recommendations for $productId: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun calculateRecommendationScore(product: ShoppingProduct): Float {
        val baseScore = product.rating / 5.0f
        val discountBonus = if (product.discount > 0.0) 0.1f else 0.0f
        val score = (baseScore * 0.5f) + (product.aiConfidence * 0.4f) + discountBonus
        return score.coerceIn(0.0f, 1.0f)
    }
}
