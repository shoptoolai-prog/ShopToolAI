package com.example.data.engine.mock

import android.util.Log
import com.example.domain.engine.EngineReviewSummary
import com.example.domain.engine.ReviewAnalysisEngine
import kotlinx.coroutines.delay

class MockReviewAnalysisEngine : ReviewAnalysisEngine {
    private val TAG = "MockReviewAnalysis"

    override suspend fun analyzeSentiment(productId: String, productName: String): EngineReviewSummary {
        Log.d(TAG, "analyzeSentiment started for product: $productName")
        delay(600)

        val lowerId = productId.lowercase()
        val summary = when {
            lowerId.contains("keyboard") -> EngineReviewSummary(
                score = 0.92f,
                prosSummary = "Superb metal body weight, incredibly smooth tactile keys, seamless Bluetooth device connection switches.",
                consSummary = "Oem ABS keycaps catch oil quickly, full RGB backlight drains battery in a few days.",
                sampleSize = 485
            )
            lowerId.contains("monitor") || lowerId.contains("light") -> EngineReviewSummary(
                score = 0.89f,
                prosSummary = "Perfect glare-free light bar, automated brightness adjustment works flawlessly to reduce eye strain.",
                consSummary = "The desk wireless control puck goes through AAA batteries faster than expected.",
                sampleSize = 310
            )
            lowerId.contains("mat") -> EngineReviewSummary(
                score = 0.87f,
                prosSummary = "Plush warm merino wool feels very premium; stabilizes desktop audio resonant feedback nicely.",
                consSummary = "A bit itchy on bare forearms, naturally pills over months and requires careful lint-shaving.",
                sampleSize = 150
            )
            lowerId.contains("sneakers") -> EngineReviewSummary(
                score = 0.94f,
                prosSummary = "Ultra classic vintage look. Offers excellent supportive footbed and leather stitching is immaculate.",
                consSummary = "Feels a bit heavier than standard athletic footwear; requires a couple of days to fully break-in.",
                sampleSize = 640
            )
            lowerId.contains("sunglasses") -> EngineReviewSummary(
                score = 0.86f,
                prosSummary = "Extremely light frame weight, bio-acetate construction. Highly polarized lenses offer brilliant vision.",
                consSummary = "Temple hinge pivots feel tight initially; the included case is soft instead of a hard shell.",
                sampleSize = 180
            )
            else -> EngineReviewSummary(
                score = 0.90f,
                prosSummary = "Sturdy durable premium build materials, aesthetic minimalist presentation, simple to assemble.",
                consSummary = "Costs more than generic alternatives on standard wholesale marketplaces.",
                sampleSize = 250
            )
        }
        
        Log.d(TAG, "analyzeSentiment finished. Score: ${summary.score}")
        return summary
    }
}
