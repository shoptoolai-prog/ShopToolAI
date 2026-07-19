package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.integration.PerformanceMonitor
import com.example.data.util.RetryUtils
import com.example.domain.model.IntelProductRef
import com.example.domain.repository.RecommendationRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class RecommendationRepositoryImpl(
    private val context: Context,
    private val cacheDirName: String = "intel_recommendations_cache"
) : RecommendationRepository {

    private val tag = "RecommendationRepositoryImpl"
    private val memoryCache = ConcurrentHashMap<String, List<IntelProductRef>>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, IntelProductRef::class.java)
    private val listAdapter = moshi.adapter<List<IntelProductRef>>(listType)

    private val cacheDir: File by lazy {
        File(context.cacheDir, cacheDirName).apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun getRecommendationsForProduct(productId: String, limit: Int): List<IntelProductRef> = withContext(Dispatchers.IO) {
        PerformanceMonitor.measureCall("RecommendationRepository", "getRecommendationsForProduct") {
            memoryCache[productId]?.let {
                Log.d(tag, "Cache Hit (Memory): $productId recommendations")
                return@measureCall it.take(limit)
            }

            try {
                RetryUtils.retryWithTimeout(maxRetries = 2, operationName = "readRecommendations") {
                    val file = File(cacheDir, "$productId.json")
                    if (file.exists()) {
                        val json = file.readText()
                        val list = listAdapter.fromJson(json)
                        if (list != null) {
                            Log.d(tag, "Cache Hit (Disk): $productId recommendations")
                            memoryCache[productId] = list
                            list.take(limit)
                        } else emptyList()
                    } else emptyList()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to read recommendations for product $productId: ${e.message}")
                emptyList()
            }
        }
    }

    override suspend fun updateRecommendations(productId: String, recommendations: List<IntelProductRef>) {
        withContext(Dispatchers.IO) {
            PerformanceMonitor.measureCall("RecommendationRepository", "updateRecommendations") {
                memoryCache[productId] = recommendations

                try {
                    RetryUtils.retryWithTimeout(maxRetries = 2, operationName = "writeRecommendations") {
                        val file = File(cacheDir, "$productId.json")
                        val json = listAdapter.toJson(recommendations)
                        file.writeText(json)
                        Log.d(tag, "Saved recommendations for product $productId")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to save recommendations for product $productId: ${e.message}")
                }
            }
        }
    }
}
