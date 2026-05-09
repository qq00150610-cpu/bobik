package com.example.bobik.data.db

import android.content.Context
import androidx.room.*

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "mem_id") val memId: String,
    val category: String,
    val title: String,
    val content: String,
    val confidence: Double = 0.0,
    @ColumnInfo(name = "created_at") val createdAt: String
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "from_id") val fromId: String,
    val role: String,
    val content: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY id DESC LIMIT :limit")
    fun getAll(limit: Int = 100): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY id DESC LIMIT :limit")
    fun search(query: String, limit: Int = 10): List<MemoryEntity>

    @Query("SELECT COUNT(*) FROM memories")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memory: MemoryEntity)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY id DESC LIMIT :limit")
    fun getRecent(limit: Int = 30): List<ConversationEntity>

    @Query("SELECT COUNT(*) FROM conversations")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conv: ConversationEntity)
}

@Dao
interface ConfigDao {
    @Query("SELECT value FROM config WHERE key = :key")
    fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(entity: ConfigEntity)

    fun set(key: String, value: String) {
        set(ConfigEntity(key, value))
    }
}

@Database(
    entities = [MemoryEntity::class, ConversationEntity::class, ConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationDao(): ConversationDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bobik.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
