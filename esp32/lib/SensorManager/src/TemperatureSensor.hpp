#pragma once

#include <Arduino.h>

#include <DallasTemperature.h>
#include <OneWire.h>

#include "NaTrackConfig.hpp"

namespace NaTrack {

class TemperatureSensor {
public:
    // Prepares the DS18B20 sensor layer for temperature measurements.
    bool begin() noexcept;

    // Returns the number of DS18B20 devices detected on the OneWire bus.
    uint8_t getDeviceCount() noexcept;

    // Reads the ROM address of a DS18B20 device by index.
    bool getDeviceAddress(uint8_t index, uint8_t* deviceAddress) noexcept;

    // Requests a new DS18B20 temperature conversion.
    void requestTemperature() noexcept;

    // Reads the latest converted temperature value and raw reading by device index.
    bool readTemperatureDiagnostics(uint8_t index, uint8_t* deviceAddress, int16_t& rawTemperature, float& temperatureCelsius) noexcept;

private:
    // Owns the 1-Wire bus on the DS18B20 data pin.
    OneWire oneWire_{Config::TemperatureSensorPin};

    // Owns the DallasTemperature driver on top of the OneWire bus.
    DallasTemperature sensor_{&oneWire_};
};

} // namespace NaTrack
