package com.example.data.service.mock

import com.example.domain.engine.ExtractedFrame
import com.example.domain.service.ReelProcessingManager
import com.example.domain.service.ReelProcessingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockReelProcessingManager : ReelProcessingManager {
    override fun processReel(url: String): Flow<ReelProcessingState> = flow {
        emit(ReelProcessingState.CheckingNetwork)
        delay(300)
        emit(ReelProcessingState.ValidatingUrl)
        delay(300)
        
        val totalBytes = 5_000_000L
        for (i in 1..5) {
            val progress = i * 0.2f
            emit(ReelProcessingState.Downloading(progress, (totalBytes * progress).toLong(), totalBytes))
            delay(150)
        }
        
        for (i in 1..3) {
            emit(ReelProcessingState.ExtractingFrames(i * 0.33f, i))
            delay(150)
        }

        val frames = listOf(
            ExtractedFrame(1, 1000L, "frame_1", 0.90f),
            ExtractedFrame(2, 2000L, "frame_2", 0.95f)
        )
        emit(ReelProcessingState.Success(url, "Mock Reel Video", frames))
    }

    override fun getCachedFrames(url: String): List<ExtractedFrame>? = null

    override fun clearCache() {}
}
