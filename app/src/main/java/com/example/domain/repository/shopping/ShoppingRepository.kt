package com.example.domain.repository.shopping

import com.example.domain.model.ShoppingProduct

interface ShoppingRepository {
    suspend fun getProductById(id: String): ShoppingProduct?
    suspend fun saveProduct(product: ShoppingProduct)
    suspend fun deleteProduct(id: String)
    suspend fun getProducts(limit: Int, offset: Int): List<ShoppingProduct>
    suspend fun searchProducts(query: String, limit: Int, offset: Int): List<ShoppingProduct>
    suspend fun syncProducts(): Boolean
}
