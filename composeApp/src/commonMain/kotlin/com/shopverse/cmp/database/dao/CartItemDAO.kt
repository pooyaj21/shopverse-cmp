package com.shopverse.cmp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shopverse.cmp.database.model.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartItemEntity): Long

    @Query("DELETE FROM CartItem WHERE product_id = :productId")
    suspend fun delete(productId: String)

    @Query("DELETE FROM CartItem")
    suspend fun deleteAll()

    @Query("SELECT * FROM CartItem")
    suspend fun selectAll(): List<CartItemEntity>

    @Query("SELECT * FROM CartItem")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM CartItem")
    fun observeTotalCount(): Flow<Int>
}
