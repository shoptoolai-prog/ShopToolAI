package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.integration.PerformanceMonitor
import com.example.domain.model.IntelProduct
import com.example.domain.repository.SearchRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class SearchRepositoryImpl(
    private val context: Context,
    private val cacheDirName: String = "intel_products_cache"
) : SearchRepository {

    private val tag = "SearchRepositoryImpl"
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val productAdapter = moshi.adapter(IntelProduct::class.java)

    private val cacheDir: File by lazy {
        File(context.cacheDir, cacheDirName).apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun searchProducts(query: String, category: String?, brand: String?): List<IntelProduct> = withContext(Dispatchers.IO) {
        PerformanceMonitor.measureCall("SearchRepository", "searchProducts") {
            try {
                val files = cacheDir.listFiles() ?: emptyArray()
                val qLower = query.lowercase().trim()

                files.asSequence()
                    .mapNotNull { file ->
                        try {
                            val json = file.readText()
                            productAdapter.fromJson(json)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .filter { product ->
                        // Match category if specified
                        if (category != null && !product.category.equals(category, ignoreCase = true)) {
                            return@filter false
                        }
                        // Match brand if specified
                        if (brand != null && !product.brand.equals(brand, ignoreCase = true)) {
                            return@filter false
                        }
                        // Fuzzy keyword search
                        if (qLower.isNotEmpty()) {
                            val inName = product.name.lowercase().contains(qLower)
                            val inBrand = product.brand.lowercase().contains(qLower)
                            val inDesc = product.description.lowercase().contains(qLower)
                            val inKeywords = product.searchKeywords.any { it.lowercase().contains(qLower) }
                            inName || inBrand || inDesc || inKeywords
                        } else {
                            true
                        }
                    }
                    .toList()
            } catch (e: Exception) {
                Log.e(tag, "Failed to perform local search: ${e.message}")
                emptyList()
            }
        }
    }

    override suspend fun indexProduct(product: IntelProduct) {
        withContext(Dispatchers.IO) {
            PerformanceMonitor.measureCall("SearchRepository", "indexProduct") {
                try {
                    val file = File(cacheDir, "${product.id}.json")
                    val json = productAdapter.toJson(product)
                    file.writeText(json)
                    Log.d(tag, "Indexed product ${product.id} successfully")
                } catch (e: Exception) {
                    Log.e(tag, "Failed to index product ${product.id}: ${e.message}")
                }
            }
        }
    }
}
