package com.example.ble

import org.junit.Test
import org.junit.Assert.*

class BLEParserTest {
    @Test
    fun testJsonParsing() {
        val payload = """{"temperature":25.0,"battery":100,"batteryVoltage":4.20,"charging":false,"sodium":"---","status":"Normal","firmware":"1.0.0","device":"Flux ESP32"}"""
        val result = BLEParser.parse(payload)
        
        assertNotNull("Result should not be null", result)
        assertEquals(25.0, result?.temperature)
        assertEquals(100, result?.battery)
        assertEquals(4.20, result?.batteryVoltage)
        assertEquals(false, result?.charging)
        assertEquals("---", result?.sodium)
        assertEquals("Normal", result?.status)
        assertEquals("1.0.0", result?.firmware)
        assertEquals("Flux ESP32", result?.device)
        assertEquals(false, result?.wifiConnected)
        assertEquals(0, result?.wifiRssi)

        val wifiPayload = """{"temperature":26.5,"battery":85,"batteryVoltage":4.02,"charging":false,"sodium":"---","status":"Normal","firmware":"2.0.0","device":"Flux1","wifiConnected":true,"wifiRssi":-57,"wifiSsid":"Flux1"}"""
        val wifiResult = BLEParser.parse(wifiPayload)
        assertNotNull(wifiResult)
        assertEquals(true, wifiResult?.wifiConnected)
        assertEquals(-57, wifiResult?.wifiRssi)
        assertEquals("Flux1", wifiResult?.wifiSsid)
        assertEquals("Flux1", wifiResult?.device)

        val defaultPayload = """{"temperature":20.0,"battery":80,"batteryVoltage":3.9,"charging":false}"""
        val defaultResult = BLEParser.parse(defaultPayload)
        assertNotNull(defaultResult)
        assertEquals("Flux1", defaultResult?.device)
    }
}
