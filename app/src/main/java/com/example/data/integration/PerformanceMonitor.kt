package com.example.data.integration

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    
    private val callCounters = ConcurrentHashMap<String, AtomicInteger>()
    private val failureCounters = ConcurrentHashMap<String, AtomicInteger>()
    private val totalLatencyMs = ConcurrentHashMap<String, Long>()

    suspend fun <T> measureCall(serviceName: String, operationName: String, block: suspend () -> T): T {
        val key = "$serviceName.$operationName"
        callCounters.getOrPut(key) { AtomicInteger(0) }.incrementAndGet()
        
        val startTime = System.currentTimeMillis()
        try {
            val result = block()
            val latency = System.currentTimeMillis() - startTime
            totalLatencyMs.merge(key, latency) { old, new -> old + new }
            Log.d(TAG, "PERF: $key completed in ${latency}ms")
            return result
        } catch (e: Exception) {
            failureCounters.getOrPut(key) { AtomicInteger(0) }.incrementAndGet()
            val latency = System.currentTimeMillis() - startTime
            Log.e(TAG, "PERF ERROR: $key failed in ${latency}ms with error: ${e.message}")
            throw e
        }
    }

    fun getStats(serviceName: String, operationName: String): String {
        val key = "$serviceName.$operationName"
        val total = callCounters[key]?.get() ?: 0
        val failures = failureCounters[key]?.get() ?: 0
        val sumLatency = totalLatencyMs[key] ?: 0L
        val averageLatency = if (total > 0) sumLatency / total else 0
        return "Service $key -> Total Calls: $total, Failures: $failures, Avg Latency: ${averageLatency}ms"
    }
}
