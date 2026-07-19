package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.integration.PerformanceMonitor
import com.example.data.util.RetryUtils
import com.example.domain.model.IntelProduct
import com.example.domain.repository.ProductRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ProductRepositoryImpl(
    private val context: Context,
    private val cacheDirName: String = "intel_products_cache"
) : ProductRepository {

    private val tag = "ProductRepositoryImpl"
    private val memoryCache = ConcurrentHashMap<String, IntelProduct>()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val productAdapter = moshi.adapter(IntelProduct::class.java)

    private val cacheDir: File by lazy {
        File(context.cacheDir, cacheDirName).apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun getProductById(id: String): IntelProduct? = withContext(Dispatchers.IO) {
        PerformanceMonitor.measureCall("ProductRepository", "getProductById") {
            // Check Memory Cache
            memoryCache[id]?.let {
                Log.d(tag, "Cache Hit (Memory): $id")
                return@measureCall it
            }

            // Check Disk/Local Cache with Retry
            try {
                RetryUtils.retryWithTimeout(maxRetries = 2, operationName = "readProductFromDisk") {
                    val file = File(cacheDir, "$id.json")
                    if (file.exists()) {
                        val json = file.readText()
                        val product = productAdapter.fromJson(json)
                        if (product != null) {
                            Log.d(tag, "Cache Hit (Disk): $id")
                            memoryCache[id] = product
                            product
                        } else null
                    } else null
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to read product $id from disk cache: ${e.message}")
                null
            }
        }
    }

    override suspend fun saveProduct(product: IntelProduct) {
        withContext(Dispatchers.IO) {
            PerformanceMonitor.measureCall("ProductRepository", "saveProduct") {
                // Update Memory Cache
                memoryCache[product.id] = product

                // Save to Disk/Local Cache with Retry
                try {
                    RetryUtils.retryWithTimeout(maxRetries = 2, operationName = "writeProductToDisk") {
                        val file = File(cacheDir, "${product.id}.json")
                        val json = productAdapter.toJson(product)
                        file.writeText(json)
                        Log.d(tag, "Saved product ${product.id} to disk cache")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to save product ${product.id} to disk cache: ${e.message}")
                }
            }
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(Dispatchers.IO) {
            PerformanceMonitor.measureCall("ProductRepository", "deleteProduct") {
                memoryCache.remove(id)
                try {
                    val file = File(cacheDir, "$id.json")
                    if (file.exists()) {
                        file.delete()
                        Log.d(tag, "Deleted product $id from disk cache")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to delete product $id: ${e.message}")
                }
            }
        }
    }

    override suspend fun getAllProducts(limit: Int, offset: Int): List<IntelProduct> = withContext(Dispatchers.IO) {
        PerformanceMonitor.measureCall("ProductRepository", "getAllProducts") {
            try {
                val files = cacheDir.listFiles() ?: emptyArray()
                files.asSequence()
                    .drop(offset)
                    .take(limit)
                    .mapNotNull { file ->
                        try {
                            val json = file.readText()
                            productAdapter.fromJson(json)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .toList()
            } catch (e: Exception) {
                Log.e(tag, "Failed to list all cached products: ${e.message}")
                memoryCache.values.toList()
            }
        }
    }
}
