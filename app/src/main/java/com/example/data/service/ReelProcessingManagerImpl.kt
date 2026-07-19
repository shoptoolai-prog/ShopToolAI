package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.data.engine.mock.MockInstagramReelAnalyzerEngine
import com.example.data.service.mock.MockVideoDownloadService
import com.example.data.service.mock.MockVideoFrameExtractionService
import com.example.data.util.NetworkUtils
import com.example.data.util.RetryUtils
import com.example.domain.engine.ExtractedFrame
import com.example.domain.engine.InstagramReelAnalyzerEngine
import com.example.domain.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class ReelProcessingManagerImpl(
    private val context: Context,
    private val reelAnalyzer: InstagramReelAnalyzerEngine = MockInstagramReelAnalyzerEngine(),
    private val downloadService: VideoDownloadService = MockVideoDownloadService(),
    private val frameExtractor: VideoFrameExtractionService = MockVideoFrameExtractionService()
) : ReelProcessingManager {

    private val TAG = "ReelProcessingManager"
    
    // Memory cache for frames to fulfill the "Cache extracted frames" requirement
    private val frameCache = ConcurrentHashMap<String, List<ExtractedFrame>>()

    override fun processReel(url: String): Flow<ReelProcessingState> = flow {
        val trimmedUrl = url.trim()
        Log.d(TAG, "processReel started for URL: $trimmedUrl")

        // 1. Check network connectivity
        emit(ReelProcessingState.CheckingNetwork)
        if (!NetworkUtils.isInternetAvailable(context)) {
            emit(ReelProcessingState.Error("No internet connection available.", canRetry = true))
            return@flow
        }

        // 2. Validate URL format
        emit(ReelProcessingState.ValidatingUrl)
        if (!reelAnalyzer.isValidReelUrl(trimmedUrl)) {
            emit(ReelProcessingState.Error("Invalid Instagram Reel URL structure.", canRetry = false))
            return@flow
        }

        // 3. Check local Cache
        val cachedFrames = getCachedFrames(trimmedUrl)
        if (cachedFrames != null) {
            Log.d(TAG, "Cache hit for URL: $trimmedUrl")
            emit(ReelProcessingState.Success(trimmedUrl, "Cached Reel Analysis", cachedFrames))
            return@flow
        }

        // 4. Download video file (using Flow download state progression)
        var downloadedPath = ""
        var downloadError: Throwable? = null

        try {
            // Apply retry and timeout around the download orchestrator
            RetryUtils.retryWithTimeout(
                timeoutMs = 15000L,
                maxRetries = 2,
                operationName = "DownloadReelVideo"
            ) {
                downloadService.downloadVideo(trimmedUrl).collect { status ->
                    when (status) {
                        is DownloadStatus.Progress -> {
                            emit(ReelProcessingState.Downloading(status.percentage, status.bytesDownloaded, status.totalBytes))
                        }
                        is DownloadStatus.Success -> {
                            downloadedPath = status.localFilePath
                        }
                        is DownloadStatus.Error -> {
                            downloadError = status.exception
                        }
                        else -> { /* idle or other */ }
                    }
                }
            }
        } catch (e: Exception) {
            emit(ReelProcessingState.Error("Failed to download Reel video: ${e.message}", canRetry = true))
            return@flow
        }

        if (downloadError != null) {
            emit(ReelProcessingState.Error("Video download failed: ${downloadError?.message}", canRetry = true))
            return@flow
        }

        if (downloadedPath.isEmpty()) {
            emit(ReelProcessingState.Error("Could not save video file.", canRetry = true))
            return@flow
        }

        // 5. Extract video frames with quality check
        var finalFrames: List<ExtractedFrame>? = null
        var extractionError: Throwable? = null

        try {
            RetryUtils.retryWithTimeout(
                timeoutMs = 15000L,
                maxRetries = 2,
                operationName = "ExtractVideoFrames"
            ) {
                frameExtractor.extractFrames(downloadedPath, 1.0f).collect { status ->
                    when (status) {
                        is ExtractionStatus.Progress -> {
                            emit(ReelProcessingState.ExtractingFrames(status.percentage, status.framesExtractedSoFar))
                        }
                        is ExtractionStatus.Success -> {
                            finalFrames = status.frames
                        }
                        is ExtractionStatus.Error -> {
                            extractionError = status.exception
                        }
                        else -> { /* idle or other */ }
                    }
                }
            }
        } catch (e: Exception) {
            emit(ReelProcessingState.Error("Failed to extract frames: ${e.message}", canRetry = true))
            return@flow
        }

        if (extractionError != null) {
            emit(ReelProcessingState.Error("Frame extraction failed: ${extractionError?.message}", canRetry = true))
            return@flow
        }

        val frames = finalFrames
        if (frames == null || frames.isEmpty()) {
            emit(ReelProcessingState.Error("No valid keyframes were extracted from video.", canRetry = true))
            return@flow
        }

        // Cache the extracted frames
        frameCache[trimmedUrl.lowercase()] = frames
        Log.d(TAG, "Cached ${frames.size} frames for $trimmedUrl")

        val title = when {
            trimmedUrl.contains("tech") || trimmedUrl.contains("setup") -> "Insane 2026 Minimalist Productivity Desk Setup"
            trimmedUrl.contains("home") || trimmedUrl.contains("minimal") -> "Wabi-Sabi Aesthetics: Modern Living Room Makeover"
            trimmedUrl.contains("fashion") || trimmedUrl.contains("ootd") -> "What I Wore This Week: Summer Streetwear Edition"
            else -> "Aesthetic Daily Routine & Curated Finds"
        }

        emit(ReelProcessingState.Success(trimmedUrl, title, frames))
    }.flowOn(Dispatchers.IO)

    override fun getCachedFrames(url: String): List<ExtractedFrame>? {
        return frameCache[url.trim().lowercase()]
    }

    override fun clearCache() {
        frameCache.clear()
        Log.d(TAG, "Cleared frames cache.")
    }
}
