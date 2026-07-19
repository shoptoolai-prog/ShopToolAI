package com.example.domain.service

interface ReviewAnalysisService {
    suspend fun analyzeReviews(productId: String): ReviewSummary
}

data class ReviewSummary(
    val positiveSummary: String,
    val negativeSummary: String
)
