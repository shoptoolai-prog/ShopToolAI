package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.decision.*
import com.example.data.local.AppDatabase
import com.example.domain.model.ShoppingProduct
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiDecisionEngineTest {

    private lateinit var database: AppDatabase
    private lateinit var confidenceEngine: ConfidenceScoreEngineImpl
    private lateinit var similarityEngine: ProductSimilarityEngineImpl
    private lateinit var rankingEngine: ProductRankingEngineImpl
    private lateinit var duplicateEngine: DuplicateRemovalEngineImpl
    private lateinit var bestMatchEngine: BestMatchEngineImpl
    private lateinit var budgetEngine: BudgetRecommendationEngineImpl
    private lateinit var premiumEngine: PremiumRecommendationEngineImpl
    private lateinit var trendingEngine: TrendingProductEngineImpl
    private lateinit var smartEngine: SmartRecommendationEngineImpl
    private lateinit var decisionManager: AiDecisionManagerImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        confidenceEngine = ConfidenceScoreEngineImpl()
        similarityEngine = ProductSimilarityEngineImpl()
        rankingEngine = ProductRankingEngineImpl()
        duplicateEngine = DuplicateRemovalEngineImpl()
        bestMatchEngine = BestMatchEngineImpl()
        budgetEngine = BudgetRecommendationEngineImpl()
        premiumEngine = PremiumRecommendationEngineImpl()
        trendingEngine = TrendingProductEngineImpl()
        smartEngine = SmartRecommendationEngineImpl()

        decisionManager = AiDecisionManagerImpl(
            context = context,
            productDao = database.shoppingProductDao(),
            rankingEngine = rankingEngine,
            similarityEngine = similarityEngine,
            confidenceEngine = confidenceEngine,
            duplicateEngine = duplicateEngine,
            bestMatchEngine = bestMatchEngine,
            budgetEngine = budgetEngine,
            premiumEngine = premiumEngine,
            trendingEngine = trendingEngine,
            smartEngine = smartEngine
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testConfidenceScoreCalculation() = runBlocking {
        val confidence = confidenceEngine.calculateConfidence(
            detectionConfidence = 0.9f,
            brandConfidence = 0.8f,
            sellerRating = 4.5f,
            ocrConfidence = 0.95f
        )
        // Ensure within range and calculated correctly according to our weights
        assertTrue(confidence in 0.0f..1.0f)
        val expected = (0.9f * 0.35f) + (0.8f * 0.30f) + (0.95f * 0.20f) + ((4.5f / 5.0f) * 0.15f)
        assertEquals(expected, confidence, 0.001f)
    }

    @Test
    fun testProductSimilarityEngine() = runBlocking {
        val candidate = ShoppingProduct(
            id = "1",
            name = "Apex Runner Shoes Red",
            brand = "Apex",
            category = "Shoes",
            color = "Red",
            images = emptyList(),
            price = 99.99,
            discount = 0.0,
            currency = "USD",
            storeName = "Apex Store",
            productUrl = "url",
            rating = 4.5f,
            reviewCount = 50,
            aiConfidence = 0.9f,
            aiRecommendationScore = 0.8f,
            deliveryTime = "2 days",
            stockStatus = "In Stock"
        )

        val queryAttrs = listOf("Apex", "Shoes", "Red")
        val similarity = similarityEngine.calculateSimilarity(queryAttrs, candidate)
        // High similarity expected since brand, category, color and name contain keywords
        assertTrue(similarity > 0.8f)
    }

    @Test
    fun testDuplicateRemovalEngine() = runBlocking {
        val prodA = ShoppingProduct(
            id = "A",
            name = "Apex Runner Shoes Red",
            brand = "Apex",
            category = "Shoes",
            color = "Red",
            images = emptyList(),
            price = 99.99,
            discount = 0.0,
            currency = "USD",
            storeName = "Apex Store",
            productUrl = "url",
            rating = 4.8f,
            reviewCount = 50,
            aiConfidence = 0.9f,
            aiRecommendationScore = 0.8f,
            deliveryTime = "2 days",
            stockStatus = "In Stock"
        )

        val prodB = prodA.copy(
            id = "B",
            name = "Apex Runner Shoes Red",
            price = 101.99, // slightly worse price
            rating = 4.5f   // slightly worse rating
        )

        val devA = rankingEngine.calculateScores(prodA, 1.0f, 0.9f)
        val devB = rankingEngine.calculateScores(prodB, 1.0f, 0.8f)

        val uniqueList = duplicateEngine.removeDuplicates(listOf(devA, devB))
        // Since they are near duplicates, the engine should keep devA which has the higher finalRankingScore
        assertEquals(1, uniqueList.size)
        assertEquals("A", uniqueList.first().product.id)
    }

    @Test
    fun testBudgetAndPremiumEngines() = runBlocking {
        val cheapProduct = ShoppingProduct(
            id = "budget1",
            name = "EcoFit Casual Shoes",
            brand = "EcoFit",
            category = "Shoes",
            color = "Grey",
            images = emptyList(),
            price = 39.99,
            discount = 5.0,
            currency = "USD",
            storeName = "Eco Store",
            productUrl = "url",
            rating = 4.3f,
            reviewCount = 120,
            aiConfidence = 0.8f,
            aiRecommendationScore = 0.7f,
            deliveryTime = "3 days",
            stockStatus = "In Stock"
        )

        val premiumProduct = ShoppingProduct(
            id = "premium1",
            name = "LuxSport Elite Runner",
            brand = "LuxSport",
            category = "Shoes",
            color = "Gold",
            images = emptyList(),
            price = 299.99,
            discount = 10.0,
            currency = "USD",
            storeName = "Lux Store",
            productUrl = "url",
            rating = 4.9f,
            reviewCount = 80,
            aiConfidence = 0.95f,
            aiRecommendationScore = 0.95f,
            deliveryTime = "1 day",
            stockStatus = "In Stock"
        )

        val devCheap = rankingEngine.calculateScores(cheapProduct, 0.8f, 0.8f)
        val devPremium = rankingEngine.calculateScores(premiumProduct, 0.9f, 0.95f)

        val candidates = listOf(devCheap, devPremium)

        // Budget Engine check with maxPrice 100.0
        val budgetOptions = budgetEngine.recommendBudgetOptions(candidates, 100.0)
        assertEquals(1, budgetOptions.size)
        assertEquals("budget1", budgetOptions.first().product.id)

        // Premium Engine check
        val premiumOptions = premiumEngine.recommendPremiumOptions(candidates)
        assertEquals(1, premiumOptions.size)
        assertEquals("premium1", premiumOptions.first().product.id)
    }

    @Test
    fun testSmartRecommendationEngine() = runBlocking {
        val prodA = ShoppingProduct(
            id = "A",
            name = "Apex Casual",
            brand = "Apex",
            category = "Shoes",
            color = "Black",
            images = emptyList(),
            price = 120.0,
            discount = 0.0,
            currency = "USD",
            storeName = "Apex Store",
            productUrl = "url",
            rating = 4.5f,
            reviewCount = 100,
            aiConfidence = 0.8f,
            aiRecommendationScore = 0.8f,
            deliveryTime = "2 days",
            stockStatus = "In Stock"
        )

        val prodB = ShoppingProduct(
            id = "B",
            name = "EcoFit Runner",
            brand = "EcoFit",
            category = "Shoes",
            color = "Blue",
            images = emptyList(),
            price = 80.0,
            discount = 0.0,
            currency = "USD",
            storeName = "Eco Store",
            productUrl = "url",
            rating = 4.5f,
            reviewCount = 100,
            aiConfidence = 0.8f,
            aiRecommendationScore = 0.8f,
            deliveryTime = "2 days",
            stockStatus = "In Stock"
        )

        val devA = rankingEngine.calculateScores(prodA, 0.8f, 0.8f)
        val devB = rankingEngine.calculateScores(prodB, 0.8f, 0.8f)

        // Preferred Brand is Apex
        val smartRecsBrand = smartEngine.generateSmartRecommendations(listOf(devA, devB), preferredBrand = "Apex")
        assertEquals("A", smartRecsBrand.first().product.id)

        // User Budget is 90.0 (prodB is under budget, prodA exceeds)
        val smartRecsBudget = smartEngine.generateSmartRecommendations(listOf(devA, devB), userBudget = 90.0)
        assertEquals("B", smartRecsBudget.first().product.id)
    }

    @Test
    fun testDecisionManagerEndToEndFlow() = runBlocking {
        val prodA = ShoppingProduct(
            id = "A",
            name = "Apex Runner Shoes Red",
            brand = "Apex",
            category = "Shoes",
            color = "Red",
            images = emptyList(),
            price = 99.99,
            discount = 0.0,
            currency = "USD",
            storeName = "Apex Store",
            productUrl = "url",
            rating = 4.8f,
            reviewCount = 50,
            aiConfidence = 0.9f,
            aiRecommendationScore = 0.8f,
            deliveryTime = "2 days",
            stockStatus = "In Stock"
        )

        val result = decisionManager.processAndDecisionQuery(
            detectedQuery = "Apex Shoes",
            queryAttributes = listOf("Apex", "Shoes", "Red"),
            candidates = listOf(prodA)
        )

        assertNotNull(result.bestMatch)
        assertEquals("A", result.bestMatch?.product?.id)
        assertEquals(1, result.rankedProducts.size)
        
        // Also verify persistence in SQLite
        val saved = database.shoppingProductDao().getProductById("A")
        assertNotNull(saved)
        assertEquals("Apex Runner Shoes Red", saved?.name)
    }
}
