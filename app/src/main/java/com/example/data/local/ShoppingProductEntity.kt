package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_products")
data class ShoppingProductEntity(
    @PrimaryKey val id: String,
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
    val stockStatus: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
