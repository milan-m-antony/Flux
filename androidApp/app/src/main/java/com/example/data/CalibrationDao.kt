package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibrationDao {
    @Query("SELECT * FROM calibrations ORDER BY timestamp DESC")
    fun getAllCalibrations(): Flow<List<CalibrationHistory>>
    
    @Query("SELECT * FROM calibrations ORDER BY timestamp DESC LIMIT 1")
    fun getLastCalibration(): Flow<CalibrationHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalibration(calibration: CalibrationHistory)
}
