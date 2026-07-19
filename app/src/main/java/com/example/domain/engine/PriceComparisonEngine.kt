package com.example.domain.engine

import com.example.data.model.PriceSource

interface PriceComparisonEngine {
    suspend fun comparePrices(productId: String, productName: String, currentBasePrice: String): List<PriceSource>
}
