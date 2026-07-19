package com.example.domain.repository.shopping

data class ReviewAggregate(
    val rating: Float,
    val reviewCount: Int,
    val positiveSummary: String,
    val negativeSummary: String,
    val reviews: List<String>
)

interface ReviewRepository {
    suspend fun getReviewAggregate(productId: String): ReviewAggregate?
    suspend fun saveReviewAggregate(productId: String, aggregate: ReviewAggregate)
}
