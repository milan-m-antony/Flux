#include "BuzzerManager.hpp"

namespace Flux {

BuzzerManager::BuzzerManager(uint8_t pin, bool activeLow) noexcept
    : pin_(pin), activeLow_(activeLow) {}

void BuzzerManager::begin(DeviceState* state) noexcept {
    state_ = state;
    silence();

    // Ensure pin is set to OUTPUT and driven to inactive state immediately at boot
    soundOff();

    Serial.printf("BuzzerManager initialized on GPIO %u (Active-%s, Temperature Alert Only)\n",
                  pin_, activeLow_ ? "Low" : "High");
}

void BuzzerManager::soundOn(uint16_t freq) noexcept {
    if (!isSounding_) {
        isSounding_ = true;
        pinMode(pin_, OUTPUT);
        if (activeLow_) {
            // Active-low buzzer (KY-012/PNP): Driven LOW turns ON the buzzer solidly
            digitalWrite(pin_, LOW);
        } else {
            #if defined(ESP32)
            tone(pin_, freq);
            #else
            digitalWrite(pin_, HIGH);
            #endif
        }
    }
}

void BuzzerManager::soundOff() noexcept {
    #if defined(ESP32)
    noTone(pin_);
    #endif
    pinMode(pin_, OUTPUT);
    if (activeLow_) {
        // Active-low buzzer (KY-012/PNP): Driven HIGH turns OFF the buzzer completely
        digitalWrite(pin_, HIGH);
    } else {
        digitalWrite(pin_, LOW);
    }
    isSounding_ = false;
}

void BuzzerManager::silence() noexcept {
    soundOff();
    currentPattern_ = BuzzerPattern::Off;
    patternStep_ = 0;
    stepStartMs_ = 0UL;
    lastAlertCycleMs_ = 0UL;
}

void BuzzerManager::playChirp(uint16_t /*durationMs*/, uint16_t /*freq*/) noexcept {
    // Non-alert confirmation chirps are strictly disabled per requirements
}

void BuzzerManager::update() noexcept {
    const unsigned long nowMs = millis();

    // Clear any stale chirp request without emitting sound
    if (state_ != nullptr && state_->requestBuzzerChirp) {
        state_->requestBuzzerChirp = false;
    }

    // 1. Alert Condition Evaluation:
    // Buzzer sounds ONLY during a genuine active temperature-threshold alert:
    // - State pointer is valid
    // - Buzzer is enabled in Preferences / BLE
    // - Sensor is connected and healthy
    // - Threshold breach is actively asserted
    const bool alertActive = (state_ != nullptr &&
                              state_->buzzerEnabled &&
                              state_->sensorConnected &&
                              state_->isThresholdAlert);

    // If alert condition is not met, ensure immediate silence and reset repeating pattern
    if (!alertActive) {
        if (currentPattern_ != BuzzerPattern::Off || isSounding_) {
            silence();
        }
        return;
    }

    // 2. Alert Pattern State Machine (Non-blocking, cooperative millis timing)
    // Periodic repeating alert burst:
    // Step 0: Pip 1 (120 ms)
    // Step 1: Pause (80 ms)
    // Step 2: Pip 2 (120 ms)
    // Step 3: Pause (80 ms)
    // Step 4: Pip 3 (150 ms)
    // Step 5: Quiet rest period between bursts (2000 ms)
    if (currentPattern_ != BuzzerPattern::AlertBeep) {
        currentPattern_ = BuzzerPattern::AlertBeep;
        patternStep_ = 0;
        stepStartMs_ = nowMs;
        lastAlertCycleMs_ = nowMs;
        soundOn(2400);
        return;
    }

    const unsigned long stepElapsed = nowMs - stepStartMs_;

    switch (patternStep_) {
    case 0: // Pip 1
        if (stepElapsed >= 120) {
            soundOff();
            patternStep_ = 1;
            stepStartMs_ = nowMs;
        }
        break;

    case 1: // Pause 1
        if (stepElapsed >= 80) {
            soundOn(2400);
            patternStep_ = 2;
            stepStartMs_ = nowMs;
        }
        break;

    case 2: // Pip 2
        if (stepElapsed >= 120) {
            soundOff();
            patternStep_ = 3;
            stepStartMs_ = nowMs;
        }
        break;

    case 3: // Pause 2
        if (stepElapsed >= 80) {
            soundOn(2600);
            patternStep_ = 4;
            stepStartMs_ = nowMs;
        }
        break;

    case 4: // Pip 3
        if (stepElapsed >= 150) {
            soundOff();
            patternStep_ = 5;
            stepStartMs_ = nowMs;
        }
        break;

    case 5: // Quiet rest period before next repeating burst (2000 ms)
        if (stepElapsed >= 2000) {
            soundOn(2400);
            patternStep_ = 0;
            stepStartMs_ = nowMs;
            lastAlertCycleMs_ = nowMs;
        }
        break;

    default:
        // Recover cleanly from any unexpected pattern step
        silence();
        break;
    }
}

} // namespace Flux
