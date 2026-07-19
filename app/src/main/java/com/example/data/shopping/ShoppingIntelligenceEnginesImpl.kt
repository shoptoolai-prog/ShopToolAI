package com.example.data.shopping

import android.util.Log
import com.example.domain.model.ShoppingProduct
import com.example.domain.repository.shopping.ReviewAggregate
import com.example.domain.repository.shopping.SellerInfo
import com.example.domain.shopping.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.CopyOnWriteArrayList

// --- Mock Shopping Providers ---

class MockAmazonProvider : ShoppingProvider {
    override val name: String = "Amazon"
    
    override suspend fun search(query: String, limit: Int, offset: Int): List<ShoppingProduct> {
        Log.d("MockAmazonProvider", "Searching for '$query' on Amazon")
        return listOf(
            ShoppingProduct(
                id = "amz_nuphy_75",
                name = "NuPhy Air75 V2 Low Profile Keyboard on Amazon",
                brand = "NuPhy",
                category = "Tech & Electronics",
                color = "Gray",
                images = listOf("https://images.unsplash.com/photo-1587829741301-dc798b83add3"),
                price = 145.00,
                discount = 5.00,
                currency = "USD",
                storeName = "Amazon",
                productUrl = "https://amazon.com/dp/B0D12345",
                rating = 4.6f,
                reviewCount = 94,
                aiConfidence = 0.94f,
                aiRecommendationScore = 0.90f,
                deliveryTime = "2 days",
                stockStatus = "In Stock"
            )
        )
    }

    override suspend fun getProductDetails(productId: String): ShoppingProduct? {
        return search(productId, 1, 0).firstOrNull()
    }
}

class MockEBayProvider : ShoppingProvider {
    override val name: String = "eBay"

    override suspend fun search(query: String, limit: Int, offset: Int): List<ShoppingProduct> {
        Log.d("MockEBayProvider", "Searching for '$query' on eBay")
        return listOf(
            ShoppingProduct(
                id = "ebay_nuphy_75",
                name = "NuPhy Air75 V2 Low Profile Keyboard",
                brand = "NuPhy",
                category = "Tech & Electronics",
                color = "Gray",
                images = listOf("https://images.unsplash.com/photo-1587829741301-dc798b83add3"),
                price = 135.00,
                discount = 0.0,
                currency = "USD",
                storeName = "EBay Electronics Depot",
                productUrl = "https://ebay.com/itm/123456",
                rating = 4.8f,
                reviewCount = 14,
                aiConfidence = 0.95f,
                aiRecommendationScore = 0.92f,
                deliveryTime = "4-7 days",
                stockStatus = "In Stock"
            )
        )
    }

    override suspend fun getProductDetails(productId: String): ShoppingProduct? {
        return search(productId, 1, 0).firstOrNull()
    }
}

// --- Shopping Provider Manager Implementation ---

class ShoppingProviderManagerImpl : ShoppingProviderManager {
    private val providers = CopyOnWriteArrayList<ShoppingProvider>()

    init {
        // Register default mock providers for scalable execution
        registerProvider(MockAmazonProvider())
        registerProvider(MockEBayProvider())
    }

    override fun getActiveProviders(): List<ShoppingProvider> {
        return providers.toList()
    }

    override fun registerProvider(provider: ShoppingProvider) {
        if (providers.none { it.name.equals(provider.name, ignoreCase = true) }) {
            providers.add(provider)
            Log.d("ShoppingProviderManager", "Registered shopping provider: ${provider.name}")
        }
    }

    override fun deregisterProvider(providerName: String) {
        providers.removeIf { it.name.equals(providerName, ignoreCase = true) }
        Log.d("ShoppingProviderManager", "Deregistered shopping provider: $providerName")
    }
}

// --- Product Search Engine ---

class ProductSearchEngineImpl(
    private val providerManager: ShoppingProviderManager
) : ProductSearchEngine {
    private val tag = "ProductSearchEngineImpl"

    override suspend fun searchAcrossProviders(query: String, limit: Int, offset: Int): List<ShoppingProduct> = coroutineScope {
        Log.d(tag, "Parallel cross-provider search triggered for '$query'")
        val active = providerManager.getActiveProviders()
        
        val deferreds = active.map { provider ->
            async {
                try {
                    provider.search(query, limit, offset)
                } catch (e: Exception) {
                    Log.e(tag, "Provider '${provider.name}' search failed: ${e.message}")
                    emptyList()
                }
            }
        }
        
        val results = deferreds.awaitAll().flatten()
        Log.d(tag, "Aggregated ${results.size} total products from ${active.size} providers")
        results
    }
}

// --- Product Matching Engine ---

class ProductMatchingEngineImpl : ProductMatchingEngine {
    override suspend fun areSameProduct(p1: ShoppingProduct, p2: ShoppingProduct): Boolean {
        if (!p1.brand.equals(p2.brand, ignoreCase = true)) return false
        
        // Simple token matching
        val tokens1 = p1.name.lowercase().split(" ").toSet()
        val tokens2 = p2.name.lowercase().split(" ").toSet()
        val common = tokens1.intersect(tokens2)
        val similarity = common.size.toFloat() / maxOf(tokens1.size, tokens2.size)
        
        return similarity >= 0.5f
    }

    override suspend fun findMatches(target: ShoppingProduct, pool: List<ShoppingProduct>): List<ShoppingProduct> {
        return pool.filter { areSameProduct(target, it) }
    }
}

// --- Price Comparison Engine ---

class PriceComparisonEngineImpl : PriceComparisonEngine {
    override suspend fun comparePrices(productId: String, candidates: List<ShoppingProduct>): List<ShoppingProduct> {
        return candidates.sortedBy { it.price - it.discount }
    }

    override suspend fun getLowestPriceOption(candidates: List<ShoppingProduct>): ShoppingProduct? {
        return candidates.minByOrNull { it.price - it.discount }
    }
}

// --- Discount Engine ---

class DiscountEngineImpl : DiscountEngine {
    override suspend fun calculateDiscountPercentage(originalPrice: Double, currentPrice: Double): Double {
        if (originalPrice <= 0.0) return 0.0
        val diff = originalPrice - currentPrice
        return ((diff / originalPrice) * 100).coerceAtLeast(0.0)
    }

    override suspend fun getBestDiscount(candidates: List<ShoppingProduct>): ShoppingProduct? {
        return candidates.maxByOrNull { calculateDiscountPercentage(it.price, it.price - it.discount) }
    }
}

// --- Coupon Engine ---

class CouponEngineImpl : CouponEngine {
    private val storeCoupons = mapOf(
        "Amazon" to listOf(ShoppingCoupon("AMZN10", "10% Off Amazon Tech", 10.0, true)),
        "eBay" to listOf(ShoppingCoupon("EBAY5", "$5 Off Accessories", 5.0, false)),
        "NuPhy Official" to listOf(ShoppingCoupon("NUPHY15", "15% Off Mechanical Keyboards", 15.0, true))
    )

    override suspend fun getAvailableCoupons(storeName: String): List<ShoppingCoupon> {
        return storeCoupons[storeName] ?: emptyList()
    }

    override suspend fun applyCoupon(product: ShoppingProduct, couponCode: String): Double {
        val coupons = getAvailableCoupons(product.storeName)
        val match = coupons.find { it.code.equals(couponCode, ignoreCase = true) } ?: return product.price - product.discount
        
        val currentPrice = product.price - product.discount
        val finalPrice = if (match.isPercent) {
            currentPrice * (1.0 - (match.discountAmount / 100.0))
        } else {
            currentPrice - match.discountAmount
        }
        return finalPrice.coerceAtLeast(0.0)
    }
}

// --- Availability Engine ---

class AvailabilityEngineImpl : AvailabilityEngine {
    override suspend fun checkStockStatus(product: ShoppingProduct): String {
        return if (product.stockStatus.contains("In Stock", ignoreCase = true)) "Available" else "Out of Stock"
    }

    override suspend fun isAvailable(product: ShoppingProduct): Boolean {
        return checkStockStatus(product) == "Available"
    }
}

// --- Delivery Estimation Engine ---

class DeliveryEstimationEngineImpl : DeliveryEstimationEngine {
    override suspend fun estimateDelivery(product: ShoppingProduct, destinationZip: String): String {
        // Zip heuristic
        val numericZip = destinationZip.filter { it.isDigit() }.toIntOrNull() ?: 10001
        return if (numericZip % 2 == 0) {
            "Estimated Delivery: 2 business days (Express)"
        } else {
            "Estimated Delivery: 4-5 business days (Standard)"
        }
    }

    override suspend fun getCheapestShippingOption(product: ShoppingProduct, destinationZip: String): Double {
        return if (product.price > 100.0) 0.0 else 5.99
    }
}

// --- Review Aggregation Engine ---

class ReviewAggregationEngineImpl : ReviewAggregationEngine {
    override suspend fun aggregateReviews(productId: String, sources: List<ShoppingProduct>): ReviewAggregate {
        if (sources.isEmpty()) {
            return ReviewAggregate(
                rating = 4.5f,
                reviewCount = 0,
                positiveSummary = "No active reviews yet.",
                negativeSummary = "No active reviews yet.",
                reviews = emptyList()
            )
        }
        
        val averageRating = sources.map { it.rating }.average().toFloat()
        val totalCount = sources.map { it.reviewCount }.sum()
        
        return ReviewAggregate(
            rating = averageRating,
            reviewCount = totalCount,
            positiveSummary = "Positive feedback highlighting strong construction and comfortable feel.",
            negativeSummary = "Minor critiques regarding key spacing and price points.",
            reviews = listOf(
                "Overall highly recommended product across multiple stores.",
                "Responsive design and high build durability."
            )
        )
    }
}

// --- Seller Ranking Engine ---

class SellerRankingEngineImpl : SellerRankingEngine {
    override suspend fun rankSellers(sellers: List<SellerInfo>): List<SellerInfo> {
        // Higher score ranks first
        return sellers.sortedByDescending { it.score }
    }

    override suspend fun getTopSeller(sellers: List<SellerInfo>): SellerInfo? {
        return rankSellers(sellers).firstOrNull()
    }
}

// --- Product Scoring Engine ---

class ProductScoringEngineImpl(
    private val recommendationRepository: com.example.domain.repository.shopping.RecommendationRepository
) : ProductScoringEngine {
    override suspend fun calculateRecommendationScore(product: ShoppingProduct): Float {
        return recommendationRepository.calculateRecommendationScore(product)
    }
}
