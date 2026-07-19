package com.example.domain.service

import com.example.domain.engine.ExtractedFrame
import kotlinx.coroutines.flow.Flow

interface VideoFrameExtractionService {
    /**
     * Extracts high-quality keyframes from a local video file [localFilePath].
     * Uses optimized sampling (sampleRateHz) to capture only relevant visual transitions.
     */
    fun extractFrames(localFilePath: String, sampleRateHz: Float): Flow<ExtractionStatus>
}

sealed interface ExtractionStatus {
    object Idle : ExtractionStatus
    data class Progress(val percentage: Float, val framesExtractedSoFar: Int) : ExtractionStatus
    data class Success(val frames: List<ExtractedFrame>) : ExtractionStatus
    data class Error(val exception: Throwable) : ExtractionStatus
}
