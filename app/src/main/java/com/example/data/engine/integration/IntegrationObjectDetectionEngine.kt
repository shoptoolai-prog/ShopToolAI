package com.example.data.engine.integration

import com.example.domain.engine.BoundingBox
import com.example.domain.engine.DetectedObjectVisual
import com.example.domain.engine.ExtractedFrame
import com.example.domain.engine.ObjectDetectionEngine
import com.example.domain.provider.ProviderManager

class IntegrationObjectDetectionEngine(
    private val providerManager: ProviderManager
) : ObjectDetectionEngine {

    override suspend fun detectObjects(frames: List<ExtractedFrame>): List<DetectedObjectVisual> {
        val activeProvider = providerManager.getActiveVisionProvider()
        val rawObjects = activeProvider.analyzeFrames(frames)

        return rawObjects.mapIndexed { idx, obj ->
            DetectedObjectVisual(
                objectId = obj.id,
                label = obj.label,
                confidence = obj.confidence,
                visualAttributes = obj.visualAttributes,
                boundingBox = BoundingBox(
                    xMin = obj.boundingBox?.xMin ?: 0f,
                    yMin = obj.boundingBox?.yMin ?: 0f,
                    xMax = obj.boundingBox?.xMax ?: 1f,
                    yMax = obj.boundingBox?.yMax ?: 1f
                ),
                sourceFrameIndex = (idx % maxOf(frames.size, 1)) + 1
            )
        }
    }
}
