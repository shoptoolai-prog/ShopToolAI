package com.example.domain.service

interface ReelProcessingService {
    suspend fun extractReelData(url: String): ProcessedReel
}

data class ProcessedReel(
    val url: String,
    val title: String,
    val durationSeconds: Int,
    val frameCount: Int
)
