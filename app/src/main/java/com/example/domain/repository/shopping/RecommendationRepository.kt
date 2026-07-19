package com.example.domain.repository.shopping

import com.example.domain.model.ShoppingProduct

interface RecommendationRepository {
    suspend fun getRecommendations(productId: String, limit: Int = 10): List<ShoppingProduct>
    suspend fun calculateRecommendationScore(product: ShoppingProduct): Float
}
