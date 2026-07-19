package com.example.domain.provider

interface ReviewIntelligenceProvider {
    fun getProviderName(): String
    suspend fun summarizeReviews(productId: String, productName: String): ProviderReviewSummary
}

data class ProviderReviewSummary(
    val score: Float,
    val pros: String,
    val cons: String,
    val sampleSize: Int
)
