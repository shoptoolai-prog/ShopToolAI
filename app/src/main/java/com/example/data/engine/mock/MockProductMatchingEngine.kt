package com.example.data.engine.mock

import android.util.Log
import com.example.data.model.Product
import com.example.domain.engine.*
import kotlinx.coroutines.delay

class MockProductMatchingEngine : ProductMatchingEngine {
    private val TAG = "MockProductMatching"

    override suspend fun findMatches(
        objects: List<DetectedObjectVisual>,
        fashionItems: List<DetectedFashionItem>
    ): List<ProductMatchResult> {
        Log.d(TAG, "findMatches started. Received ${objects.size} objects & ${fashionItems.size} fashion items.")
        delay(700)

        // We will generate the products matching the visual clues.
        // We'll inspect attributes to make it smart.
        // If there's a keyboard or monitor light detected, we match Workspace products.
        // If there's sneakers or sunglasses, we match Streetwear/Fashion.
        // We can also have standard home items.
        
        val matchedList = mutableListOf<ProductMatchResult>()

        val hasTech = objects.any { it.label == "Keyboard" || it.label == "Monitor LightBar" }
        val hasFashion = fashionItems.any { it.category == "Sneakers" || it.category == "Sunglasses" }

        if (hasTech) {
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_keyboard_1",
                        name = "Keychron Q1 Pro QMK/VIA Wireless Keyboard",
                        brand = "Keychron",
                        imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 98,
                        lowestPrice = "$199.99",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=Keychron+Q1+Pro"
                    ),
                    matchConfidence = 0.98f,
                    visualAttributesUsed = listOf("mechanical", "75% layout", "RGB backlight"),
                    matchingAlgorithm = "FeatureVectorSim v4.2"
                )
            )
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_monitor_light_1",
                        name = "BenQ ScreenBar Halo LED Monitor Light",
                        brand = "BenQ",
                        imageUrl = "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 94,
                        lowestPrice = "$139.00",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=BenQ+ScreenBar+Halo"
                    ),
                    matchConfidence = 0.94f,
                    visualAttributesUsed = listOf("asymmetrical beam", "matte black"),
                    matchingAlgorithm = "BBoxSegmentAlign v1.9"
                )
            )
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_desk_mat_1",
                        name = "Grovemade Wool Felt Desk Pad (Medium)",
                        brand = "Grovemade",
                        imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 89,
                        lowestPrice = "$80.00",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=Grovemade+Wool+Felt+Desk+Pad"
                    ),
                    matchConfidence = 0.89f,
                    visualAttributesUsed = listOf("felt wool mat", "minimalist"),
                    matchingAlgorithm = "GlobalTextureMatch v1.1"
                )
            )
        } else if (hasFashion) {
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_sneakers_1",
                        name = "New Balance 550 Vintage White Leather",
                        brand = "New Balance",
                        imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 97,
                        lowestPrice = "$110.00",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=New+Balance+550"
                    ),
                    matchConfidence = 0.97f,
                    visualAttributesUsed = listOf("retro low-top", "cream leather"),
                    matchingAlgorithm = "FashionFeatureSim v5.0"
                )
            )
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_sunglasses_1",
                        name = "Meller Bio-Based Chunky Sunglasses",
                        brand = "Meller",
                        imageUrl = "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 91,
                        lowestPrice = "$49.00",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=Meller+Chunky+Sunglasses"
                    ),
                    matchConfidence = 0.91f,
                    visualAttributesUsed = listOf("tortoiseshell", "polarized"),
                    matchingAlgorithm = "FaceAccessoryAlign v2.2"
                )
            )
        } else {
            // General Fallback (Home decor, diffuser, pleated lamp)
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_diffuser_1",
                        name = "Vitruvi Stone Essential Oil Diffuser",
                        brand = "Vitruvi",
                        imageUrl = "https://images.unsplash.com/photo-1602928321679-560bb453f190?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 96,
                        lowestPrice = "$119.00",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=Vitruvi+Stone+Diffuser"
                    ),
                    matchConfidence = 0.96f,
                    visualAttributesUsed = listOf("ceramic diffuser", "white clay"),
                    matchingAlgorithm = "FeatureVectorSim v4.2"
                )
            )
            matchedList.add(
                ProductMatchResult(
                    matchedProduct = Product(
                        id = "prod_lamp_1",
                        name = "Hay Matin Pleated Table Lamp",
                        brand = "HAY",
                        imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80",
                        matchPercentage = 92,
                        lowestPrice = "$245.00",
                        priceComparison = emptyList(),
                        positiveReviewSummary = "",
                        negativeReviewSummary = "",
                        buyUrl = "https://www.google.com/search?tbm=shop&q=Hay+Matin+Lamp"
                    ),
                    matchConfidence = 0.92f,
                    visualAttributesUsed = listOf("pleated paper lamp", "wooden base"),
                    matchingAlgorithm = "FeatureVectorSim v4.2"
                )
            )
        }

        Log.d(TAG, "findMatches finished. Matched ${matchedList.size} products.")
        return matchedList
    }
}
