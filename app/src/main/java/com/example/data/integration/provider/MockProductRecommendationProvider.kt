package com.example.data.integration.provider

import com.example.data.model.Product
import com.example.domain.engine.RecommendedProduct
import com.example.domain.provider.ProductRecommendationProvider

class MockProductRecommendationProvider : ProductRecommendationProvider {
    override fun getProviderName(): String = "Mock-Vector-Matcher"

    override suspend fun findSimilarProducts(product: Product): List<RecommendedProduct> {
        val idLower = product.id.lowercase()
        return when {
            idLower.contains("keyboard") -> listOf(
                RecommendedProduct("rec_kb_1", "NuPhy Air75 V2 Low-Profile Keyboard", "NuPhy", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80", "$129.00", "https://www.google.com/search?q=NuPhy+Air75+V2", 0.91f),
                RecommendedProduct("rec_kb_2", "Logitech MX Keys Mini Wireless", "Logitech", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80", "$99.99", "https://www.google.com/search?q=Logitech+MX+Keys+Mini", 0.85f)
            )
            else -> listOf(
                RecommendedProduct("rec_gen_1", "Alternative Curated Boutique Option", "Nordic Studio", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80", "$45.00", "https://www.google.com/search?q=Nordic+Studio+Alternative", 0.80f)
            )
        }
    }
}
