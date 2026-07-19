package com.example.data.engine.mock

import android.util.Log
import com.example.data.model.PriceSource
import com.example.domain.engine.PriceComparisonEngine
import kotlinx.coroutines.delay

class MockPriceComparisonEngine : PriceComparisonEngine {
    private val TAG = "MockPriceComparison"

    override suspend fun comparePrices(productId: String, productName: String, currentBasePrice: String): List<PriceSource> {
        Log.d(TAG, "comparePrices started for: $productName ($productId)")
        delay(500)
        
        val numericPrice = currentBasePrice.replace("$", "").toDoubleOrNull() ?: 100.0
        val lowPrice = numericPrice * 0.92
        val highPrice = numericPrice * 1.10
        
        val results = listOf(
            PriceSource("Official Store", String.format("$%.2f", numericPrice), false),
            PriceSource("Amazon Marketplace", String.format("$%.2f", lowPrice), true),
            PriceSource("Nordstrom / Specialty Shop", String.format("$%.2f", highPrice), false)
        )
        Log.d(TAG, "comparePrices finished. Found ${results.size} retail sources.")
        return results
    }
}
