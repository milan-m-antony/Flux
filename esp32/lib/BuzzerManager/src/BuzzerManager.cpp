#include "BuzzerManager.hpp"

namespace Flux {

BuzzerManager::BuzzerManager(uint8_t pin) noexcept
    : pin_(pin) {}

void BuzzerManager::begin(DeviceState* state) noexcept {
    state_ = state;
    isSounding_ = false;
    currentPattern_ = BuzzerPattern::Off;
    patternStep_ = 0;
    stepStartMs_ = millis();
    lastAlertCycleMs_ = 0UL;

    pinMode(pin_, OUTPUT);
    digitalWrite(pin_, LOW);

    Serial.printf("BuzzerManager initialized on GPIO %u (Temperature Alert Only)\n", pin_);
}

void BuzzerManager::soundOn(uint16_t freq) noexcept {
    #if defined(ESP32)
    tone(pin_, freq);
    #else
    digitalWrite(pin_, HIGH);
    #endif
    isSounding_ = true;
}

void BuzzerManager::soundOff() noexcept {
    #if defined(ESP32)
    noTone(pin_);
    ledcDetach(pin_);
    #endif
    pinMode(pin_, OUTPUT);
    digitalWrite(pin_, LOW);
    isSounding_ = false;
}

void BuzzerManager::silence() noexcept {
    soundOff();
    currentPattern_ = BuzzerPattern::Off;
    patternStep_ = 0;
    lastAlertCycleMs_ = 0UL;
}

void BuzzerManager::playChirp(uint16_t /*durationMs*/, uint16_t /*freq*/) noexcept {
    // Sound is strictly reserved for temperature alerts only
}

void BuzzerManager::update() noexcept {
    const unsigned long nowMs = millis();

    // 1. Mute guard: If buzzer is disabled in Settings or state is missing, force silence
    if (state_ == nullptr || !state_->buzzerEnabled) {
        if (isSounding_) {
            soundOff();
        }
        lastAlertCycleMs_ = 0UL;
        return;
    }

    // 2. Temperature Alert: ONLY sound when sensor is connected AND temperature breached threshold
    bool alertActive = (state_->sensorConnected && state_->isThresholdAlert);

    if (alertActive) {
        if (lastAlertCycleMs_ == 0UL) {
            lastAlertCycleMs_ = nowMs;
        }

        // Periodic non-continuous alarm burst:
        // 3 crisp distinct alert pips (~550ms total), followed by a quiet 10-second rest:
        // [0-120ms: Pip 1 (2400Hz)] -> [120-200ms: Rest]
        // [200-320ms: Pip 2 (2400Hz)] -> [320-400ms: Rest]
        // [400-550ms: Pip 3 (2600Hz)] -> [550-10000ms: Silence / Rest]
        unsigned long cycleElapsed = nowMs - lastAlertCycleMs_;

        if (cycleElapsed < 120) {
            soundOn(2400);
        } else if (cycleElapsed < 200) {
            soundOff();
        } else if (cycleElapsed < 320) {
            soundOn(2400);
        } else if (cycleElapsed < 400) {
            soundOff();
        } else if (cycleElapsed < 550) {
            soundOn(2600);
        } else if (cycleElapsed < 10000UL) {
            soundOff();
        } else {
            // Restart the 10-second alert cycle
            lastAlertCycleMs_ = nowMs;
            soundOn(2400);
        }
    } else {
        // Temperature normalized or sensor disconnected: immediately turn off sound
        if (isSounding_) {
            soundOff();
        }
        lastAlertCycleMs_ = 0UL;
    }
}

} // namespace Flux
