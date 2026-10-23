#pragma once

#include <Arduino.h>
#include "DeviceState.hpp"
#include "TemperatureSensor.hpp"

namespace Flux {

class SensorManager {
public:
    void begin(DeviceState* state) noexcept;
    void update() noexcept;

private:
    DeviceState* state_ = nullptr;
    TemperatureSensor tempSensor_;
    bool tempSensorConnected_ = false;
    unsigned long lastReadMs_ = 0UL;

    static constexpr unsigned long ReadIntervalMs = 1000UL;
};

} // namespace Flux
