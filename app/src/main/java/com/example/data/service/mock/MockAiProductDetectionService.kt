package com.example.data.service.mock

import com.example.domain.service.AiProductDetectionService
import com.example.domain.service.DetectedObject
import com.example.domain.service.ProcessedReel
import kotlinx.coroutines.delay

class MockAiProductDetectionService : AiProductDetectionService {
    override suspend fun detectProductsInVideo(reel: ProcessedReel): List<DetectedObject> {
        // Simulate video processing/detection delay
        delay(900)
        
        val titleLower = reel.title.lowercase()
        return when {
            titleLower.contains("desk") || titleLower.contains("setup") || titleLower.contains("productivity") -> listOf(
                DetectedObject("Keyboard", listOf("mechanical", "rgb backlight", "75% layout", "grey keycaps"), 0.98f),
                DetectedObject("Monitor Light", listOf("asymmetrical light bar", "usb-c", "matte black"), 0.94f),
                DetectedObject("Desk Mat", listOf("felt wool mat", "medium grey", "minimalist"), 0.89f)
            )
            titleLower.contains("living") || titleLower.contains("home") || titleLower.contains("wabi-sabi") -> listOf(
                DetectedObject("Diffuser", listOf("ceramic diffuser", "warm ambient glow", "white clay"), 0.96f),
                DetectedObject("Lamp", listOf("pleated paper lamp", "wooden base", "soft yellow light"), 0.92f),
                DetectedObject("Tray", listOf("concrete vanity tray", "oval shape", "brushed gold trim"), 0.85f)
            )
            titleLower.contains("fashion") || titleLower.contains("ootd") || titleLower.contains("streetwear") -> listOf(
                DetectedObject("Sneakers", listOf("retro low-top", "cream leather", "vintage sole"), 0.97f),
                DetectedObject("Sunglasses", listOf("tortoiseshell frame", "dark lenses", "chunky acetate"), 0.91f),
                DetectedObject("Overshirt", listOf("linen overshirt", "sand color", "relaxed fit"), 0.88f)
            )
            else -> listOf(
                DetectedObject("Smart Mug", listOf("temperature control", "matte black", "charging coaster"), 0.95f),
                DetectedObject("Journal", listOf("leather notebook", "dotted paper", "embossed spine"), 0.87f)
            )
        }
    }
}
