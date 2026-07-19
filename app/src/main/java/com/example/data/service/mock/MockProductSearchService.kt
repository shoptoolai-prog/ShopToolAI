package com.example.data.service.mock

import com.example.data.model.PriceSource
import com.example.data.model.Product
import com.example.domain.service.DetectedObject
import com.example.domain.service.ProductSearchService
import kotlinx.coroutines.delay

class MockProductSearchService : ProductSearchService {
    override suspend fun searchProducts(detectedObjects: List<DetectedObject>): List<Product> {
        // Simulate database/index lookup delay
        delay(1000)
        
        return detectedObjects.mapIndexed { index, obj ->
            val id = "prod_${obj.category.lowercase()}_${index + 1}"
            val name = when (obj.category) {
                "Keyboard" -> "Keychron Q1 Pro QMK/VIA Wireless Keyboard"
                "Monitor Light" -> "BenQ ScreenBar Halo LED Monitor Light"
                "Desk Mat" -> "Grovemade Wool Felt Desk Pad (Medium)"
                "Diffuser" -> "Vitruvi Stone Essential Oil Diffuser"
                "Lamp" -> "Hay Matin Pleated Table Lamp"
                "Tray" -> "Nordic Concrete Oval Vanity Tray"
                "Sneakers" -> "New Balance 550 Vintage White Leather"
                "Sunglasses" -> "Meller Bio-Based Chunky Sunglasses"
                "Overshirt" -> "Selected Homme Relaxed Linen Shirt"
                "Smart Mug" -> "Ember Temperature Control Smart Mug 2"
                "Journal" -> "Smythson Crossgrain Leather Bound Journal"
                else -> "${obj.category} Premium Curated Edition"
            }
            
            val brand = when (obj.category) {
                "Keyboard" -> "Keychron"
                "Monitor Light" -> "BenQ"
                "Desk Mat" -> "Grovemade"
                "Diffuser" -> "Vitruvi"
                "Lamp" -> "HAY"
                "Tray" -> "Kinto"
                "Sneakers" -> "New Balance"
                "Sunglasses" -> "Meller"
                "Overshirt" -> "Selected Homme"
                "Smart Mug" -> "Ember"
                "Journal" -> "Smythson"
                else -> "Generic Boutique"
            }
            
            val imageUrl = when (obj.category) {
                "Keyboard" -> "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80"
                "Monitor Light" -> "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80"
                "Desk Mat" -> "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=600&q=80"
                "Diffuser" -> "https://images.unsplash.com/photo-1602928321679-560bb453f190?auto=format&fit=crop&w=600&q=80"
                "Lamp" -> "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80"
                "Tray" -> "https://images.unsplash.com/photo-1532372320978-9b4d7a92b24d?auto=format&fit=crop&w=600&q=80"
                "Sneakers" -> "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80"
                "Sunglasses" -> "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?auto=format&fit=crop&w=600&q=80"
                "Overshirt" -> "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?auto=format&fit=crop&w=600&q=80"
                "Smart Mug" -> "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80"
                "Journal" -> "https://images.unsplash.com/photo-1531346878377-a5be20888e57?auto=format&fit=crop&w=600&q=80"
                else -> "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"
            }
            
            val basePriceVal = when (obj.category) {
                "Keyboard" -> 199.99
                "Monitor Light" -> 139.00
                "Desk Mat" -> 80.00
                "Diffuser" -> 119.00
                "Lamp" -> 245.00
                "Tray" -> 35.00
                "Sneakers" -> 110.00
                "Sunglasses" -> 49.00
                "Overshirt" -> 79.99
                "Smart Mug" -> 149.95
                "Journal" -> 125.00
                else -> 59.99
            }
            
            Product(
                id = id,
                name = name,
                brand = brand,
                imageUrl = imageUrl,
                matchPercentage = (obj.confidence * 100).toInt(),
                lowestPrice = String.format("$%.2f", basePriceVal),
                priceComparison = emptyList(), // Filled by PriceComparisonService
                positiveReviewSummary = "",   // Filled by ReviewAnalysisService
                negativeReviewSummary = "",   // Filled by ReviewAnalysisService
                buyUrl = "https://www.google.com/search?tbm=shop&q=${java.net.URLEncoder.encode(name, "UTF-8")}"
            )
        }
    }
}
