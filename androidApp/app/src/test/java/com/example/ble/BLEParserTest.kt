package com.example.ble

import org.junit.Test
import org.junit.Assert.*

class BLEParserTest {
    @Test
    fun testJsonParsing() {
        val payload = """{"temperature":25.0,"battery":100,"batteryVoltage":4.20,"charging":false,"sodium":"---","status":"Normal","firmware":"1.0.0","device":"NaTrack"}"""
        val result = BLEParser.parse(payload)
        
        assertNotNull("Result should not be null", result)
        assertEquals(25.0, result?.temperature)
        assertEquals(100, result?.battery)
        assertEquals(4.20, result?.batteryVoltage)
        assertEquals(false, result?.charging)
        assertEquals("---", result?.sodium)
        assertEquals("Normal", result?.status)
        assertEquals("1.0.0", result?.firmware)
        assertEquals("NaTrack", result?.device)
    }
}
