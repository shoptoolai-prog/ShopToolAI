package com.example.domain.intelligence

import com.example.domain.engine.ExtractedFrame
import com.example.domain.model.IntelProduct

interface ProductIntelligenceManager {
    suspend fun analyzeVideoFrames(frames: List<ExtractedFrame>): List<IntelProduct>
}
