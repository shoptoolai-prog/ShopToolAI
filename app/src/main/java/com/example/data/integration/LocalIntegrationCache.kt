package com.example.data.integration

import java.util.concurrent.ConcurrentHashMap

class LocalIntegrationCache<K : Any, V : Any>(private val maxEntries: Int = 100) {
    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() > entry.expiryTimeMs) {
            cache.remove(key)
            return null
        }
        return entry.value
    }

    fun put(key: K, value: V, durationMs: Long = 300_000L) { // Default 5 mins cache
        if (cache.size >= maxEntries) {
            // Simple eviction: clear oldest or just clear half
            val keys = cache.keys()
            if (keys.hasMoreElements()) {
                cache.remove(keys.nextElement())
            }
        }
        cache[key] = CacheEntry(value, System.currentTimeMillis() + durationMs)
    }

    fun remove(key: K) {
        cache.remove(key)
    }

    fun clear() {
        cache.clear()
    }

    private data class CacheEntry<V>(val value: V, val expiryTimeMs: Long)
}
