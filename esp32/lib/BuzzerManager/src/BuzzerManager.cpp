#include "BuzzerManager.hpp"

namespace Flux {

BuzzerManager::BuzzerManager(uint8_t pin) noexcept
    : pin_(pin) {}

void BuzzerManager::begin(DeviceState* state) noexcept {
    state_ = state;
    isSounding_ = false;
    currentPattern_ = BuzzerPattern::Startup;
    patternStep_ = 0;
    stepStartMs_ = millis();
    lastAlertCycleMs_ = millis();

    pinMode(pin_, OUTPUT);
    digitalWrite(pin_, LOW);

    Serial.printf("BuzzerManager initialized on GPIO %u\n", pin_);
}

void BuzzerManager::soundOn(uint16_t freq) noexcept {
    if (!isSounding_) {
        isSounding_ = true;
        #if defined(ESP32)
        tone(pin_, freq);
        #else
        digitalWrite(pin_, HIGH);
        #endif
    }
}

void BuzzerManager::soundOff() noexcept {
    if (isSounding_) {
        isSounding_ = false;
        #if defined(ESP32)
        noTone(pin_);
        #endif
        pinMode(pin_, OUTPUT);
        digitalWrite(pin_, LOW);
    }
}

void BuzzerManager::silence() noexcept {
    soundOff();
    currentPattern_ = BuzzerPattern::Off;
    patternStep_ = 0;
}

void BuzzerManager::playChirp(uint16_t durationMs, uint16_t freq) noexcept {
    if (state_ != nullptr && !state_->buzzerEnabled) {
        return;
    }
    currentPattern_ = BuzzerPattern::ConfirmationChirp;
    patternStep_ = 0;
    stepStartMs_ = millis();
}

void BuzzerManager::update() noexcept {
    const unsigned long nowMs = millis();

    // 1. Mute guard: If buzzer is disabled in Settings, force silence immediately
    if (state_ != nullptr && !state_->buzzerEnabled) {
        if (isSounding_) {
            silence();
        }
        currentPattern_ = BuzzerPattern::Off;
        return;
    }

    // 2. Process asynchronous chirp request from UI or BLE
    if (state_ != nullptr && state_->requestBuzzerChirp) {
        state_->requestBuzzerChirp = false;
        if (state_->buzzerEnabled) {
            playChirp();
        }
    }

    // 3. One-shot startup chime (crisp ascending two-tone)
    if (currentPattern_ == BuzzerPattern::Startup) {
        unsigned long elapsed = nowMs - stepStartMs_;
        if (patternStep_ == 0) {
            soundOn(2700);
            if (elapsed >= 80) {
                soundOff();
                patternStep_ = 1;
                stepStartMs_ = nowMs;
            }
        } else if (patternStep_ == 1) {
            soundOff();
            if (elapsed >= 50) {
                patternStep_ = 2;
                stepStartMs_ = nowMs;
            }
        } else if (patternStep_ == 2) {
            soundOn(3300);
            if (elapsed >= 90) {
                soundOff();
                currentPattern_ = BuzzerPattern::Off;
                patternStep_ = 0;
                lastAlertCycleMs_ = nowMs;
            }
        }
        return;
    }

    // 4. One-shot confirmation chirp (single high-frequency pip)
    if (currentPattern_ == BuzzerPattern::ConfirmationChirp) {
        unsigned long elapsed = nowMs - stepStartMs_;
        if (patternStep_ == 0) {
            soundOn(3000);
            if (elapsed >= 60) {
                soundOff();
                currentPattern_ = BuzzerPattern::Off;
                patternStep_ = 0;
            }
        }
        return;
    }

    // 5. Continuous Alert handling when threshold is breached
    bool alertActive = (state_ != nullptr && state_->isThresholdAlert && state_->buzzerEnabled);

    if (alertActive) {
        // Double-beep alarm burst:
        // [0-140ms: Beep 2400Hz] -> [140-220ms: Rest] -> [220-360ms: Beep 2600Hz] -> [360-1600ms: Rest]
        unsigned long cycleElapsed = nowMs - lastAlertCycleMs_;

        if (cycleElapsed < 140) {
            soundOn(2400);
        } else if (cycleElapsed < 220) {
            soundOff();
        } else if (cycleElapsed < 360) {
            soundOn(2600);
        } else if (cycleElapsed < 1600) {
            soundOff();
        } else {
            // Cycle restarts
            lastAlertCycleMs_ = nowMs;
            soundOn(2400);
        }
    } else {
        if (isSounding_) {
            soundOff();
        }
        lastAlertCycleMs_ = nowMs;
    }
}

} // namespace Flux
