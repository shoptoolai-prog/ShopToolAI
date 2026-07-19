package com.example.domain.provider

interface ProviderManager {
    fun getActiveVisionProvider(): AiVisionProvider
    fun setActiveVisionProvider(providerName: String)

    fun getActiveSearchProvider(): ProductSearchProvider
    fun setActiveSearchProvider(providerName: String)

    fun getActivePriceProvider(): PriceComparisonProvider
    fun setActivePriceProvider(providerName: String)

    fun getActiveReviewProvider(): ReviewIntelligenceProvider
    fun setActiveReviewProvider(providerName: String)

    fun getActiveRecommendationProvider(): ProductRecommendationProvider
    fun setActiveRecommendationProvider(providerName: String)

    fun getAvailableProviders(): List<String>
}
