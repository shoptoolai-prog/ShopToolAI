package com.example.data.service.mock

import com.example.domain.service.ProcessedReel
import com.example.domain.service.ReelProcessingService
import kotlinx.coroutines.delay

class MockReelProcessingService : ReelProcessingService {
    override suspend fun extractReelData(url: String): ProcessedReel {
        // Simulate extraction delay
        delay(800)
        
        val lowercaseUrl = url.lowercase()
        val title = when {
            lowercaseUrl.contains("tech") || lowercaseUrl.contains("setup") -> "Insane 2026 Minimalist Productivity Desk Setup"
            lowercaseUrl.contains("home") || lowercaseUrl.contains("minimal") -> "Wabi-Sabi Aesthetics: Modern Living Room Makeover"
            lowercaseUrl.contains("fashion") || lowercaseUrl.contains("ootd") -> "What I Wore This Week: Summer Streetwear Edition"
            else -> "Aesthetic Daily Routine & Curated Finds"
        }
        
        return ProcessedReel(
            url = url,
            title = title,
            durationSeconds = 15,
            frameCount = 450
        )
    }
}
