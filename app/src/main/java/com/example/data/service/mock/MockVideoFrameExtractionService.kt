package com.example.data.service.mock

import com.example.domain.engine.ExtractedFrame
import com.example.domain.service.ExtractionStatus
import com.example.domain.service.VideoFrameExtractionService
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class MockVideoFrameExtractionService : VideoFrameExtractionService {
    override fun extractFrames(localFilePath: String, sampleRateHz: Float): Flow<ExtractionStatus> = flow {
        emit(ExtractionStatus.Idle)
        delay(300)

        val totalFramesToExtract = 3
        val framesList = mutableListOf<ExtractedFrame>()

        for (i in 1..totalFramesToExtract) {
            if (!currentCoroutineContext().isActive) {
                emit(ExtractionStatus.Error(InterruptedException("Frame extraction cancelled.")))
                return@flow
            }
            delay(200) // simulate visual processing workload
            
            val frame = ExtractedFrame(
                frameIndex = i,
                timestampMs = i * 3000L,
                imageUrlPlaceholder = "frame_${i}_placeholder",
                qualityScore = 0.85f + (i * 0.04f)
            )
            framesList.add(frame)
            
            val progress = i.toFloat() / totalFramesToExtract
            emit(ExtractionStatus.Progress(progress, i))
        }

        emit(ExtractionStatus.Success(framesList))
    }
}
