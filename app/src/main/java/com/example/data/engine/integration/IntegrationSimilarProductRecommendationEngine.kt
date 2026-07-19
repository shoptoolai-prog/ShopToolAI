package com.example.data.engine.integration

import com.example.data.model.Product
import com.example.domain.engine.RecommendedProduct
import com.example.domain.engine.SimilarProductRecommendationEngine
import com.example.domain.provider.ProviderManager

class IntegrationSimilarProductRecommendationEngine(
    private val providerManager: ProviderManager
) : SimilarProductRecommendationEngine {

    override suspend fun recommendSimilar(product: Product): List<RecommendedProduct> {
        val activeProvider = providerManager.getActiveRecommendationProvider()
        return activeProvider.findSimilarProducts(product)
    }
}
