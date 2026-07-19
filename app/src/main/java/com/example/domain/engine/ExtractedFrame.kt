package com.example.domain.engine

data class ExtractedFrame(
    val imageBase64: String,
    val timestamp: Long = System.currentTimeMillis()
)
