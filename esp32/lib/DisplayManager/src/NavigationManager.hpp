#pragma once

#include <Arduino.h>
#include "ButtonManager.hpp"
#include "DeviceState.hpp"

namespace Flux {

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
        if (state_ && state_->activeScreen == ScreenId::ModeSelection) {
            return modeIndex_;
        }
        if (state_ && state_->activeScreen == ScreenId::Preferences) {
            return prefIndex_;
        }
        return settingsIndex_;
    }

private:
    DeviceState* state_ = nullptr;
    bool transitioning_ = false;
    unsigned long transitionStartMs_ = 0UL;
    bool slideLeft_ = true; // true = slide left (next), false = slide right (prev)
    int settingsIndex_ = 0;
    int modeIndex_ = 0;
    int prefIndex_ = 0;

    static constexpr unsigned long TransitionDurationMs = 200UL;
};

} // namespace Flux
