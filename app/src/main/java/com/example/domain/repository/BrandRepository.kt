package com.example.domain.repository

import com.example.domain.model.BrandDetails

interface BrandRepository {
    suspend fun getBrandDetails(brandName: String): BrandDetails?
    suspend fun saveBrandDetails(brandDetails: BrandDetails)
}
