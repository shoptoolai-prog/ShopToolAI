package com.example.domain.intelligence

import com.example.domain.engine.ExtractedFrame
import com.example.domain.model.IntelProduct
import com.example.domain.model.IntelProductRef

data class RawProductDetection(
    val id: String,
    val label: String,
    val confidence: Float,
    val visualAttributes: List<String>,
    val boundingBox: Pair<Float, Float>? = null
)

interface ProductDetectionModule {
    suspend fun detectProducts(frames: List<ExtractedFrame>): List<RawProductDetection>
}

interface BrandRecognitionModule {
    suspend fun recognizeBrand(productLabel: String, visualAttributes: List<String>): Pair<String, Float>
}

interface ColorDetectionModule {
    suspend fun detectColors(visualAttributes: List<String>): List<String>
}

interface CategoryDetectionModule {
    suspend fun detectCategory(productLabel: String, visualAttributes: List<String>): String
}

interface SimilarProductFinderModule {
    suspend fun findSimilar(product: IntelProduct): List<IntelProductRef>
}

interface DuplicateProductFilterModule {
    suspend fun filterDuplicates(products: List<IntelProduct>): List<IntelProduct>
}

interface ProductRankingModule {
    suspend fun rankProducts(products: List<IntelProduct>, context: Map<String, String>? = null): List<IntelProduct>
}

interface ConfidenceScoreModule {
    suspend fun calculateConfidence(detectionConfidence: Float, brandConfidence: Float, matchConfidence: Float): Float
}
