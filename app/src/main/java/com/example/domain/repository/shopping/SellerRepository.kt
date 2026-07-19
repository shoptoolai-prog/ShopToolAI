package com.example.domain.repository.shopping

data class SellerInfo(
    val name: String,
    val rating: Float,
    val status: String,
    val shipSpeedRating: Float,
    val returnPolicyRating: Float,
    val score: Float
)

interface SellerRepository {
    suspend fun getSellersForProduct(productId: String): List<SellerInfo>
    suspend fun saveSellerInfo(sellerInfo: SellerInfo)
}
