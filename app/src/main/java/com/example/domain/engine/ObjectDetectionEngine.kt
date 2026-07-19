package com.example.domain.engine

interface ObjectDetectionEngine {
    suspend fun detectObjects(frames: List<ExtractedFrame>): List<DetectedObjectVisual>
}

data class DetectedObjectVisual(
    val objectId: String,
    val label: String,
    val confidence: Float,
    val visualAttributes: List<String>,
    val boundingBox: BoundingBox,
    val sourceFrameIndex: Int
)

data class BoundingBox(
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float
)
