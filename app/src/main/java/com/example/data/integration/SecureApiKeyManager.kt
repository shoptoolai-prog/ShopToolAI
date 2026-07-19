package com.example.data.integration

import android.util.Log

object SecureApiKeyManager {
    private const val TAG = "SecureApiKeyManager"

    // Safe retrieval of API keys with validation check.
    fun getGeminiApiKey(): String {
        // Try retrieving from BuildConfig or system properties
        val key = getEnvOrProperty("GEMINI_API_KEY") ?: ""
        if (key.isEmpty() || key == "PLACEHOLDER") {
            Log.w(TAG, "GEMINI_API_KEY is not defined or is placeholder. Falling back to mock engine safely.")
        }
        return key
    }

    fun getGoogleShoppingApiKey(): String {
        val key = getEnvOrProperty("GOOGLE_SHOPPING_API_KEY") ?: ""
        if (key.isEmpty()) {
            Log.w(TAG, "GOOGLE_SHOPPING_API_KEY is missing. Retail comparisons will run in simulated live mode.")
        }
        return key
    }

    private fun getEnvOrProperty(name: String): String? {
        return try {
            // Check System property or environment
            System.getProperty(name) ?: System.getenv(name)
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up API Key for name: $name", e)
            null
        }
    }
}
