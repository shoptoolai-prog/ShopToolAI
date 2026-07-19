package com.example.data.engine.mock

import android.util.Log
import com.example.domain.engine.InstagramReelAnalyzerEngine
import com.example.domain.engine.ReelMetadata
import kotlinx.coroutines.delay

class MockInstagramReelAnalyzerEngine : InstagramReelAnalyzerEngine {
    private val TAG = "MockReelAnalyzerEngine"

    override fun isValidReelUrl(url: String): Boolean {
        val trimmed = url.trim()
        val isInstagramLink = trimmed.contains("instagram.com/reel/", ignoreCase = true) || 
                              trimmed.contains("instagram.com/p/", ignoreCase = true) ||
                              trimmed.contains("instagram.com/share", ignoreCase = true) ||
                              trimmed.contains("instagram.com/", ignoreCase = true)
        return isInstagramLink || trimmed.startsWith("http")
    }

    override suspend fun analyzeReelMetadata(url: String): ReelMetadata {
        Log.d(TAG, "analyzeReelMetadata started for URL: $url")
        // Simulate network extraction delay
        delay(600)
        
        val shortcode = url.substringAfter("/reel/", "").substringBefore("/").ifEmpty { "Cx8Y7bA_abc" }
        val lowercaseUrl = url.lowercase()
        
        val (username, description) = when {
            lowercaseUrl.contains("tech") || lowercaseUrl.contains("setup") -> 
                "tech_haven" to "Ultimate 2026 Minimalist Desk Setup tour. Everything you need for pure focus and speed. ✨ #workspace #desksetup"
            lowercaseUrl.contains("home") || lowercaseUrl.contains("minimal") -> 
                "nordic_living" to "Exploring Wabi-Sabi textures in our newly updated cozy living corner. Let us know your favorite piece! ☕ #interiordesign #livingroom"
            lowercaseUrl.contains("fashion") || lowercaseUrl.contains("ootd") -> 
                "streetwear_daily" to "What I wore for summer styling today. Chill relaxed fits only. 👕👟 #ootd #summerstyle"
            else -> 
                "curated_life" to "Checking out some daily aesthetic lifestyle objects. Curated and reviewed. #vlog #aesthetic"
        }

        Log.d(TAG, "analyzeReelMetadata finished successfully: author=@$username")
        return ReelMetadata(
            url = url,
            shortcode = shortcode,
            authorUsername = username,
            description = description,
            viewCount = (10_000..500_000).random().toLong()
        )
    }
}
