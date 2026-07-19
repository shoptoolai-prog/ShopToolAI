package com.example.data.local

import androidx.room.*

@Dao
interface ShoppingProductDao {
    @Query("SELECT * FROM shopping_products WHERE id = :id")
    suspend fun getProductById(id: String): ShoppingProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ShoppingProductEntity)

    @Delete
    suspend fun deleteProduct(product: ShoppingProductEntity)

    @Query("SELECT * FROM shopping_products ORDER BY lastUpdated DESC LIMIT :limit OFFSET :offset")
    suspend fun getProducts(limit: Int, offset: Int): List<ShoppingProductEntity>

    @Query("SELECT * FROM shopping_products WHERE name LIKE :query OR brand LIKE :query OR category LIKE :query ORDER BY lastUpdated DESC LIMIT :limit OFFSET :offset")
    suspend fun searchProducts(query: String, limit: Int, offset: Int): List<ShoppingProductEntity>

    @Query("DELETE FROM shopping_products WHERE id = :id")
    suspend fun deleteProductById(id: String)
}
