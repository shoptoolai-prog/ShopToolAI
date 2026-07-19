package com.example.domain.repository

import com.example.domain.model.IntelProduct

interface ProductRepository {
    suspend fun getProductById(id: String): IntelProduct?
    suspend fun saveProduct(product: IntelProduct)
    suspend fun deleteProduct(id: String)
    suspend fun getAllProducts(limit: Int = 100, offset: Int = 0): List<IntelProduct>
}
