package com.example.data.util

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

object RetryUtils {
    private const val TAG = "RetryUtils"

    suspend fun <T> retryWithTimeout(
        timeoutMs: Long = 10000L,
        maxRetries: Int = 3,
        initialDelayMs: Long = 500L,
        factor: Double = 2.0,
        operationName: String = "Operation",
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var latestException: Throwable? = null

        for (attempt in 1..maxRetries) {
            try {
                Log.d(TAG, "Executing $operationName - Attempt $attempt of $maxRetries")
                return withTimeout(timeoutMs) {
                    block()
                }
            } catch (e: Exception) {
                latestException = e
                Log.w(TAG, "Attempt $attempt failed for $operationName: ${e.message}")
                if (attempt < maxRetries) {
                    Log.d(TAG, "Waiting ${currentDelay}ms before retrying...")
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong()
                }
            }
        }
        
        Log.e(TAG, "All $maxRetries attempts failed for $operationName.")
        throw latestException ?: IllegalStateException("Unknown error during $operationName")
    }
}
