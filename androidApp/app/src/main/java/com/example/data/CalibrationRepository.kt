package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CalibrationRepository(private val calibrationDao: CalibrationDao) {
    val allCalibrations: Flow<List<CalibrationHistory>> = calibrationDao.getAllCalibrations()
    val lastCalibration: Flow<CalibrationHistory?> = calibrationDao.getLastCalibration()

    suspend fun insert(e0: Double, slope: Double) {
        val calibration = CalibrationHistory(
            id = UUID.randomUUID().toString(),
            e0 = e0,
            slope = slope,
            timestamp = System.currentTimeMillis()
        )
        calibrationDao.insertCalibration(calibration)
    }
}
