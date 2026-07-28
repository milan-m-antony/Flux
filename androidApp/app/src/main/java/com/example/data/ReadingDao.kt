package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<Reading>>
    
    @Query("SELECT * FROM readings WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getReadingsSince(since: Long): Flow<List<Reading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: Reading)

    @Query("DELETE FROM readings")
    suspend fun deleteAll()
}
