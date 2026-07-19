package com.example.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BrandDetails(
    val name: String,
    val description: String,
    val logoUrl: String,
    val officialWebsite: String,
    val marketTier: String, // e.g., Luxury, Premium, Mid-range, Budget
    val categorySpecialties: List<String>
)
