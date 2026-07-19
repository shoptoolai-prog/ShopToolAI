package com.example.data.repository.shopping

import android.content.Context
import android.util.Log
import com.example.data.integration.NetworkClient
import com.example.data.util.NetworkUtils
import com.example.domain.repository.shopping.SellerInfo
import com.example.domain.repository.shopping.SellerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class SellerRepositoryImpl(
    private val context: Context,
    private val networkClient: NetworkClient = NetworkClient(context)
) : SellerRepository {

    private val tag = "SellerRepositoryImpl"
    private val memoryCache = ConcurrentHashMap<String, List<SellerInfo>>()

    override suspend fun getSellersForProduct(productId: String): List<SellerInfo> = withContext(Dispatchers.IO) {
        memoryCache[productId]?.let {
            Log.d(tag, "Memory cache hit for sellers of $productId")
            return@withContext it
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            try {
                networkClient.executeSecureCall(
                    serviceName = "SellerRankingService",
                    apiPath = "sellers/$productId"
                ) {
                    val sellers = listOf(
                        SellerInfo(
                            name = "NuPhy Official Store",
                            rating = 4.9f,
                            status = "Active",
                            shipSpeedRating = 4.8f,
                            returnPolicyRating = 4.9f,
                            score = 0.98f
                        ),
                        SellerInfo(
                            name = "TechWarehouse Corp",
                            rating = 4.4f,
                            status = "Active",
                            shipSpeedRating = 4.2f,
                            returnPolicyRating = 4.0f,
                            score = 0.85f
                        )
                    )
                    memoryCache[productId] = sellers
                    sellers
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to get sellers for $productId: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun saveSellerInfo(sellerInfo: SellerInfo) {
        withContext(Dispatchers.IO) {
            Log.d(tag, "Saved seller info: ${sellerInfo.name}")
        }
    }
}
