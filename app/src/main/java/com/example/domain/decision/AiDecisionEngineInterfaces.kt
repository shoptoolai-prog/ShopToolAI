package com.example.domain.decision

import com.example.domain.model.DecisionProduct
import com.example.domain.model.ShoppingProduct

// 1. AI Decision Manager interface
interface AiDecisionManager {
    suspend fun processAndDecisionQuery(
        detectedQuery: String,
        queryAttributes: List<String>,
        candidates: List<ShoppingProduct>,
        userBudget: Double? = null,
        preferredBrand: String? = null
    ): DecisionResult
}

data class DecisionResult(
    val bestMatch: DecisionProduct?,
    val rankedProducts: List<DecisionProduct>,
    val budgetOptions: List<DecisionProduct>,
    val premiumOptions: List<DecisionProduct>,
    val trendingOptions: List<DecisionProduct>,
    val smartRecommendations: List<DecisionProduct>
)

// 2. Product Ranking Engine
interface ProductRankingEngine {
    suspend fun calculateScores(
        product: ShoppingProduct,
        similarityPercentage: Float,
        aiConfidenceScore: Float
    ): DecisionProduct

    suspend fun rankProducts(
        products: List<DecisionProduct>
    ): List<DecisionProduct>
}

// 3. Product Similarity Engine
interface ProductSimilarityEngine {
    suspend fun calculateSimilarity(
        queryAttributes: List<String>,
        candidate: ShoppingProduct
    ): Float
}

// 4. Confidence Score Engine
interface ConfidenceScoreEngine {
    suspend fun calculateConfidence(
        detectionConfidence: Float,
        brandConfidence: Float,
        sellerRating: Float,
        ocrConfidence: Float = 1.0f
    ): Float
}

// 5. Duplicate Removal Engine
interface DuplicateRemovalEngine {
    suspend fun removeDuplicates(
        products: List<DecisionProduct>
    ): List<DecisionProduct>
}

// 6. Best Match Engine
interface BestMatchEngine {
    suspend fun findBestMatch(
        detectedQuery: String,
        candidates: List<DecisionProduct>
    ): DecisionProduct?
}

// 7. Budget Recommendation Engine
interface BudgetRecommendationEngine {
    suspend fun recommendBudgetOptions(
        candidates: List<DecisionProduct>,
        maxPrice: Double? = null
    ): List<DecisionProduct>
}

// 8. Premium Recommendation Engine
interface PremiumRecommendationEngine {
    suspend fun recommendPremiumOptions(
        candidates: List<DecisionProduct>
    ): List<DecisionProduct>
}

// 9. Trending Product Engine
interface TrendingProductEngine {
    suspend fun getTrendingProducts(
        candidates: List<DecisionProduct>
    ): List<DecisionProduct>
}

// 10. Smart Recommendation Engine
interface SmartRecommendationEngine {
    suspend fun generateSmartRecommendations(
        candidates: List<DecisionProduct>,
        userBudget: Double? = null,
        preferredBrand: String? = null
    ): List<DecisionProduct>
}
