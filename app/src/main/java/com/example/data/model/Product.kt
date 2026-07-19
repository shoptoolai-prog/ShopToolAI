package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val imageUrl: String,
    val matchPercentage: Int,
    val lowestPrice: String,
    val priceComparison: List<PriceSource>,
    val positiveReviewSummary: String,
    val negativeReviewSummary: String,
    val buyUrl: String
)

@JsonClass(generateAdapter = true)
data class PriceSource(
    val sourceName: String,
    val price: String,
    val isLowest: Boolean = false
)
