package com.example.domain.service

interface AiProductDetectionService {
    suspend fun detectProductsInVideo(reel: ProcessedReel): List<DetectedObject>
}

data class DetectedObject(
    val category: String,
    val visualFeatures: List<String>,
    val confidence: Float
)
