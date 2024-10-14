package com.example.funfactassignment

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.example.dreamdoodler.FilePath
import kotlinx.coroutines.flow.Flow

/**
 * Database for storing fun facts via Room
 */
@Database(
    entities= [FilePath::class],
    version = 2,
    exportSchema = false
)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
}

/**
 * Data Access Object for file paths
 */
@Dao
interface DrawingDao {
    @Upsert
    suspend fun addFilePath(filePath: FilePath)

    @Query("SELECT * FROM filePaths")
    fun allDrawingPaths(): Flow<List<FilePath>>
}