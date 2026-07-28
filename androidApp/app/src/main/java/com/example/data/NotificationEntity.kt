package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notifications")
@Serializable
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val time: String,
    val type: String, // INFO, WARNING, ALERT
    val isRead: Boolean = false
)
