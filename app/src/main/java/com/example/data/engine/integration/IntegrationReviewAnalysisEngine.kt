package com.example.data.engine.integration

import com.example.domain.engine.EngineReviewSummary
import com.example.domain.engine.ReviewAnalysisEngine
import com.example.domain.provider.ProviderManager

class IntegrationReviewAnalysisEngine(
    private val providerManager: ProviderManager
) : ReviewAnalysisEngine {

    override suspend fun analyzeSentiment(productId: String, productName: String): EngineReviewSummary {
        val activeReviewProvider = providerManager.getActiveReviewProvider()
        val providerSummary = activeReviewProvider.summarizeReviews(productId, productName)

        return EngineReviewSummary(
            score = providerSummary.score,
            prosSummary = providerSummary.pros,
            consSummary = providerSummary.cons,
            sampleSize = providerSummary.sampleSize
        )
    }
}
