package com.example.domain.engine

interface VideoFrameExtractorEngine {
    suspend fun extractKeyframes(videoUrl: String, sampleRateHz: Float): List<ExtractedFrame>
}

data class ExtractedFrame(
    val frameIndex: Int,
    val timestampMs: Long,
    val imageUrlPlaceholder: String, // Path or URL placeholder for the frame image
    val qualityScore: Float
)
