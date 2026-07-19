package com.example.data.integration

import com.example.data.integration.provider.*
import com.example.domain.provider.*

class ProviderManagerImpl(private val networkClient: NetworkClient) : ProviderManager {

    // Default implementations are local mock / simulated engines to ensure offline safety
    private var activeVision: AiVisionProvider = MockAiVisionProvider()
    private var activeSearch: ProductSearchProvider = MockProductSearchProvider()
    private var activePrice: PriceComparisonProvider = MockPriceComparisonProvider()
    private var activeReview: ReviewIntelligenceProvider = MockReviewIntelligenceProvider()
    private var activeRecommendation: ProductRecommendationProvider = MockProductRecommendationProvider()

    // Supported lists of providers
    private val visionProviders = mapOf(
        "MOCK" to MockAiVisionProvider(),
        "GEMINI" to GeminiVisionProvider(networkClient)
    )

    private val searchProviders = mapOf(
        "MOCK" to MockProductSearchProvider(),
        "GOOGLE_SHOPPING" to GoogleShoppingProductSearchProvider(networkClient)
    )

    private val priceProviders = mapOf(
        "MOCK" to MockPriceComparisonProvider(),
        "GOOGLE_SHOPPING" to GoogleShoppingPriceComparisonProvider(networkClient)
    )

    private val reviewProviders = mapOf(
        "MOCK" to MockReviewIntelligenceProvider(),
        "GEMINI" to GeminiReviewIntelligenceProvider(networkClient)
    )

    private val recommendationProviders = mapOf(
        "MOCK" to MockProductRecommendationProvider(),
        "VECTOR_EMBEDDING" to VectorProductRecommendationProvider(networkClient)
    )

    init {
        // Initialize to standard high-performance providers if required
        activeVision = visionProviders["GEMINI"] ?: MockAiVisionProvider()
        activeSearch = searchProviders["GOOGLE_SHOPPING"] ?: MockProductSearchProvider()
        activePrice = priceProviders["GOOGLE_SHOPPING"] ?: MockPriceComparisonProvider()
        activeReview = reviewProviders["GEMINI"] ?: MockReviewIntelligenceProvider()
        activeRecommendation = recommendationProviders["VECTOR_EMBEDDING"] ?: MockProductRecommendationProvider()
    }

    override fun getActiveVisionProvider(): AiVisionProvider = activeVision

    override fun setActiveVisionProvider(providerName: String) {
        visionProviders[providerName.uppercase()]?.let { activeVision = it }
    }

    override fun getActiveSearchProvider(): ProductSearchProvider = activeSearch

    override fun setActiveSearchProvider(providerName: String) {
        searchProviders[providerName.uppercase()]?.let { activeSearch = it }
    }

    override fun getActivePriceProvider(): PriceComparisonProvider = activePrice

    override fun setActivePriceProvider(providerName: String) {
        priceProviders[providerName.uppercase()]?.let { activePrice = it }
    }

    override fun getActiveReviewProvider(): ReviewIntelligenceProvider = activeReview

    override fun setActiveReviewProvider(providerName: String) {
        reviewProviders[providerName.uppercase()]?.let { activeReview = it }
    }

    override fun getActiveRecommendationProvider(): ProductRecommendationProvider = activeRecommendation

    override fun setActiveRecommendationProvider(providerName: String) {
        recommendationProviders[providerName.uppercase()]?.let { activeRecommendation = it }
    }

    override fun getAvailableProviders(): List<String> {
        return listOf("MOCK", "GEMINI", "GOOGLE_SHOPPING", "VECTOR_EMBEDDING")
    }
}
