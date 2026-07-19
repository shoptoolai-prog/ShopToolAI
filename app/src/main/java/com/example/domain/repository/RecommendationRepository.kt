package com.example.domain.repository

import com.example.domain.model.IntelProductRef

interface RecommendationRepository {
    suspend fun getRecommendationsForProduct(productId: String, limit: Int = 10): List<IntelProductRef>
    suspend fun updateRecommendations(productId: String, recommendations: List<IntelProductRef>)
}
