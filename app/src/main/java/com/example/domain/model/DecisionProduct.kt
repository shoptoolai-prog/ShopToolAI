package com.example.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DecisionProduct(
    val product: ShoppingProduct,
    val aiConfidenceScore: Float,       // AI Confidence Score
    val similarityPercentage: Float,    // Similarity Percentage
    val recommendationScore: Float,     // Recommendation Score
    val qualityScore: Float,            // Quality Score
    val valueScore: Float,              // Value Score
    val popularityScore: Float,          // Popularity Score
    val priceScore: Float,               // Price Score
    val finalRankingScore: Float         // Final Ranking Score
)
