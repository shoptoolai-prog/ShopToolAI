package com.example.domain.engine

interface FashionDetectionEngine {
    suspend fun detectFashionItems(frames: List<ExtractedFrame>): List<DetectedFashionItem>
}

data class DetectedFashionItem(
    val id: String,
    val category: String, // e.g. Hoodie, Sunglasses, Sneaker, Pants
    val confidence: Float,
    val styleTags: List<String>, // e.g. "oversized", "streetwear", "minimalist", "linen"
    val mainColor: String,
    val estimatedGender: String, // Unisex, Mens, Womens
    val sourceFrameIndex: Int
)
