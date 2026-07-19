package com.example.data.integration.provider

import com.example.domain.provider.ProviderReviewSummary
import com.example.domain.provider.ReviewIntelligenceProvider

class MockReviewIntelligenceProvider : ReviewIntelligenceProvider {
    override fun getProviderName(): String = "Mock-Review-Intel"

    override suspend fun summarizeReviews(productId: String, productName: String): ProviderReviewSummary {
        val lowerId = productId.lowercase()
        return when {
            lowerId.contains("keyboard") -> ProviderReviewSummary(
                score = 0.92f,
                pros = "Superb metal body weight, smooth tactile keys, fast multi-device Bluetooth switching.",
                cons = "ABS keycaps wear down quickly, high RGB light configuration drains batteries.",
                sampleSize = 485
            )
            else -> ProviderReviewSummary(
                score = 0.88f,
                pros = "Sleek look, highly functional, easy to set up.",
                cons = "Premium price, somewhat delicate build materials.",
                sampleSize = 120
            )
        }
    }
}
