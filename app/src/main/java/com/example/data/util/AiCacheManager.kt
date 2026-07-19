package com.example.data.util

import com.example.data.model.Product
import java.util.concurrent.ConcurrentHashMap

object AiCacheManager {
    private val cache = ConcurrentHashMap<String, List<Product>>()

    fun get(url: String): List<Product>? {
        val normalized = url.trim().lowercase()
        return cache[normalized]
    }

    fun put(url: String, products: List<Product>) {
        val normalized = url.trim().lowercase()
        cache[normalized] = products
    }

    fun clear() {
        cache.clear()
    }
}
