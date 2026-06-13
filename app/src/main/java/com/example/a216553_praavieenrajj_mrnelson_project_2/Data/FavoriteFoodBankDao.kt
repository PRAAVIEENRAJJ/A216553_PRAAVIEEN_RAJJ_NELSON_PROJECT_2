package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteFoodBankDao {
    @Query("SELECT * FROM favorite_food_banks")
    fun getAllFavorites(): Flow<List<FavoriteFoodBank>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(bank: FavoriteFoodBank)

    @Delete
    suspend fun removeFavorite(bank: FavoriteFoodBank)

    @Query("SELECT EXISTS(SELECT * FROM favorite_food_banks WHERE bankId = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Query("DELETE FROM favorite_food_banks")
    suspend fun clearAllFavorites()
}
