package com.example.domain.repository.shopping

import com.example.domain.model.ShoppingProduct

interface PriceRepository {
    suspend fun getPriceComparison(productId: String): List<ShoppingProduct>
    suspend fun getHistoricalPrices(productId: String): List<Pair<Long, Double>>
    suspend fun updatePrice(productId: String, storeName: String, price: Double)
}
