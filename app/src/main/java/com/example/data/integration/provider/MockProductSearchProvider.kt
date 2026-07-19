package com.example.data.integration.provider

import com.example.data.model.Product
import com.example.domain.provider.ProductSearchProvider
import com.example.domain.provider.VisionObject

class MockProductSearchProvider : ProductSearchProvider {
    override fun getProviderName(): String = "Mock-Product-Search-v1"

    override suspend fun searchProductsByVisuals(visionObjects: List<VisionObject>): List<Product> {
        return listOf(
            Product(
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
            Product(
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
            )
        )
    }
}
