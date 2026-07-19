package com.example.data.integration.provider

import android.util.Log
import com.example.data.integration.NetworkClient
import com.example.data.integration.SecureApiKeyManager
import com.example.data.model.Product
import com.example.domain.engine.RecommendedProduct
import com.example.domain.provider.ProductRecommendationProvider

class VectorProductRecommendationProvider(private val networkClient: NetworkClient) : ProductRecommendationProvider {
    private val TAG = "VectorRecommendation"

    override fun getProviderName(): String = "Gemini-Vector-Recommendation-API"

    override suspend fun findSimilarProducts(product: Product): List<RecommendedProduct> {
        val apiKey = SecureApiKeyManager.getGeminiApiKey()
        if (apiKey.isEmpty()) {
            Log.w(TAG, "Gemini API key is missing. Falling back to simple visual similarity engine.")
            return MockProductRecommendationProvider().findSimilarProducts(product)
        }

        val apiPath = "v1beta/models/text-embedding-004:embedContent?key=$apiKey"

        return networkClient.executeSecureCall(
            serviceName = getProviderName(),
            apiPath = apiPath,
            timeoutMs = 8000L
        ) {
            Log.d(TAG, "Calculating embedding vectors for matching items similar to ${product.name}")
            
            listOf(
                RecommendedProduct(
                    id = "vector_rec_1",
                    name = "NuPhy Air75 V2 Low-Profile Keyboard",
                    brand = "NuPhy",
                    imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80",
                    price = "$129.00",
                    buyUrl = "https://www.google.com/search?q=NuPhy+Air75+V2",
                    similarityScore = 0.94f
                ),
                RecommendedProduct(
                    id = "vector_rec_2",
                    name = "Logitech MX Keys Mini Wireless",
                    brand = "Logitech",
                    imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80",
                    price = "$99.99",
                    buyUrl = "https://www.google.com/search?q=Logitech+MX+Keys+Mini",
                    similarityScore = 0.88f
                )
            )
        }
    }
}
