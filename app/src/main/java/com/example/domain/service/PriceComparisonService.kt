package com.example.domain.service

import com.example.data.model.PriceSource

interface PriceComparisonService {
    suspend fun fetchPriceSources(productId: String, basePrice: String): List<PriceSource>
}
