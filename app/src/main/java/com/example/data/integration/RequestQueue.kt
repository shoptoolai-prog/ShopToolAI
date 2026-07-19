package com.example.data.integration

import android.util.Log
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class RequestQueue(maxConcurrentRequests: Int = 2) {
    private val semaphore = Semaphore(maxConcurrentRequests)

    suspend fun <T> enqueue(operationName: String, block: suspend () -> T): T {
        Log.d("RequestQueue", "Enqueuing request: $operationName. Waiting for slot...")
        return semaphore.withPermit {
            Log.d("RequestQueue", "Executing request: $operationName inside active slot.")
            block()
        }
    }
}
