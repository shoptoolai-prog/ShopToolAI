package com.example.domain.engine

interface InstagramReelAnalyzerEngine {
    fun isValidReelUrl(url: String): Boolean
    suspend fun analyzeReelMetadata(url: String): ReelMetadata
}

data class ReelMetadata(
    val url: String,
    val shortcode: String,
    val authorUsername: String,
    val description: String,
    val viewCount: Long
)
