package com.example.data.integration

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay

class RateLimiter(private val requestsPerSecond: Double) {
    private val mutex = Mutex()
    private val minIntervalMs = (1000 / requestsPerSecond).toLong()
    private var lastRequestTimeMs = 0L

    suspend fun acquire() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val timeSinceLast = currentTime - lastRequestTimeMs
            if (timeSinceLast < minIntervalMs) {
                val waitTime = minIntervalMs - timeSinceLast
                Log.d("RateLimiter", "Rate limit trigger. Throttling request for ${waitTime}ms")
                delay(waitTime)
            }
            lastRequestTimeMs = System.currentTimeMillis()
        }
    }
}
