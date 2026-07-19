package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Product

@Entity(tableName = "search_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val products: List<Product>
)
