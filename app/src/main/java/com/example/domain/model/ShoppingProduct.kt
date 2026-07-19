package com.example.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShoppingProduct(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val color: String,
    val images: List<String>,
    val price: Double,
    val discount: Double,
    val currency: String,
    val storeName: String,
    val productUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val aiConfidence: Float,
    val aiRecommendationScore: Float,
    val deliveryTime: String,
    val stockStatus: String
)
