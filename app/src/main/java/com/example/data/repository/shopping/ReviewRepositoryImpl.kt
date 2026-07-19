package com.example.data.repository.shopping

import android.content.Context
import android.util.Log
import com.example.data.integration.NetworkClient
import com.example.data.util.NetworkUtils
import com.example.domain.repository.shopping.ReviewAggregate
import com.example.domain.repository.shopping.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class ReviewRepositoryImpl(
    private val context: Context,
    private val networkClient: NetworkClient = NetworkClient(context)
) : ReviewRepository {

    private val tag = "ReviewRepositoryImpl"
    private val memoryCache = ConcurrentHashMap<String, ReviewAggregate>()

    override suspend fun getReviewAggregate(productId: String): ReviewAggregate? = withContext(Dispatchers.IO) {
        memoryCache[productId]?.let {
            Log.d(tag, "Memory cache hit for reviews of $productId")
            return@withContext it
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                networkClient.executeSecureCall(
                    serviceName = "ReviewAggregationService",
                    apiPath = "reviews/$productId"
                ) {
                    val aggregate = ReviewAggregate(
                        rating = 4.8f,
                        reviewCount = 150,
                        positiveSummary = "Users highly praise the responsive build quality, gorgeous aesthetics, and comfortable design.",
                        negativeSummary = "Some users noted that the keycaps are slightly prone to grease accumulation.",
                        reviews = listOf(
                            "Best low-profile keyboard I have ever purchased!",
                            "Solid construction and long battery life.",
                            "Highly responsive switches, but keys feel a bit close."
                        )
                    )
                    memoryCache[productId] = aggregate
                    aggregate
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to aggregate reviews for $productId: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    override suspend fun saveReviewAggregate(productId: String, aggregate: ReviewAggregate) {
        withContext(Dispatchers.IO) {
            memoryCache[productId] = aggregate
            Log.d(tag, "Saved review aggregate for product: $productId")
        }
    }
}
