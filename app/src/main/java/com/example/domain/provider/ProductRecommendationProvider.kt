package com.example.domain.provider

import com.example.data.model.Product
import com.example.domain.engine.RecommendedProduct

interface ProductRecommendationProvider {
    fun getProviderName(): String
    suspend fun findSimilarProducts(product: Product): List<RecommendedProduct>
}
