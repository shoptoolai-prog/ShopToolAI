package com.example.data.engine.mock

import android.util.Log
import com.example.domain.engine.ExtractedFrame
import com.example.domain.engine.VideoFrameExtractorEngine
import kotlinx.coroutines.delay

class MockVideoFrameExtractorEngine : VideoFrameExtractorEngine {
    private val TAG = "MockFrameExtractor"

    override suspend fun extractKeyframes(videoUrl: String, sampleRateHz: Float): List<ExtractedFrame> {
        Log.d(TAG, "extractKeyframes started. URL: $videoUrl, rate: $sampleRateHz Hz")
        // Simulate frame extraction process
        delay(700)
        
        val frames = listOf(
            ExtractedFrame(1, 1200L, "frame_01_placeholder", 0.95f),
            ExtractedFrame(2, 4500L, "frame_02_placeholder", 0.88f),
            ExtractedFrame(3, 8900L, "frame_03_placeholder", 0.91f)
        )
        Log.d(TAG, "extractKeyframes finished. Extracted ${frames.size} high-quality keyframes.")
        return frames
    }
}
