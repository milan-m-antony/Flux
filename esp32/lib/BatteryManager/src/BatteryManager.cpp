#include "BatteryManager.hpp"

namespace NaTrack {

void BatteryManager::begin(DeviceState* state) noexcept {
    state_ = state;
    lastUpdateMs_ = millis();

    if (state_ != nullptr) {
        state_->batteryPercentage = 87;
        state_->batteryVoltage = 0.0f; // 0.0V represents Not Connected (voltage --)
        state_->charging = false;
    }
}

void BatteryManager::update() noexcept {
    if (state_ == nullptr) {
        return;
    }
    // Future ADC battery read logic goes here
}

} // namespace NaTrack
