package com.example.domain.engine

import com.example.data.model.Product

interface ProductMatchingEngine {
    suspend fun findMatches(
        objects: List<DetectedObjectVisual>,
        fashionItems: List<DetectedFashionItem>
    ): List<ProductMatchResult>
}

data class ProductMatchResult(
    val matchedProduct: Product,
    val matchConfidence: Float,
    val visualAttributesUsed: List<String>,
    val matchingAlgorithm: String
)
