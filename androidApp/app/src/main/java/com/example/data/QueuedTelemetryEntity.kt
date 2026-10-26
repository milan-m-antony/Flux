package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-backed durable offline queue entity.
 * Persists unsent telemetry readings locally to survive process termination,
 * app restarts, and offline network gaps.
 */
@Entity(
    tableName = "queued_telemetry",
    indices = [
        Index(value = ["recordedAtMs"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class QueuedTelemetryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idempotencyKey: String,
    val deviceId: String,
    val temperatureCelsius: Double,
    val recordedAtMs: Long,
    val activeProfile: String,
    val isAlert: Boolean,
    val source: String = "BLE",
    val batteryPercentage: Int? = null,
    val batteryVoltage: Double? = null,
    val sensorConnected: Boolean = true,
    val retryCount: Int = 0,
    val lastAttemptMs: Long = 0L,
    val createdAtMs: Long = System.currentTimeMillis()
)
