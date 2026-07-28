#include "MAX30102Sensor.hpp"

namespace NaTrack {

void MAX30102Sensor::begin() noexcept {
    // Sensor initialization will be added only after the base ESP32 layer is verified.
}

void MAX30102Sensor::update() noexcept {
    // The acquisition loop will stay separate from the startup verification code.
}

} // namespace NaTrack
