package com.example.data.engine.mock

import android.util.Log
import com.example.domain.engine.BoundingBox
import com.example.domain.engine.DetectedObjectVisual
import com.example.domain.engine.ExtractedFrame
import com.example.domain.engine.ObjectDetectionEngine
import kotlinx.coroutines.delay

class MockObjectDetectionEngine : ObjectDetectionEngine {
    private val TAG = "MockObjectDetection"

    override suspend fun detectObjects(frames: List<ExtractedFrame>): List<DetectedObjectVisual> {
        Log.d(TAG, "detectObjects started for ${frames.size} frames")
        delay(600)

        // Return mock objects
        val results = listOf(
            DetectedObjectVisual(
                objectId = "obj_01",
                label = "Keyboard",
                confidence = 0.97f,
                visualAttributes = listOf("mechanical", "75% layout", "RGB backlight"),
                boundingBox = BoundingBox(0.1f, 0.6f, 0.4f, 0.9f),
                sourceFrameIndex = 1
            ),
            DetectedObjectVisual(
                objectId = "obj_02",
                label = "Monitor LightBar",
                confidence = 0.93f,
                visualAttributes = listOf("asymmetrical beam", "matte black", "USB powered"),
                boundingBox = BoundingBox(0.3f, 0.1f, 0.7f, 0.3f),
                sourceFrameIndex = 2
            )
        )
        Log.d(TAG, "detectObjects completed. Detected ${results.size} objects.")
        return results
    }
}
