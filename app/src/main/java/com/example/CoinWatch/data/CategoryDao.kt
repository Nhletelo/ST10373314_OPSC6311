package com.example.CoinWatch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//Dao
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE user_id = :userId")
    suspend fun getCategoriesByUser(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE category_id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Category?

    @Query("SELECT category_id FROM categories WHERE category_name = :name AND user_id = :userId LIMIT 1")
    suspend fun getCategoryIdByNameAndUserId(name: String, userId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)
}

