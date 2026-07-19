package com.example.data.engine.integration

import com.example.domain.engine.DetectedFashionItem
import com.example.domain.engine.DetectedObjectVisual
import com.example.domain.engine.ProductMatchResult
import com.example.domain.engine.ProductMatchingEngine
import com.example.domain.provider.ProviderManager
import com.example.domain.provider.VisionObject

class IntegrationProductMatchingEngine(
    private val providerManager: ProviderManager
) : ProductMatchingEngine {

    override suspend fun findMatches(
        objects: List<DetectedObjectVisual>,
        fashionItems: List<DetectedFashionItem>
    ): List<ProductMatchResult> {
        val activeSearchProvider = providerManager.getActiveSearchProvider()

        // Gather all visual objects to query the search indexes
        val queryObjects = objects.map { obj ->
            VisionObject(
                id = obj.objectId,
                label = obj.label,
                confidence = obj.confidence,
                visualAttributes = obj.visualAttributes,
                category = "General"
            )
        } + fashionItems.map { fash ->
            VisionObject(
                id = fash.id,
                label = fash.category,
                confidence = fash.confidence,
                visualAttributes = fash.styleTags,
                category = "Fashion"
            )
        }

        val productsFound = activeSearchProvider.searchProductsByVisuals(queryObjects)

        return productsFound.map { product ->
            ProductMatchResult(
                matchedProduct = product,
                matchConfidence = (product.matchPercentage.toFloat() / 100.0f),
                visualAttributesUsed = queryObjects.flatMap { it.visualAttributes }.distinct().take(3),
                matchingAlgorithm = activeSearchProvider.getProviderName()
            )
        }
    }
}
