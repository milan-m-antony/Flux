#include "SensorManager.hpp"

namespace NaTrack {

void SensorManager::begin(DeviceState* state) noexcept {
    state_ = state;
    tempSensor_.begin();
    tempSensorConnected_ = (tempSensor_.getDeviceCount() > 0);
    lastReadMs_ = millis();

    if (state_ != nullptr) {
        state_->temperature = 0.0f; // Waiting for first real reading
        state_->sodiumValue = -1; // Waiting for sensor
        state_->sodiumStatus = "Waiting For Sensor";
    }
}

void SensorManager::update() noexcept {
    if (state_ == nullptr) {
        return;
    }

    const unsigned long nowMs = millis();
    if ((nowMs - lastReadMs_) >= ReadIntervalMs) {
        lastReadMs_ = nowMs;

        if (!tempSensorConnected_) {
            tempSensorConnected_ = (tempSensor_.getDeviceCount() > 0);
        }

        if (tempSensorConnected_) {
            uint8_t addr[8];
            int16_t rawTemp = 0;
            float tempVal = 0.0f;
            if (tempSensor_.readTemperatureDiagnostics(0, addr, rawTemp, tempVal)) {
                state_->temperature = tempVal;
            } else {
                tempSensorConnected_ = false;
            }
            tempSensor_.requestTemperature();
        }
    }
}

} // namespace NaTrack
