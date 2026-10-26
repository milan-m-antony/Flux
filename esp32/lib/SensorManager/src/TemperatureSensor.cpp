#include "TemperatureSensor.hpp"

namespace Flux {

bool TemperatureSensor::begin() noexcept {
    // Enable internal pull-up resistor (can sometimes work instead of an external resistor for short wires)
    pinMode(Config::TemperatureSensorPin, INPUT_PULLUP);

    // Initialize the DallasTemperature driver.
    sensor_.begin();

    // Use non-blocking conversion flow so the app can stay millis-driven.
    sensor_.setWaitForConversion(false);

    // Request the first conversion immediately.
    sensor_.requestTemperatures();

    // Treat the bus as ready even if no sensor is attached yet.
    return true;
}

uint8_t TemperatureSensor::getDeviceCount() noexcept {
    // Query how many DS18B20 devices are present on the bus.
    return static_cast<uint8_t>(sensor_.getDeviceCount());
}

bool TemperatureSensor::getDeviceAddress(uint8_t index, uint8_t* deviceAddress) noexcept {
    // Read the ROM address for the requested device index.
    return sensor_.getAddress(deviceAddress, index);
}

void TemperatureSensor::requestTemperature() noexcept {
    // Start a new temperature conversion.
    sensor_.requestTemperatures();
}

bool TemperatureSensor::readTemperatureDiagnostics(uint8_t index, uint8_t* deviceAddress, int16_t& rawTemperature, float& temperatureCelsius) noexcept {
    // Read the ROM address for the requested device index if needed by the caller.
    if (!getDeviceAddress(index, deviceAddress)) {
        return false;
    }

    // Read the raw DS18B20 temperature value.
    rawTemperature = sensor_.getTemp(deviceAddress);

    // Detect sensor disconnect or invalid readings.
    if (rawTemperature == DEVICE_DISCONNECTED_RAW ||
        rawTemperature == DEVICE_FAULT_OPEN_RAW ||
        rawTemperature == DEVICE_FAULT_SHORTGND_RAW ||
        rawTemperature == DEVICE_FAULT_SHORTVDD_RAW ||
        rawTemperature == DEVICE_POWER_ON_RESET_RAW ||
        rawTemperature == DEVICE_INSUFFICIENT_POWER_RAW ||
        rawTemperature <= -7040) {
        return false;
    }

    // Convert the raw temperature into Celsius.
    temperatureCelsius = sensor_.rawToCelsius(rawTemperature);
    return true;
}

} // namespace Flux
