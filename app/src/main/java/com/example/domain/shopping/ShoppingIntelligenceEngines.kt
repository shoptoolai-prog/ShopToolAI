package com.example.domain.shopping

import com.example.domain.model.ShoppingProduct
import com.example.domain.repository.shopping.ReviewAggregate
import com.example.domain.repository.shopping.SellerInfo

interface ShoppingProvider {
    val name: String
    suspend fun search(query: String, limit: Int, offset: Int): List<ShoppingProduct>
    suspend fun getProductDetails(productId: String): ShoppingProduct?
}

interface ShoppingProviderManager {
    fun getActiveProviders(): List<ShoppingProvider>
    fun registerProvider(provider: ShoppingProvider)
    fun deregisterProvider(providerName: String)
}

interface ProductSearchEngine {
    suspend fun searchAcrossProviders(query: String, limit: Int = 20, offset: Int = 0): List<ShoppingProduct>
}

interface ProductMatchingEngine {
    suspend fun areSameProduct(p1: ShoppingProduct, p2: ShoppingProduct): Boolean
    suspend fun findMatches(target: ShoppingProduct, pool: List<ShoppingProduct>): List<ShoppingProduct>
}

interface PriceComparisonEngine {
    suspend fun comparePrices(productId: String, candidates: List<ShoppingProduct>): List<ShoppingProduct>
    suspend fun getLowestPriceOption(candidates: List<ShoppingProduct>): ShoppingProduct?
}

interface DiscountEngine {
    suspend fun calculateDiscountPercentage(originalPrice: Double, currentPrice: Double): Double
    suspend fun getBestDiscount(candidates: List<ShoppingProduct>): ShoppingProduct?
}

data class ShoppingCoupon(
    val code: String,
    val description: String,
    val discountAmount: Double,
    val isPercent: Boolean
)

interface CouponEngine {
    suspend fun getAvailableCoupons(storeName: String): List<ShoppingCoupon>
    suspend fun applyCoupon(product: ShoppingProduct, couponCode: String): Double
}

interface AvailabilityEngine {
    suspend fun checkStockStatus(product: ShoppingProduct): String
    suspend fun isAvailable(product: ShoppingProduct): Boolean
}

interface DeliveryEstimationEngine {
    suspend fun estimateDelivery(product: ShoppingProduct, destinationZip: String): String
    suspend fun getCheapestShippingOption(product: ShoppingProduct, destinationZip: String): Double
}

interface ReviewAggregationEngine {
    suspend fun aggregateReviews(productId: String, sources: List<ShoppingProduct>): ReviewAggregate
}

interface SellerRankingEngine {
    suspend fun rankSellers(sellers: List<SellerInfo>): List<SellerInfo>
    suspend fun getTopSeller(sellers: List<SellerInfo>): SellerInfo?
}

interface ProductScoringEngine {
    suspend fun calculateRecommendationScore(product: ShoppingProduct): Float
}
