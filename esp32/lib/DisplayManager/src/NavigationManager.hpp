#pragma once

#include <Arduino.h>
#include "ButtonManager.hpp"
#include "DeviceState.hpp"

namespace NaTrack {

class NavigationManager : public ButtonListener {
public:
    void begin(DeviceState* state) noexcept;
    void update() noexcept;

    // From ButtonListener
    void onButtonEvent(ButtonId button, ButtonEvent event) noexcept override;

    bool isTransitioning() const noexcept {
        return transitioning_;
    }

    int16_t getSlideOffset() const noexcept;
    int getSettingsIndex() const noexcept {
        return settingsIndex_;
    }

private:
    DeviceState* state_ = nullptr;
    bool transitioning_ = false;
    unsigned long transitionStartMs_ = 0UL;
    bool slideLeft_ = true; // true = slide left (next), false = slide right (prev)
    int settingsIndex_ = 0;

    static constexpr unsigned long TransitionDurationMs = 200UL;
};

} // namespace NaTrack
