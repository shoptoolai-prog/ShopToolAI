package com.example.data.repository

import com.example.data.local.HistoryDao
import com.example.data.local.HistoryEntity
import com.example.data.model.PriceSource
import com.example.data.model.Product
import com.example.domain.service.*
import com.example.data.service.mock.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val historyDao: HistoryDao,
    val reelProcessingService: ReelProcessingService = MockReelProcessingService(),
    val aiProductDetectionService: AiProductDetectionService = MockAiProductDetectionService(),
    val productSearchService: ProductSearchService = MockProductSearchService(),
    val priceComparisonService: PriceComparisonService = MockPriceComparisonService(),
    val reviewAnalysisService: ReviewAnalysisService = MockReviewAnalysisService(),
    val reelAnalyzerEngine: com.example.domain.engine.InstagramReelAnalyzerEngine = com.example.data.engine.mock.MockInstagramReelAnalyzerEngine(),
    val frameExtractorEngine: com.example.domain.engine.VideoFrameExtractorEngine = com.example.data.engine.mock.MockVideoFrameExtractorEngine(),
    val objectDetectionEngine: com.example.domain.engine.ObjectDetectionEngine = com.example.data.engine.mock.MockObjectDetectionEngine(),
    val fashionDetectionEngine: com.example.domain.engine.FashionDetectionEngine = com.example.data.engine.mock.MockFashionDetectionEngine(),
    val productMatchingEngine: com.example.domain.engine.ProductMatchingEngine = com.example.data.engine.mock.MockProductMatchingEngine(),
    val priceComparisonEngine: com.example.domain.engine.PriceComparisonEngine = com.example.data.engine.mock.MockPriceComparisonEngine(),
    val reviewAnalysisEngine: com.example.domain.engine.ReviewAnalysisEngine = com.example.data.engine.mock.MockReviewAnalysisEngine(),
    val similarProductRecommendationEngine: com.example.domain.engine.SimilarProductRecommendationEngine = com.example.data.engine.mock.MockSimilarProductRecommendationEngine(),
    val reelProcessingManager: ReelProcessingManager = com.example.data.service.mock.MockReelProcessingManager(),
    val productIntelligenceManager: com.example.domain.intelligence.ProductIntelligenceManager? = null
) {

    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun insertHistory(url: String, products: List<Product>): Long {
        val entity = HistoryEntity(url = url, products = products)
        return historyDao.insertHistory(entity)
    }

    suspend fun deleteHistory(id: Int) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    // Helper to generate hyper-realistic, beautiful products based on a URL
    suspend fun analyzeReelMock(url: String): List<Product> {
        // Simulate deep analysis times
        delay(3500) 

        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains("key") || lowerUrl.contains("desk") -> getKeyboardSetupProducts()
            lowerUrl.contains("wear") || lowerUrl.contains("shoe") || lowerUrl.contains("fit") -> getFashionProducts()
            lowerUrl.contains("tech") || lowerUrl.contains("phone") || lowerUrl.contains("gadget") -> getTechProducts()
            else -> getGeneralTrendingProducts()
        }
    }

    private fun getKeyboardSetupProducts() = listOf(
        Product(
            id = "kb-1",
            name = "AeroBoard Pro 75% Mechanical Keyboard",
            brand = "Vortex Labs",
            imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 98,
            lowestPrice = "$169.00",
            priceComparison = listOf(
                PriceSource("Vortex Direct", "$169.00", isLowest = true),
                PriceSource("DeskSpace Co", "$179.99"),
                PriceSource("Global Tech", "$185.00")
            ),
            positiveReviewSummary = "Stunning Gateron Oil King linear switches, exceptionally solid aluminum CNC chassis, gorgeous double-shot PBT keycaps.",
            negativeReviewSummary = "Requires companion software to configure RGB lighting custom macros, on the heavier side.",
            buyUrl = "https://example.com/buy/aeroboard"
        ),
        Product(
            id = "kb-2",
            name = "Minimalist Merino Wool Desk Mat (Slate)",
            brand = "NordicDesk",
            imageUrl = "https://images.unsplash.com/photo-1632292224971-0d45778b361e?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 94,
            lowestPrice = "$45.00",
            priceComparison = listOf(
                PriceSource("NordicDesk", "$45.00", isLowest = true),
                PriceSource("Amazon", "$49.99"),
                PriceSource("Etsy Premium", "$52.00")
            ),
            positiveReviewSummary = "Extremely premium thick felt weave. Prevents keyboard slipping and dampens acoustic hollow resonance.",
            negativeReviewSummary = "Slightly scratchy for direct forearm resting. Needs careful hand washing if coffee spills.",
            buyUrl = "https://example.com/buy/deskmat"
        )
    )

    private fun getFashionProducts() = listOf(
        Product(
            id = "fs-1",
            name = "Retro Runner V2 Suede Sneakers",
            brand = "Aether Footwear",
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 97,
            lowestPrice = "$120.00",
            priceComparison = listOf(
                PriceSource("Aether Shop", "$120.00", isLowest = true),
                PriceSource("Nordstrom", "$135.00"),
                PriceSource("Zappos Prime", "$140.00")
            ),
            positiveReviewSummary = "Ultra-comfortable Ortholite insoles, beautiful vintage color palette with premium Italian hairy suede overlays.",
            negativeReviewSummary = "Slightly narrow toe box. White midsoles stain easily and require protective water-repellent spray.",
            buyUrl = "https://example.com/buy/sneakers"
        ),
        Product(
            id = "fs-2",
            name = "Heavyweight Oversized Loopback Hoodie",
            brand = "Blank Canvas",
            imageUrl = "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 92,
            lowestPrice = "$85.00",
            priceComparison = listOf(
                PriceSource("Blank Canvas", "$85.00", isLowest = true),
                PriceSource("Farfetch", "$95.00")
            ),
            positiveReviewSummary = "Incredible 500GSM double-fleece cotton weight. Holds a perfect structured architectural box drape.",
            negativeReviewSummary = "Thick hood makes wearing tight jackets over it a bit restrictive. Hand drying recommended.",
            buyUrl = "https://example.com/buy/hoodie"
        )
    )

    private fun getTechProducts() = listOf(
        Product(
            id = "tc-1",
            name = "AeroFocus Active ANC Headphones",
            brand = "Sonic Labs",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 99,
            lowestPrice = "$299.00",
            priceComparison = listOf(
                PriceSource("Sonic Labs Store", "$299.00", isLowest = true),
                PriceSource("BestBuy", "$329.00"),
                PriceSource("B&H Photo", "$349.99")
            ),
            positiveReviewSummary = "Industry-defining noise-canceling, ultra-soft memory foam cups, rich cinematic custom equalizer curves.",
            negativeReviewSummary = "Included protective hardshell case is bulky. Battery life drops slightly with high-bitrate LDAC mode.",
            buyUrl = "https://example.com/buy/headphones"
        ),
        Product(
            id = "tc-2",
            name = "Nomad MagSafe Carbon Fiber Charger",
            brand = "Nomad Tech",
            imageUrl = "https://images.unsplash.com/photo-1622445262465-2481c4574875?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 95,
            lowestPrice = "$59.00",
            priceComparison = listOf(
                PriceSource("Nomad Tech", "$59.00", isLowest = true),
                PriceSource("Target Tech", "$64.99")
            ),
            positiveReviewSummary = "Sleek aerospace-grade dry weave carbon fiber. High magnetic snap latch strength, supports fast 15W Qi2.",
            negativeReviewSummary = "Braided cable is somewhat rigid. Does not include the required 20W USB-C wall adapter brick.",
            buyUrl = "https://example.com/buy/charger"
        )
    )

    private fun getGeneralTrendingProducts() = listOf(
        Product(
            id = "gp-1",
            name = "Chronos Minimalist Titanium Pilot Watch",
            brand = "Chronos Design",
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 96,
            lowestPrice = "$350.00",
            priceComparison = listOf(
                PriceSource("Chronos Direct", "$350.00", isLowest = true),
                PriceSource("Huckberry", "$375.00"),
                PriceSource("WatchFinder", "$399.00")
            ),
            positiveReviewSummary = "Extremely lightweight Grade 5 titanium case, scratch-resistant double-domed sapphire crystal, sweeping Seiko hybrid movement.",
            negativeReviewSummary = "Super-LumiNova luminescence could be brighter in pitch darkness. Sizing the micro-adjustments requires tools.",
            buyUrl = "https://example.com/buy/watch"
        ),
        Product(
            id = "gp-2",
            name = "Polar Matte Double-Wall Insulated Flask",
            brand = "HydroFlow",
            imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500&auto=format&fit=crop&q=80",
            matchPercentage = 91,
            lowestPrice = "$32.00",
            priceComparison = listOf(
                PriceSource("HydroFlow Co", "$32.00", isLowest = true),
                PriceSource("REI Outlet", "$36.00"),
                PriceSource("Amazon", "$38.50")
            ),
            positiveReviewSummary = "Keeps water ice-cold for full 36 hours. Beautiful powder-coat matte surface resists scuffs perfectly.",
            negativeReviewSummary = "Heavier than standard plastic bottles. Wide mouth can spill if drinking while walking quickly.",
            buyUrl = "https://example.com/buy/flask"
        )
    )
}
