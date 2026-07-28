package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ReadingRepository(private val readingDao: ReadingDao) {
    val allReadings: Flow<List<Reading>> = readingDao.getAllReadings()
    
    fun getReadingsSince(since: Long): Flow<List<Reading>> = readingDao.getReadingsSince(since)

    suspend fun insert(
        naValue: Double,
        temperature: Double,
        millivolts: Double,
        status: String,
        calibrationE0: Double,
        calibrationSlope: Double
    ) {
        val reading = Reading(
            id = UUID.randomUUID().toString(),
            naValue = naValue,
            temperature = temperature,
            millivolts = millivolts,
            status = status,
            timestamp = System.currentTimeMillis(),
            calibrationE0 = calibrationE0,
            calibrationSlope = calibrationSlope
        )
        readingDao.insertReading(reading)
    }
    
    suspend fun insertReading(reading: Reading) {
        readingDao.insertReading(reading)
    }

    suspend fun deleteAll() = readingDao.deleteAll()
}
