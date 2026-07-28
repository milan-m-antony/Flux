package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

data class HealthSummary(
    val waterIntake: Double, // in Liters
    val waterGoal: Double,
    val bodyTemp: Double,
    val batteryLevel: Int,
    val bluetoothConnected: Boolean,
    val steps: Int,
    val calories: Int
)

data class TrendData(
    val timestamp: Long,
    val value: Double
)

class HealthRepository {
    
    fun getHealthSummary(): Flow<HealthSummary> = flow {
        emit(
            HealthSummary(0.0, 0.0, 0.0, 0, false, 0, 0)
        )
    }
    
    fun getWeeklyTrends(): Flow<List<TrendData>> = flow {
        emit(emptyList())
    }
}
