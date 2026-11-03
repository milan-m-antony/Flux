package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id ASC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("UPDATE notifications SET title = REPLACE(REPLACE(title, 'NaTrack', 'Flux'), 'natrack', 'Flux'), message = REPLACE(REPLACE(message, 'NaTrack', 'Flux'), 'natrack', 'Flux') WHERE title LIKE '%natrack%' OR message LIKE '%natrack%'")
    suspend fun sanitizeExistingNotifications()
}
