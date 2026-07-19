package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Product
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val productListType = Types.newParameterizedType(List::class.java, Product::class.java)
    private val adapter = moshi.adapter<List<Product>>(productListType)

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    @TypeConverter
    fun fromProductList(products: List<Product>?): String {
        return products?.let { adapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toProductList(json: String?): List<Product> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.let { stringListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
