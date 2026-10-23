package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "readings")
@Serializable
data class Reading(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val temperature: Double = 0.0,
    val status: String = "OK",
    val timestamp: Long = System.currentTimeMillis(),
    val naValue: Double = 0.0,
    val millivolts: Double = 0.0,
    val calibrationE0: Double = 0.0,
    val calibrationSlope: Double = 0.0
)
