package com.example.data.intelligence

import android.util.Log
import com.example.domain.engine.ExtractedFrame
import com.example.domain.intelligence.*
import com.example.domain.model.IntelProduct
import com.example.domain.model.IntelProductRef
import com.example.domain.provider.ProviderManager

class ProductDetectionModuleImpl(
    private val providerManager: ProviderManager
) : ProductDetectionModule {
    private val tag = "ProductDetectionModuleImpl"

    override suspend fun detectProducts(frames: List<ExtractedFrame>): List<RawProductDetection> {
        Log.d(tag, "Detecting products across ${frames.size} frames")
        val activeVisionProvider = providerManager.getActiveVisionProvider()
        
        val visionObjects = activeVisionProvider.analyzeFrames(frames)
        return visionObjects.map { obj ->
            RawProductDetection(
                id = obj.id,
                label = obj.label,
                confidence = obj.confidence,
                visualAttributes = obj.visualAttributes,
                boundingBox = obj.boundingBox?.let { Pair(it.xMin, it.yMin) }
            )
        }
    }
}

class BrandRecognitionModuleImpl : BrandRecognitionModule {
    private val tag = "BrandRecognitionModule"
    private val brandsDict = listOf("Keychron", "BenQ", "Logitech", "NuPhy", "Apple", "Sony", "Samsung", "Nike", "Adidas")

    override suspend fun recognizeBrand(productLabel: String, visualAttributes: List<String>): Pair<String, Float> {
        Log.d(tag, "Recognizing brand for label: $productLabel")
        
        // Dynamic search through known brands
        for (brand in brandsDict) {
            if (productLabel.contains(brand, ignoreCase = true) || 
                visualAttributes.any { it.contains(brand, ignoreCase = true) }) {
                return Pair(brand, 0.95f)
            }
        }
        
        // Fallback heuristic
        val fallbackBrand = productLabel.split(" ").firstOrNull() ?: "Generic"
        return Pair(fallbackBrand, 0.70f)
    }
}

class ColorDetectionModuleImpl : ColorDetectionModule {
    private val standardColors = listOf("Black", "White", "Silver", "Gray", "Red", "Blue", "Green", "Yellow", "Gold", "Pink", "RGB")

    override suspend fun detectColors(visualAttributes: List<String>): List<String> {
        val detected = mutableListOf<String>()
        for (attr in visualAttributes) {
            for (color in standardColors) {
                if (attr.contains(color, ignoreCase = true)) {
                    detected.add(color)
                }
            }
        }
        if (detected.isEmpty()) {
            detected.add("Multicolor")
        }
        return detected.distinct()
    }
}

class CategoryDetectionModuleImpl : CategoryDetectionModule {
    override suspend fun detectCategory(productLabel: String, visualAttributes: List<String>): String {
        val labelLower = productLabel.lowercase()
        return when {
            labelLower.contains("keyboard") || labelLower.contains("mouse") || labelLower.contains("monitor") || labelLower.contains("tech") -> "Tech & Electronics"
            labelLower.contains("shirt") || labelLower.contains("pants") || labelLower.contains("jacket") || labelLower.contains("shoe") -> "Apparel & Fashion"
            labelLower.contains("bag") || labelLower.contains("watch") || labelLower.contains("light") -> "Home & Lifestyle Accessories"
            else -> "General Goods"
        }
    }
}

class SimilarProductFinderModuleImpl(
    private val providerManager: ProviderManager
) : SimilarProductFinderModule {
    override suspend fun findSimilar(product: IntelProduct): List<IntelProductRef> {
        val activeRecommendationProvider = providerManager.getActiveRecommendationProvider()
        
        // Map IntelProduct to com.example.data.model.Product for compatibility
        val legacyProduct = com.example.data.model.Product(
            id = product.id,
            name = product.name,
            brand = product.brand,
            imageUrl = product.thumbnail,
            matchPercentage = (product.confidenceScore * 100).toInt(),
            lowestPrice = product.price,
            priceComparison = emptyList(),
            positiveReviewSummary = "",
            negativeReviewSummary = "",
            buyUrl = product.buyUrl
        )

        val recommended = activeRecommendationProvider.findSimilarProducts(legacyProduct)
        return recommended.map { rec ->
            IntelProductRef(
                id = rec.id,
                name = rec.name,
                brand = rec.brand,
                thumbnail = rec.imageUrl,
                price = rec.price,
                similarityScore = rec.similarityScore
            )
        }
    }
}

class DuplicateProductFilterModuleImpl : DuplicateProductFilterModule {
    override suspend fun filterDuplicates(products: List<IntelProduct>): List<IntelProduct> {
        val uniqueProducts = mutableListOf<IntelProduct>()
        for (prod in products) {
            val isDuplicate = uniqueProducts.any { unique ->
                unique.name.equals(prod.name, ignoreCase = true) &&
                unique.brand.equals(prod.brand, ignoreCase = true)
            }
            if (!isDuplicate) {
                uniqueProducts.add(prod)
            }
        }
        return uniqueProducts
    }
}

class ProductRankingModuleImpl : ProductRankingModule {
    override suspend fun rankProducts(products: List<IntelProduct>, context: Map<String, String>?): List<IntelProduct> {
        // Higher confidence and completeness ranked higher
        return products.sortedWith(
            compareByDescending<IntelProduct> { it.confidenceScore }
                .thenByDescending { it.similarProducts.size }
        )
    }
}

class ConfidenceScoreModuleImpl : ConfidenceScoreModule {
    override suspend fun calculateConfidence(
        detectionConfidence: Float,
        brandConfidence: Float,
        matchConfidence: Float
    ): Float {
        // Weighted formula
        val unified = (detectionConfidence * 0.4f) + (brandConfidence * 0.3f) + (matchConfidence * 0.3f)
        return unified.coerceIn(0.0f, 1.0f)
    }
}
