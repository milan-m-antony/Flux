#pragma once

#include <Arduino.h>
#include "DeviceState.hpp"

namespace Flux {

class BatteryManager {
public:
    void begin(DeviceState* state) noexcept;
    void update() noexcept;

private:
    DeviceState* state_ = nullptr;
    unsigned long lastUpdateMs_ = 0UL;
};

} // namespace Flux
