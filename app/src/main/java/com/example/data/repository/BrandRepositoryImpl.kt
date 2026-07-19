package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.integration.PerformanceMonitor
import com.example.data.util.RetryUtils
import com.example.domain.model.BrandDetails
import com.example.domain.repository.BrandRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class BrandRepositoryImpl(
    private val context: Context,
    private val cacheDirName: String = "intel_brands_cache"
) : BrandRepository {

    private val tag = "BrandRepositoryImpl"
    private val memoryCache = ConcurrentHashMap<String, BrandDetails>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val brandAdapter = moshi.adapter(BrandDetails::class.java)

    private val cacheDir: File by lazy {
        File(context.cacheDir, cacheDirName).apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun getBrandDetails(brandName: String): BrandDetails? = withContext(Dispatchers.IO) {
        val sanitizedKey = brandName.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
        PerformanceMonitor.measureCall("BrandRepository", "getBrandDetails") {
            memoryCache[sanitizedKey]?.let {
                Log.d(tag, "Cache Hit (Memory): $brandName")
                return@measureCall it
            }

            try {
                RetryUtils.retryWithTimeout(maxRetries = 2, operationName = "readBrandFromDisk") {
                    val file = File(cacheDir, "$sanitizedKey.json")
                    if (file.exists()) {
                        val json = file.readText()
                        val brand = brandAdapter.fromJson(json)
                        if (brand != null) {
                            Log.d(tag, "Cache Hit (Disk): $brandName")
                            memoryCache[sanitizedKey] = brand
                            brand
                        } else null
                    } else null
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to read brand $brandName from disk cache: ${e.message}")
                null
            }
        }
    }

    override suspend fun saveBrandDetails(brandDetails: BrandDetails) {
        withContext(Dispatchers.IO) {
            val sanitizedKey = brandDetails.name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            PerformanceMonitor.measureCall("BrandRepository", "saveBrandDetails") {
                memoryCache[sanitizedKey] = brandDetails

                try {
                    RetryUtils.retryWithTimeout(maxRetries = 2, operationName = "writeBrandToDisk") {
                        val file = File(cacheDir, "$sanitizedKey.json")
                        val json = brandAdapter.toJson(brandDetails)
                        file.writeText(json)
                        Log.d(tag, "Saved brand ${brandDetails.name} to disk cache")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to save brand ${brandDetails.name} to disk: ${e.message}")
                }
            }
        }
    }
}
