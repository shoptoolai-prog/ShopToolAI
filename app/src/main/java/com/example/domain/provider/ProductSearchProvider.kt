package com.example.domain.provider

import com.example.data.model.Product

interface ProductSearchProvider {
    fun getProviderName(): String
    suspend fun searchProductsByVisuals(visionObjects: List<VisionObject>): List<Product>
}
