package com.example.data.local

import androidx.room.*
import com.example.data.model.LoveRoastItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LoveRoastDao {
    @Query("SELECT * FROM love_roast_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<LoveRoastItem>>

    @Query("""
        SELECT * FROM love_roast_history 
        WHERE (inputName LIKE :query OR inputSituation LIKE :query OR outputText LIKE :query) 
        ORDER BY timestamp DESC
    """)
    fun searchHistory(query: String): Flow<List<LoveRoastItem>>

    @Query("SELECT * FROM love_roast_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<LoveRoastItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LoveRoastItem): Long

    @Delete
    suspend fun deleteItem(item: LoveRoastItem)

    @Query("UPDATE love_roast_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM love_roast_history")
    suspend fun clearHistory()
}
