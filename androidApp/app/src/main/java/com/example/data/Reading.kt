package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "readings")
@Serializable
data class Reading(
    @PrimaryKey
    val id: String,
    val naValue: Double,
    val temperature: Double,
    val millivolts: Double,
    val status: String,
    val timestamp: Long,
    val calibrationE0: Double,
    val calibrationSlope: Double
)
