package com.kanav.spirelabassign.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun observeAll(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items")
    suspend fun getAll(): List<CartItem>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getById(productId: Int): CartItem?

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    fun observeById(productId: Int): Flow<CartItem?>

    @Upsert
    suspend fun upsert(item: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun delete(productId: Int)
}
