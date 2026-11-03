#include "BatteryManager.hpp"
#include <Arduino.h>

namespace Flux {

// GPIO 35 is standard ADC1 channel on ESP32 DevKit for battery voltage divider
constexpr uint8_t BatteryAdcPin = 35;

void BatteryManager::begin(DeviceState* state) noexcept {
    state_ = state;
    lastUpdateMs_ = millis();
    pinMode(BatteryAdcPin, INPUT);
    update();
}

void BatteryManager::update() noexcept {
    if (state_ == nullptr) {
        return;
    }

    unsigned long now = millis();
    if (now - lastUpdateMs_ < 2000UL && lastUpdateMs_ != 0UL) {
        return;
    }
    lastUpdateMs_ = now;

    // Read 12-bit analog voltage (0 - 4095)
    int raw = analogRead(BatteryAdcPin);
    // Standard 2:1 divider (e.g. 100k + 100k): V_batt = (raw / 4095.0) * 3.3 * 2.0
    float pinVolts = (raw / 4095.0f) * 3.3f;
    float battVolts = pinVolts * 2.0f;

    if (battVolts >= 3.0f && battVolts <= 4.35f) {
        // Active LiPo / Li-Ion battery connected
        state_->batteryVoltage = battVolts;
        int pct = (int)(((battVolts - 3.2f) / (4.2f - 3.2f)) * 100.0f);
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        state_->batteryPercentage = pct;
        state_->charging = (battVolts > 4.18f);
    } else {
        // Running on continuous USB 5V VBUS power
        state_->batteryVoltage = 5.0f;
        state_->batteryPercentage = 100;
        state_->charging = true;
    }
}

} // namespace Flux
