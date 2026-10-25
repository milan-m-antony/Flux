#pragma once

#include <Arduino.h>
#include "DeviceState.hpp"
#include "FluxConfig.hpp"

namespace Flux {

enum class BuzzerPattern : uint8_t {
    Off = 0,
    Startup,
    AlertBeep,
    ConfirmationChirp
};

class BuzzerManager {
public:
    explicit BuzzerManager(uint8_t pin = Config::BuzzerPin) noexcept;

    void begin(DeviceState* state) noexcept;
    void update() noexcept;

    // Direct playback triggers for UI and actions
    void playChirp(uint16_t durationMs = 60, uint16_t freq = 2700) noexcept;
    void silence() noexcept;

    bool isSounding() const noexcept { return isSounding_; }
    uint8_t getPin() const noexcept { return pin_; }

private:
    void soundOn(uint16_t freq) noexcept;
    void soundOff() noexcept;

    uint8_t pin_;
    DeviceState* state_ = nullptr;
    bool isSounding_ = false;

    // Non-blocking pattern state machine
    BuzzerPattern currentPattern_ = BuzzerPattern::Off;
    uint8_t patternStep_ = 0;
    unsigned long stepStartMs_ = 0UL;
    unsigned long lastAlertCycleMs_ = 0UL;
};

} // namespace Flux
