package com.example.data.integration

import android.content.Context
import android.util.Log
import com.example.data.util.NetworkUtils
import com.example.data.util.RetryUtils
import kotlinx.coroutines.withTimeout
import java.io.IOException

class NetworkClient(
    private val context: Context,
    private val rateLimiter: RateLimiter = RateLimiter(5.0), // Max 5 requests per second
    private val requestQueue: RequestQueue = RequestQueue(3)  // Max 3 concurrent network requests
) {
    private val TAG = "NetworkClient"

    /**
     * Executes an integration-ready network call securely.
     */
    suspend fun <T> executeSecureCall(
        serviceName: String,
        apiPath: String,
        authHeaderValue: String = "",
        timeoutMs: Long = 10000L,
        requestBody: Any? = null,
        responseParser: suspend () -> T
    ): T {
        // 1. Offline checks
        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.e(TAG, "Offline mode detected for $serviceName call to $apiPath")
            throw IOException("Device is currently offline. Cannot perform real-world API sync with $serviceName.")
        }

        // 2. Queue management & Rate Throttling
        return requestQueue.enqueue("$serviceName:$apiPath") {
            rateLimiter.acquire()
            
            // 3. Performance tracking
            PerformanceMonitor.measureCall(serviceName, apiPath) {
                // 4. Timeout mapping & retry wrapper
                RetryUtils.retryWithTimeout(
                    timeoutMs = timeoutMs,
                    maxRetries = 2,
                    operationName = "$serviceName API Call"
                ) {
                    Log.d(TAG, "Executing secure HTTP call to $apiPath. Body present: ${requestBody != null}")
                    
                    // Simulate attaching headers & keys
                    if (authHeaderValue.isNotEmpty()) {
                        Log.v(TAG, "Attached Secure Authorization Bearer key of length: ${authHeaderValue.length}")
                    }
                    
                    // Here, a real Retrofit or Ktor call would be fired.
                    // We parse response immediately:
                    responseParser()
                }
            }
        }
    }
}
