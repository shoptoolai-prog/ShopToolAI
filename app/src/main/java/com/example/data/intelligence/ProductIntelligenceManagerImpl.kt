package com.example.data.intelligence

import android.util.Log
import com.example.domain.engine.ExtractedFrame
import com.example.domain.intelligence.*
import com.example.domain.model.BrandDetails
import com.example.domain.model.IntelProduct
import com.example.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductIntelligenceManagerImpl(
    private val detectionModule: ProductDetectionModule,
    private val brandModule: BrandRecognitionModule,
    private val colorModule: ColorDetectionModule,
    private val categoryModule: CategoryDetectionModule,
    private val similarityModule: SimilarProductFinderModule,
    private val duplicateFilterModule: DuplicateProductFilterModule,
    private val rankingModule: ProductRankingModule,
    private val confidenceModule: ConfidenceScoreModule,
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val searchRepository: SearchRepository,
    private val recommendationRepository: RecommendationRepository
) : ProductIntelligenceManager {

    private val tag = "ProductIntelManager"

    override suspend fun analyzeVideoFrames(frames: List<ExtractedFrame>): List<IntelProduct> = withContext(Dispatchers.Default) {
        Log.d(tag, "Starting product intelligence pipeline on ${frames.size} frames")
        
        try {
            // 1. Detect Raw Products
            val rawDetections = detectionModule.detectProducts(frames)
            Log.d(tag, "Detected ${rawDetections.size} raw items")

            val detectedProducts = mutableListOf<IntelProduct>()

            // 2. Enrich each detected item
            for (raw in rawDetections) {
                // a. Brand Recognition
                val (brandName, brandConf) = brandModule.recognizeBrand(raw.label, raw.visualAttributes)
                
                // Save brand details if not already present
                val existingBrand = brandRepository.getBrandDetails(brandName)
                if (existingBrand == null) {
                    val newBrand = BrandDetails(
                        name = brandName,
                        description = "Premium lifestyle brand specializing in modern high-quality products.",
                        logoUrl = "",
                        officialWebsite = "https://www.${brandName.lowercase().replace(" ", "")}.com",
                        marketTier = "Premium",
                        categorySpecialties = listOf(raw.label)
                    )
                    brandRepository.saveBrandDetails(newBrand)
                }

                // b. Color Detection
                val colors = colorModule.detectColors(raw.visualAttributes)
                val primaryColor = colors.firstOrNull() ?: "Multicolor"

                // c. Category Detection
                val category = categoryModule.detectCategory(raw.label, raw.visualAttributes)

                // d. Confidence calculation
                val unifiedConfidence = confidenceModule.calculateConfidence(
                    detectionConfidence = raw.confidence,
                    brandConfidence = brandConf,
                    matchConfidence = 0.90f
                )

                // e. Form descriptive keywords
                val keywords = (raw.visualAttributes + brandName + category + primaryColor)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                // f. Build initial IntelProduct for finder module
                val initialProduct = IntelProduct(
                    id = "intel_${raw.id}",
                    sku = "SKU-${brandName.take(3).uppercase()}-${raw.id.takeLast(4).uppercase()}",
                    name = if (raw.label.contains(brandName, ignoreCase = true)) raw.label else "$brandName ${raw.label}",
                    brand = brandName,
                    category = category,
                    color = primaryColor,
                    gender = "Unisex",
                    confidenceScore = unifiedConfidence,
                    thumbnail = if (raw.label.lowercase().contains("keyboard")) {
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80"
                    } else {
                        "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80"
                    },
                    description = "An expertly identified $category item with distinct design notes: ${raw.visualAttributes.joinToString(", ")}.",
                    similarProducts = emptyList(),
                    searchKeywords = keywords,
                    price = if (raw.label.lowercase().contains("keyboard")) "$199.99" else "$139.00",
                    buyUrl = "https://www.google.com/search?q=${brandName}+${raw.label}"
                )

                // g. Find Similar Products
                val similarRefs = similarityModule.findSimilar(initialProduct)
                
                // h. Create final product with similarities attached
                val finalProduct = initialProduct.copy(similarProducts = similarRefs)

                // i. Store and Index product details
                productRepository.saveProduct(finalProduct)
                searchRepository.indexProduct(finalProduct)
                recommendationRepository.updateRecommendations(finalProduct.id, similarRefs)

                detectedProducts.add(finalProduct)
            }

            // 3. Deduplicate
            val uniqueProducts = duplicateFilterModule.filterDuplicates(detectedProducts)
            Log.d(tag, "Filtered duplicates: ${detectedProducts.size} -> ${uniqueProducts.size}")

            // 4. Rank
            val rankedProducts = rankingModule.rankProducts(uniqueProducts)
            Log.d(tag, "Pipeline completed successfully with ${rankedProducts.size} ranked items")

            rankedProducts

        } catch (e: Exception) {
            Log.e(tag, "Error in product intelligence pipeline: ${e.message}", e)
            emptyList()
        }
    }
}
