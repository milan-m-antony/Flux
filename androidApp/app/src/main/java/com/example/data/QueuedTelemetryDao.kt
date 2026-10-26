package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QueuedTelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QueuedTelemetryEntity): Long

    @Query("SELECT * FROM queued_telemetry ORDER BY recordedAtMs ASC LIMIT :limit")
    suspend fun getOldestBatch(limit: Int): List<QueuedTelemetryEntity>

    @Query("DELETE FROM queued_telemetry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM queued_telemetry WHERE id IN (:ids)")
    suspend fun deleteBatch(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM queued_telemetry")
    fun getQueueCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM queued_telemetry")
    suspend fun getQueueCount(): Int

    @Update
    suspend fun update(item: QueuedTelemetryEntity)

    /**
     * Bounded queue protection: Prune oldest records if queue exceeds max capacity (e.g. 500)
     */
    @Query("DELETE FROM queued_telemetry WHERE id NOT IN (SELECT id FROM queued_telemetry ORDER BY recordedAtMs DESC LIMIT :keepCount)")
    suspend fun pruneOldest(keepCount: Int = 500)
}
