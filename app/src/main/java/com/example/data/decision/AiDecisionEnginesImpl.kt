package com.example.data.decision

import android.content.Context
import android.util.Log
import com.example.data.integration.PerformanceMonitor
import com.example.data.local.ShoppingProductDao
import com.example.data.local.ShoppingProductEntity
import com.example.data.util.RetryUtils
import com.example.domain.decision.*
import com.example.domain.model.DecisionProduct
import com.example.domain.model.ShoppingProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

// --- 4. Confidence Score Engine ---
class ConfidenceScoreEngineImpl : ConfidenceScoreEngine {
    override suspend fun calculateConfidence(
        detectionConfidence: Float,
        brandConfidence: Float,
        sellerRating: Float,
        ocrConfidence: Float
    ): Float = withContext(Dispatchers.Default) {
        // detection: 35%, brand: 30%, OCR: 20%, seller: 15% (sellerRating scaled from 0-5 to 0-1)
        val normalizedSeller = (sellerRating / 5.0f).coerceIn(0.0f, 1.0f)
        val score = (detectionConfidence * 0.35f) +
                    (brandConfidence * 0.30f) +
                    (ocrConfidence * 0.20f) +
                    (normalizedSeller * 0.15f)
        score.coerceIn(0.0f, 1.0f)
    }
}

// --- 3. Product Similarity Engine ---
class ProductSimilarityEngineImpl : ProductSimilarityEngine {
    override suspend fun calculateSimilarity(
        queryAttributes: List<String>,
        candidate: ShoppingProduct
    ): Float = withContext(Dispatchers.Default) {
        if (queryAttributes.isEmpty()) return@withContext 1.0f
        
        val normalizedAttrs = queryAttributes.map { it.lowercase().trim() }
        val nameLower = candidate.name.lowercase()
        val brandLower = candidate.brand.lowercase()
        val categoryLower = candidate.category.lowercase()
        val colorLower = candidate.color.lowercase()

        var matches = 0
        normalizedAttrs.forEach { attr ->
            if (nameLower.contains(attr) || 
                brandLower.contains(attr) || 
                categoryLower.contains(attr) || 
                colorLower.contains(attr)) {
                matches++
            }
        }

        // Add direct brand match bonus
        val brandMatchBonus = if (normalizedAttrs.any { brandLower.contains(it) || it.contains(brandLower) }) 0.2f else 0.0f
        
        val matchRatio = matches.toFloat() / normalizedAttrs.size.toFloat()
        val totalSimilarity = (matchRatio * 0.8f) + brandMatchBonus
        totalSimilarity.coerceIn(0.0f, 1.0f)
    }
}

// --- 2. Product Ranking Engine ---
class ProductRankingEngineImpl : ProductRankingEngine {
    override suspend fun calculateScores(
        product: ShoppingProduct,
        similarityPercentage: Float,
        aiConfidenceScore: Float
    ): DecisionProduct = withContext(Dispatchers.Default) {
        // 1. Quality Score based on rating (0.0 to 1.0)
        val qualityScore = (product.rating / 5.0f).coerceIn(0.0f, 1.0f)

        // 2. Value Score based on quality, price, and discounts
        // A discounted highly-rated product has high value score
        val discountBonus = if (product.price > 0.0) (product.discount / product.price).toFloat() else 0.0f
        val valueScore = ((qualityScore * 0.7f) + (discountBonus * 0.3f)).coerceIn(0.0f, 1.0f)

        // 3. Popularity Score based on log scale of reviews and raw rating
        val popularityScore = (Math.log1p(product.reviewCount.toDouble()) / Math.log1p(1000.0))
            .toFloat()
            .coerceIn(0.0f, 1.0f)

        // 4. Price Score (lower price gives a higher price score for standard budget affinity)
        // Set an arbitrary high price boundary of 1000.0 to normalize
        val priceScore = (1.0f - (product.price / 1000.0f).toFloat()).coerceIn(0.0f, 1.0f)

        // 5. Recommendation Score: weighted integration of confidence, similarity, quality, and value
        val recommendationScore = (aiConfidenceScore * 0.3f) +
                                  (similarityPercentage * 0.3f) +
                                  (qualityScore * 0.2f) +
                                  (valueScore * 0.2f)

        // 6. Final Ranking Score: Comprehensive weighted composite
        val finalRankingScore = (aiConfidenceScore * 0.20f) +
                                (similarityPercentage * 0.25f) +
                                (qualityScore * 0.20f) +
                                (valueScore * 0.15f) +
                                (popularityScore * 0.10f) +
                                (priceScore * 0.10f)

        DecisionProduct(
            product = product,
            aiConfidenceScore = aiConfidenceScore,
            similarityPercentage = similarityPercentage,
            recommendationScore = recommendationScore.coerceIn(0.0f, 1.0f),
            qualityScore = qualityScore,
            valueScore = valueScore,
            popularityScore = popularityScore,
            priceScore = priceScore,
            finalRankingScore = finalRankingScore.coerceIn(0.0f, 1.0f)
        )
    }

    override suspend fun rankProducts(
        products: List<DecisionProduct>
    ): List<DecisionProduct> = withContext(Dispatchers.Default) {
        products.sortedByDescending { it.finalRankingScore }
    }
}

// --- 5. Duplicate Removal Engine ---
class DuplicateRemovalEngineImpl : DuplicateRemovalEngine {
    override suspend fun removeDuplicates(
        products: List<DecisionProduct>
    ): List<DecisionProduct> = withContext(Dispatchers.Default) {
        val uniqueProducts = mutableListOf<DecisionProduct>()
        
        for (prod in products) {
            val isDuplicate = uniqueProducts.any { existing ->
                val sameBrand = existing.product.brand.equals(prod.product.brand, ignoreCase = true)
                val sameStore = existing.product.storeName.equals(prod.product.storeName, ignoreCase = true)
                val nameOverlap = calculateNameOverlap(existing.product.name, prod.product.name) >= 0.8f
                
                // If same brand, same store and extremely high name overlap, it is a duplicate
                (sameBrand && sameStore && nameOverlap) || 
                // Or if same ID
                (existing.product.id == prod.product.id)
            }
            if (!isDuplicate) {
                uniqueProducts.add(prod)
            } else {
                // If it is a duplicate, we keep the one with the higher ranking score
                val idx = uniqueProducts.indexOfFirst { 
                    (it.product.brand.equals(prod.product.brand, ignoreCase = true) && 
                     it.product.storeName.equals(prod.product.storeName, ignoreCase = true) &&
                     calculateNameOverlap(it.product.name, prod.product.name) >= 0.8f) ||
                    (it.product.id == prod.product.id)
                }
                if (idx != -1 && prod.finalRankingScore > uniqueProducts[idx].finalRankingScore) {
                    uniqueProducts[idx] = prod
                }
            }
        }
        uniqueProducts
    }

    private fun calculateNameOverlap(n1: String, n2: String): Float {
        val tokens1 = n1.lowercase().split(" ").filter { it.length > 2 }.toSet()
        val tokens2 = n2.lowercase().split(" ").filter { it.length > 2 }.toSet()
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0f
        val common = tokens1.intersect(tokens2)
        return common.size.toFloat() / maxOf(tokens1.size, tokens2.size).toFloat()
    }
}

// --- 6. Best Match Engine ---
class BestMatchEngineImpl : BestMatchEngine {
    override suspend fun findBestMatch(
        detectedQuery: String,
        candidates: List<DecisionProduct>
    ): DecisionProduct? = withContext(Dispatchers.Default) {
        val inStock = candidates.filter { it.product.stockStatus.contains("In Stock", ignoreCase = true) }
        if (inStock.isEmpty()) {
            candidates.maxByOrNull { it.finalRankingScore }
        } else {
            inStock.maxByOrNull { it.finalRankingScore }
        }
    }
}

// --- 7. Budget Recommendation Engine ---
class BudgetRecommendationEngineImpl : BudgetRecommendationEngine {
    override suspend fun recommendBudgetOptions(
        candidates: List<DecisionProduct>,
        maxPrice: Double?
    ): List<DecisionProduct> = withContext(Dispatchers.Default) {
        if (candidates.isEmpty()) return@withContext emptyList()

        val threshold = maxPrice ?: run {
            // Default threshold is the average price of candidates
            candidates.map { it.product.price }.average()
        }

        candidates.filter { (it.product.price - it.product.discount) <= threshold }
            .sortedWith(compareByDescending<DecisionProduct> { it.valueScore }.thenByDescending { it.finalRankingScore })
    }
}

// --- 8. Premium Recommendation Engine ---
class PremiumRecommendationEngineImpl : PremiumRecommendationEngine {
    override suspend fun recommendPremiumOptions(
        candidates: List<DecisionProduct>
    ): List<DecisionProduct> = withContext(Dispatchers.Default) {
        if (candidates.isEmpty()) return@withContext emptyList()

        val avgPrice = candidates.map { it.product.price }.average()
        
        // Premium products are in top price tiers with high rating
        candidates.filter { it.product.price >= avgPrice && it.product.rating >= 4.0f }
            .sortedWith(compareByDescending<DecisionProduct> { it.qualityScore }.thenByDescending { it.finalRankingScore })
    }
}

// --- 9. Trending Product Engine ---
class TrendingProductEngineImpl : TrendingProductEngine {
    override suspend fun getTrendingProducts(
        candidates: List<DecisionProduct>
    ): List<DecisionProduct> = withContext(Dispatchers.Default) {
        // High review counts and high popularity scores
        candidates.sortedWith(compareByDescending<DecisionProduct> { it.popularityScore }.thenByDescending { it.product.reviewCount })
    }
}

// --- 10. Smart Recommendation Engine ---
class SmartRecommendationEngineImpl : SmartRecommendationEngine {
    override suspend fun generateSmartRecommendations(
        candidates: List<DecisionProduct>,
        userBudget: Double?,
        preferredBrand: String?
    ): List<DecisionProduct> = withContext(Dispatchers.Default) {
        if (candidates.isEmpty()) return@withContext emptyList()

        candidates.map { candidate ->
            var boost = 0.0f
            
            // Apply budget context boost
            userBudget?.let { budget ->
                val finalPrice = candidate.product.price - candidate.product.discount
                if (finalPrice <= budget) {
                    // Closer to budget but under gets higher boost
                    val ratio = (finalPrice / budget).toFloat()
                    boost += ratio * 0.15f
                } else {
                    // Penalty for exceeding budget
                    boost -= ((finalPrice - budget) / budget).toFloat().coerceAtMost(0.5f)
                }
            }

            // Apply brand preferences boost
            preferredBrand?.let { brand ->
                if (candidate.product.brand.equals(brand, ignoreCase = true)) {
                    boost += 0.35f
                }
            }

            val adjustedScore = (candidate.finalRankingScore + boost).coerceIn(0.0f, 1.0f)
            Pair(candidate, adjustedScore)
        }.sortedByDescending { it.second }.map { it.first }
    }
}

// --- 1. AI Decision Manager Implementation ---
class AiDecisionManagerImpl(
    private val context: Context,
    private val productDao: ShoppingProductDao,
    private val rankingEngine: ProductRankingEngine,
    private val similarityEngine: ProductSimilarityEngine,
    private val confidenceEngine: ConfidenceScoreEngine,
    private val duplicateEngine: DuplicateRemovalEngine,
    private val bestMatchEngine: BestMatchEngine,
    private val budgetEngine: BudgetRecommendationEngine,
    private val premiumEngine: PremiumRecommendationEngine,
    private val trendingEngine: TrendingProductEngine,
    private val smartEngine: SmartRecommendationEngine
) : AiDecisionManager {

    private val tag = "AiDecisionManagerImpl"
    private val decisionCache = ConcurrentHashMap<String, DecisionResult>()

    override suspend fun processAndDecisionQuery(
        detectedQuery: String,
        queryAttributes: List<String>,
        candidates: List<ShoppingProduct>,
        userBudget: Double?,
        preferredBrand: String?
    ): DecisionResult = coroutineScope {
        val cacheKey = "$detectedQuery-${queryAttributes.joinToString(",")}-$userBudget-$preferredBrand"
        
        decisionCache[cacheKey]?.let {
            Log.d(tag, "Memory cache hit for decision query: $detectedQuery")
            return@coroutineScope it
        }

        PerformanceMonitor.measureCall("AiDecisionManager", "processAndDecisionQuery") {
            try {
                // Background retry-supported computation
                RetryUtils.retryWithTimeout(
                    timeoutMs = 8000L,
                    maxRetries = 2,
                    initialDelayMs = 200L,
                    operationName = "DecisionEngineExecution"
                ) {
                    // 1. Calculate similarity and confidence in parallel for all candidates
                    val evaluatedDeferred = candidates.map { candidate ->
                        async(Dispatchers.Default) {
                            val similarity = similarityEngine.calculateSimilarity(queryAttributes, candidate)
                            val confidence = confidenceEngine.calculateConfidence(
                                detectionConfidence = candidate.aiConfidence,
                                brandConfidence = if (candidate.brand.isNotEmpty()) 0.90f else 0.50f,
                                sellerRating = candidate.rating,
                                ocrConfidence = 0.95f
                            )
                            rankingEngine.calculateScores(candidate, similarity, confidence)
                        }
                    }

                    val evaluatedProducts = evaluatedDeferred.map { it.await() }

                    // 2. Remove Duplicates
                    val uniqueEvaluated = duplicateEngine.removeDuplicates(evaluatedProducts)

                    // 3. Rank products
                    val ranked = rankingEngine.rankProducts(uniqueEvaluated)

                    // 4. Find Best Match
                    val bestMatch = bestMatchEngine.findBestMatch(detectedQuery, ranked)

                    // 5. Generate Recommendations in parallel
                    val budgetDeferred = async { budgetEngine.recommendBudgetOptions(ranked, userBudget) }
                    val premiumDeferred = async { premiumEngine.recommendPremiumOptions(ranked) }
                    val trendingDeferred = async { trendingEngine.getTrendingProducts(ranked) }
                    val smartDeferred = async { smartEngine.generateSmartRecommendations(ranked, userBudget, preferredBrand) }

                    val result = DecisionResult(
                        bestMatch = bestMatch,
                        rankedProducts = ranked,
                        budgetOptions = budgetDeferred.await(),
                        premiumOptions = premiumDeferred.await(),
                        trendingOptions = trendingDeferred.await(),
                        smartRecommendations = smartDeferred.await()
                    )

                    // Save best-match details to Room as local cache
                    bestMatch?.let {
                        try {
                            productDao.insertProduct(
                                ShoppingProductEntity(
                                    id = it.product.id,
                                    name = it.product.name,
                                    brand = it.product.brand,
                                    category = it.product.category,
                                    color = it.product.color,
                                    images = it.product.images,
                                    price = it.product.price,
                                    discount = it.product.discount,
                                    currency = it.product.currency,
                                    storeName = it.product.storeName,
                                    productUrl = it.product.productUrl,
                                    rating = it.product.rating,
                                    reviewCount = it.product.reviewCount,
                                    aiConfidence = it.aiConfidenceScore,
                                    aiRecommendationScore = it.recommendationScore,
                                    deliveryTime = it.product.deliveryTime,
                                    stockStatus = it.product.stockStatus,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                            Log.d(tag, "Persisted best-match product to Room DB: ${it.product.id}")
                        } catch (e: Exception) {
                            Log.e(tag, "Failed to persist best-match to local DB: ${e.message}")
                        }
                    }

                    decisionCache[cacheKey] = result
                    result
                }
            } catch (e: Exception) {
                Log.e(tag, "Decision Engine process failed: ${e.message}. Triggering recovery fallback...")
                
                // Recovery Fallback: Map raw candidates with default metrics so the system never crashes
                val fallbackProducts = candidates.map {
                    DecisionProduct(
                        product = it,
                        aiConfidenceScore = it.aiConfidence,
                        similarityPercentage = 0.5f,
                        recommendationScore = it.aiRecommendationScore,
                        qualityScore = 0.5f,
                        valueScore = 0.5f,
                        popularityScore = 0.5f,
                        priceScore = 0.5f,
                        finalRankingScore = 0.5f
                    )
                }

                DecisionResult(
                    bestMatch = fallbackProducts.firstOrNull(),
                    rankedProducts = fallbackProducts,
                    budgetOptions = fallbackProducts,
                    premiumOptions = fallbackProducts,
                    trendingOptions = fallbackProducts,
                    smartRecommendations = fallbackProducts
                )
            }
        }
    }
}
