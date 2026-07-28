#pragma once

#include <Arduino.h>

namespace NaTrack {

class MAX30102Sensor {
public:
    // Prepares the pulse oximeter sensor layer for future integration work.
    void begin() noexcept;

    // Keeps the sensor interface ready for non-blocking acquisition logic.
    void update() noexcept;
};

} // namespace NaTrack
