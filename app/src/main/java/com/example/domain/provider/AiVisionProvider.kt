package com.example.domain.provider

import com.example.domain.engine.ExtractedFrame

interface AiVisionProvider {
    fun getProviderName(): String
    suspend fun analyzeFrames(frames: List<ExtractedFrame>): List<VisionObject>
}

data class VisionObject(
    val id: String,
    val label: String,
    val confidence: Float,
    val visualAttributes: List<String>,
    val category: String, // e.g. Apparel, Accessory, Tech
    val boundingBox: VisionBoundingBox? = null
)

data class VisionBoundingBox(
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float
)
