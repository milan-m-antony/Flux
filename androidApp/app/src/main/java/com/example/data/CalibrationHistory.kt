package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "calibrations")
@Serializable
data class CalibrationHistory(
    @PrimaryKey
    val id: String,
    val e0: Double,
    val slope: Double,
    val timestamp: Long
)
