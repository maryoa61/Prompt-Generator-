package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

typealias PromptHistoryEntity = PromptEntity

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val styleName: String = "SOFTWARE_DEV",
    val inputKeywords: String = "",
    val rawInput: String = "",
    val keywords: String = "",
    val style: String = "SOFTWARE_DEV",
    val generatedPrompt: String = "",
    val role: String = "",
    val context: String = "",
    val task: String = "",
    val constraints: String = "",
    val outputFormat: String = "",
    val fullGeneratedPrompt: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY timestamp DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE title LIKE '%' || :query || '%' OR inputKeywords LIKE '%' || :query || '%' OR fullGeneratedPrompt LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPrompts(query: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun getPromptById(id: Long): PromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PromptEntity): Long

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePromptById(id: Long)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE prompts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM prompts")
    suspend fun deleteAllPrompts()

    @Query("DELETE FROM prompts")
    suspend fun clearAll()
}

typealias PromptHistoryDao = PromptDao

@Database(entities = [PromptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
}
