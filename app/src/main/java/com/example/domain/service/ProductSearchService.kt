package com.example.domain.service

import com.example.data.model.Product

interface ProductSearchService {
    suspend fun searchProducts(detectedObjects: List<DetectedObject>): List<Product>
}
