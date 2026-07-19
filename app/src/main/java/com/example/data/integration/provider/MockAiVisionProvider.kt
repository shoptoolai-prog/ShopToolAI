package com.example.data.integration.provider

import com.example.domain.engine.ExtractedFrame
import com.example.domain.provider.AiVisionProvider
import com.example.domain.provider.VisionBoundingBox
import com.example.domain.provider.VisionObject

class MockAiVisionProvider : AiVisionProvider {
    override fun getProviderName(): String = "Mock-Vision-v1"

    override suspend fun analyzeFrames(frames: List<ExtractedFrame>): List<VisionObject> {
        return listOf(
            VisionObject(
                id = "vis_01",
                label = "Mechanical Keyboard",
                confidence = 0.96f,
                visualAttributes = listOf("mechanical", "75% layout", "RGB backlight"),
                category = "Tech",
                boundingBox = VisionBoundingBox(0.1f, 0.5f, 0.4f, 0.9f)
            ),
            VisionObject(
                id = "vis_02",
                label = "Monitor Light Bar",
                confidence = 0.91f,
                visualAttributes = listOf("asymmetrical beam", "matte black", "USB-C"),
                category = "Tech",
                boundingBox = VisionBoundingBox(0.3f, 0.1f, 0.7f, 0.3f)
            )
        )
    }
}
