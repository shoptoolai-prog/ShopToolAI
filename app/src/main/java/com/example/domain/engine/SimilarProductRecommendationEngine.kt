package com.example.domain.engine

import com.example.data.model.Product

interface SimilarProductRecommendationEngine {
    suspend fun recommendSimilar(product: Product): List<RecommendedProduct>
}

data class RecommendedProduct(
    val id: String,
    val name: String,
    val brand: String,
    val imageUrl: String,
    val price: String,
    val buyUrl: String,
    val similarityScore: Float
)
