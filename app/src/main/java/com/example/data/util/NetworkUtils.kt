package com.example.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object NetworkUtils {
    private const val TAG = "NetworkUtils"

    fun isInternetAvailable(context: Context): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
                val connected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                Log.d(TAG, "isInternetAvailable: $connected")
                return connected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking internet connectivity, falling back to true", e)
        }
        return true // Fallback to true if checking fails (e.g. mock situations or permissions issues)
    }
}
