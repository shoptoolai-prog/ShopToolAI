package com.example.data.engine.mock

import android.util.Log
import com.example.data.model.Product
import com.example.domain.engine.RecommendedProduct
import com.example.domain.engine.SimilarProductRecommendationEngine
import kotlinx.coroutines.delay

class MockSimilarProductRecommendationEngine : SimilarProductRecommendationEngine {
    private val TAG = "MockRecommendation"

    override suspend fun recommendSimilar(product: Product): List<RecommendedProduct> {
        Log.d(TAG, "recommendSimilar started for product: ${product.name}")
        delay(500)

        val idLower = product.id.lowercase()
        val recommendations = when {
            idLower.contains("keyboard") -> listOf(
                RecommendedProduct("rec_kb_1", "NuPhy Air75 V2 Low-Profile Keyboard", "NuPhy", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80", "$129.00", "https://www.google.com/search?q=NuPhy+Air75+V2", 0.91f),
                RecommendedProduct("rec_kb_2", "Logitech MX Keys Mini Wireless", "Logitech", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80", "$99.99", "https://www.google.com/search?q=Logitech+MX+Keys+Mini", 0.85f)
            )
            idLower.contains("light") -> listOf(
                RecommendedProduct("rec_lt_1", "Quntis Computer Monitor Light Bar", "Quntis", "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80", "$39.99", "https://www.google.com/search?q=Quntis+Monitor+Light+Bar", 0.88f),
                RecommendedProduct("rec_lt_2", "Yeelight Screenbar Pro RGB", "Yeelight", "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80", "$89.90", "https://www.google.com/search?q=Yeelight+Screenbar+Pro", 0.82f)
            )
            idLower.contains("sneakers") -> listOf(
                RecommendedProduct("rec_sn_1", "Adidas Forum Low Classic White", "Adidas", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80", "$100.00", "https://www.google.com/search?q=Adidas+Forum+Low", 0.93f),
                RecommendedProduct("rec_sn_2", "Nike Killshot 2 Leather Sail", "Nike", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80", "$90.00", "https://www.google.com/search?q=Nike+Killshot+2", 0.89f)
            )
            else -> listOf(
                RecommendedProduct("rec_gen_1", "Alternative Curated Boutique Option", "Nordic Studio", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80", "$45.00", "https://www.google.com/search?q=Nordic+Studio+Alternative", 0.80f)
            )
        }

        Log.d(TAG, "recommendSimilar finished. Recommended ${recommendations.size} products.")
        return recommendations
    }
}
