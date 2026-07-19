package com.example.domain.engine

interface ReviewAnalysisEngine {
    suspend fun analyzeSentiment(productId: String, productName: String): EngineReviewSummary
}

data class EngineReviewSummary(
    val score: Float, // 0.0 to 1.0
    val prosSummary: String,
    val consSummary: String,
    val sampleSize: Int
)
