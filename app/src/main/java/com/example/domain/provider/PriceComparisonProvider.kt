package com.example.domain.provider

import com.example.data.model.PriceSource

interface PriceComparisonProvider {
    fun getProviderName(): String
    suspend fun findAlternativePrices(productId: String, productName: String, basePrice: String): List<PriceSource>
}
