package com.example.data.engine.integration

import com.example.data.model.PriceSource
import com.example.domain.engine.PriceComparisonEngine
import com.example.domain.provider.ProviderManager

class IntegrationPriceComparisonEngine(
    private val providerManager: ProviderManager
) : PriceComparisonEngine {

    override suspend fun comparePrices(productId: String, productName: String, currentBasePrice: String): List<PriceSource> {
        val activeProvider = providerManager.getActivePriceProvider()
        return activeProvider.findAlternativePrices(productId, productName, currentBasePrice)
    }
}
