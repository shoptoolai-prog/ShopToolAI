package com.example.domain.service

import com.example.domain.engine.ExtractedFrame
import kotlinx.coroutines.flow.Flow

interface ReelProcessingManager {
    /**
     * Validates and parses the given URL, then queues it for processing.
     * Emits state updates asynchronously, with full support for cancellation.
     */
    fun processReel(url: String): Flow<ReelProcessingState>

    /**
     * Retrieves the cached frames for a given reel URL if available.
     */
    fun getCachedFrames(url: String): List<ExtractedFrame>?

    /**
     * Clears all cached media files and frame extraction cache.
     */
    fun clearCache()
}

sealed interface ReelProcessingState {
    object Idle : ReelProcessingState
    object CheckingNetwork : ReelProcessingState
    object ValidatingUrl : ReelProcessingState
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : ReelProcessingState
    data class ExtractingFrames(val progress: Float, val count: Int) : ReelProcessingState
    data class Success(val url: String, val title: String, val frames: List<ExtractedFrame>) : ReelProcessingState
    data class Error(val message: String, val canRetry: Boolean = true) : ReelProcessingState
}
