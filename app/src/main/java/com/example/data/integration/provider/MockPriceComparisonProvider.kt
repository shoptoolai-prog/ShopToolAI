package com.example.data.integration.provider

import com.example.data.model.PriceSource
import com.example.domain.provider.PriceComparisonProvider

class MockPriceComparisonProvider : PriceComparisonProvider {
    override fun getProviderName(): String = "Mock-Price-Engine"

    override suspend fun findAlternativePrices(productId: String, productName: String, basePrice: String): List<PriceSource> {
        val numericPrice = basePrice.replace("$", "").toDoubleOrNull() ?: 100.0
        val lowPrice = numericPrice * 0.92
        val highPrice = numericPrice * 1.10
        return listOf(
            PriceSource("Official Store", String.format("$%.2f", numericPrice), false),
            PriceSource("Amazon Marketplace", String.format("$%.2f", lowPrice), true),
            PriceSource("Nordstrom / Specialty Shop", String.format("$%.2f", highPrice), false)
        )
    }
}
