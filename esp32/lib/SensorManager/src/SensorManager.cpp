#include "SensorManager.hpp"

namespace NaTrack {

void SensorManager::begin(DeviceState* state) noexcept {
    state_ = state;
    tempSensor_.begin();
    tempSensorConnected_ = (tempSensor_.getDeviceCount() > 0);
    lastReadMs_ = millis();

    if (state_ != nullptr) {
        state_->temperature = 0.0f;
        state_->temperatureF = 32.0f;
        state_->sensorConnected = tempSensorConnected_;
        state_->sensorStatus = tempSensorConnected_ ? "Online" : "Disconnected";
        state_->minTemperature = 999.0f;
        state_->maxTemperature = -999.0f;
        state_->avgTemperature = 0.0f;
        state_->tempSum = 0.0;
        state_->sampleCount = 0;
        state_->sessionStartMs = millis();
        state_->sodiumValue = -1;
        state_->sodiumStatus = "Normal";
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
                // Ensure valid reading (DS18B20 returns -127 if disconnected/bus error)
                if (tempVal > -55.0f && tempVal < 125.0f) {
                    state_->sensorConnected = true;
                    state_->temperature = tempVal;
                    state_->temperatureF = (tempVal * 9.0f / 5.0f) + 32.0f;

                    // Update session statistics
                    if (tempVal < state_->minTemperature) {
                        state_->minTemperature = tempVal;
                    }
                    if (tempVal > state_->maxTemperature) {
                        state_->maxTemperature = tempVal;
                    }
                    state_->tempSum += tempVal;
                    state_->sampleCount++;
                    state_->avgTemperature = static_cast<float>(state_->tempSum / state_->sampleCount);

                    // Threshold and profile evaluation
                    if (tempVal < state_->lowThreshold || tempVal > state_->highThreshold) {
                        state_->isThresholdAlert = true;
                        state_->sensorStatus = "Threshold Alert";
                    } else {
                        state_->isThresholdAlert = false;
                        state_->sensorStatus = "Normal";
                    }
                } else {
                    tempSensorConnected_ = false;
                    state_->sensorConnected = false;
                    state_->sensorStatus = "Read Fault";
                }
            } else {
                tempSensorConnected_ = false;
                state_->sensorConnected = false;
                state_->sensorStatus = "Disconnected";
            }
            tempSensor_.requestTemperature();
        } else {
            state_->sensorConnected = false;
            state_->sensorStatus = "Disconnected";
        }
    }
}

} // namespace NaTrack
