#include "SensorManager.hpp"

namespace Flux {

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
        state_->isThresholdAlert = false;
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
            const bool readOk = tempSensor_.readTemperatureDiagnostics(0, addr, rawTemp, tempVal);

            // DS18B20 Power-On Reset (POR) default value is +85.0C (raw 0x0550).
            // If the sensor is read before its first temperature conversion finishes,
            // or suffered a bus brownout, it returns 85.0C. Do not treat this as a valid alert.
            if (readOk && rawTemp == 0x0550 && state_->sampleCount == 0) {
                tempSensor_.requestTemperature();
                return;
            }

            if (readOk && (tempVal > -55.0f && tempVal < 125.0f)) {
                state_->sensorConnected = true;
                state_->temperature = tempVal;
                state_->temperatureF = (tempVal * 9.0f / 5.0f) + 32.0f;
                state_->addHistory(tempVal);

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

                // Threshold and profile evaluation using configured low and high limits
                if (tempVal < state_->lowThreshold || tempVal > state_->highThreshold) {
                    if (!state_->isThresholdAlert) {
                        state_->alertSentAnimMs = millis();
                    }
                    state_->isThresholdAlert = true;
                    state_->sensorStatus = "Threshold Alert";
                } else {
                    state_->isThresholdAlert = false;
                    state_->sensorStatus = "Normal";
                }
            } else {
                tempSensorConnected_ = false;
                state_->sensorConnected = false;
                state_->isThresholdAlert = false;
                state_->sensorStatus = readOk ? "Read Fault" : "Disconnected";
            }
            tempSensor_.requestTemperature();
        } else {
            state_->sensorConnected = false;
            state_->isThresholdAlert = false;
            state_->sensorStatus = "Disconnected";
        }
    }
}

} // namespace Flux
