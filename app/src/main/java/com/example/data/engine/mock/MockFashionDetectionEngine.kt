package com.example.data.engine.mock

import android.util.Log
import com.example.domain.engine.DetectedFashionItem
import com.example.domain.engine.ExtractedFrame
import com.example.domain.engine.FashionDetectionEngine
import kotlinx.coroutines.delay

class MockFashionDetectionEngine : FashionDetectionEngine {
    private val TAG = "MockFashionDetection"

    override suspend fun detectFashionItems(frames: List<ExtractedFrame>): List<DetectedFashionItem> {
        Log.d(TAG, "detectFashionItems started for ${frames.size} frames")
        delay(600)

        val results = listOf(
            DetectedFashionItem(
                id = "fash_01",
                category = "Sneakers",
                confidence = 0.98f,
                styleTags = listOf("retro low-top", "cream leather", "vintage sole"),
                mainColor = "Cream/White",
                estimatedGender = "Unisex",
                sourceFrameIndex = 1
            ),
            DetectedFashionItem(
                id = "fash_02",
                category = "Sunglasses",
                confidence = 0.92f,
                styleTags = listOf("tortoiseshell", "chunky acetate", "polarized"),
                mainColor = "Amber Brown",
                estimatedGender = "Unisex",
                sourceFrameIndex = 3
            )
        )
        Log.d(TAG, "detectFashionItems completed. Detected ${results.size} apparel elements.")
        return results
    }
}
