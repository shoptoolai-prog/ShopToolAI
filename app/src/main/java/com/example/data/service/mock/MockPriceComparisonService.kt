package com.example.data.service.mock

import com.example.data.model.PriceSource
import com.example.domain.service.PriceComparisonService
import kotlinx.coroutines.delay

class MockPriceComparisonService : PriceComparisonService {
    override suspend fun fetchPriceSources(productId: String, basePrice: String): List<PriceSource> {
        // Simulate real-time store scrapers
        delay(600)
        
        val numericPrice = basePrice.replace("$", "").toDoubleOrNull() ?: 50.0
        val lowPrice = numericPrice * 0.90
        val highPrice = numericPrice * 1.12
        
        return listOf(
            PriceSource("Official Brand Store", String.format("$%.2f", numericPrice), false),
            PriceSource("Amazon Global Marketplace", String.format("$%.2f", lowPrice), true),
            PriceSource("Nordstrom / Specialty Retail", String.format("$%.2f", highPrice), false)
        )
    }
}
