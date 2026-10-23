#pragma once

#include <Arduino.h>
#include "DeviceState.hpp"

namespace Flux {

class SettingsManager {
public:
    void begin(DeviceState* state) noexcept;
    void update() noexcept;

    int getSettingsIndex() const noexcept {
        return settingsIndex_;
    }

    void advanceSettingsIndex() noexcept {
        settingsIndex_ = (settingsIndex_ + 1) % 5;
    }

private:
    DeviceState* state_ = nullptr;
    int settingsIndex_ = 0;
};

} // namespace Flux
