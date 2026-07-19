package com.example.domain.repository

import com.example.domain.model.IntelProduct

interface SearchRepository {
    suspend fun searchProducts(query: String, category: String? = null, brand: String? = null): List<IntelProduct>
    suspend fun indexProduct(product: IntelProduct)
}
