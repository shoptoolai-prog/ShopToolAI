package com.example.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IntelProductRef(
    val id: String,
    val name: String,
    val brand: String,
    val thumbnail: String,
    val price: String,
    val similarityScore: Float
)

@JsonClass(generateAdapter = true)
data class IntelProduct(
    val id: String,
    val sku: String?,
    val name: String,
    val brand: String,
    val category: String,
    val color: String,
    val gender: String,
    val confidenceScore: Float,
    val thumbnail: String,
    val description: String,
    val similarProducts: List<IntelProductRef>,
    val searchKeywords: List<String>,
    val price: String = "$0.00",
    val buyUrl: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
