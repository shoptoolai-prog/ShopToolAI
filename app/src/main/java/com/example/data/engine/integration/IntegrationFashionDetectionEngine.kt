package com.example.data.engine.integration

import com.example.domain.engine.DetectedFashionItem
import com.example.domain.engine.ExtractedFrame
import com.example.domain.engine.FashionDetectionEngine
import com.example.domain.provider.ProviderManager

class IntegrationFashionDetectionEngine(
    private val providerManager: ProviderManager
) : FashionDetectionEngine {

    override suspend fun detectFashionItems(frames: List<ExtractedFrame>): List<DetectedFashionItem> {
        val activeProvider = providerManager.getActiveVisionProvider()
        val rawObjects = activeProvider.analyzeFrames(frames)

        // Filters or marks relevant fashion/lifestyle elements
        return rawObjects.mapIndexed { idx, obj ->
            DetectedFashionItem(
                id = "fashion_" + obj.id,
                category = obj.category,
                confidence = obj.confidence,
                styleTags = obj.visualAttributes,
                mainColor = "Multicolor",
                estimatedGender = "Unisex",
                sourceFrameIndex = (idx % maxOf(frames.size, 1)) + 1
            )
        }
    }
}
